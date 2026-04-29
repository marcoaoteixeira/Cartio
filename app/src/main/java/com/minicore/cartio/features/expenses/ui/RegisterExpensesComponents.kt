package com.minicore.cartio.features.expenses.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.minicore.cartio.core.database.entity.MeasureUnit
import com.minicore.cartio.core.format.CurrencyFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseItemRow(
    row: ExpenseRowState,
    onPriceChange: (String) -> Unit,
    onMeasureUnitChange: (MeasureUnit) -> Unit,
    isLastRow: Boolean = false,
    modifier: Modifier = Modifier
) {
    val unitPrice = row.unitPrice.toDoubleOrNull() ?: 0.0
    val lineTotal = unitPrice * row.quantity

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = row.productName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = CurrencyFormat.format(lineTotal),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${row.quantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                MeasureUnitDropdown(
                    selected = row.measureUnit,
                    onSelected = onMeasureUnitChange,
                    modifier = Modifier.width(100.dp)
                )
                OutlinedTextField(
                    value = row.unitPrice,
                    onValueChange = { if (isValidMonetaryInput(it)) onPriceChange(it) },
                    prefix = { Text("$") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = if (isLastRow) ImeAction.Done else ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { state ->
                            if (!state.isFocused) {
                                val normalised = normalisePrice(row.unitPrice)
                                if (normalised != row.unitPrice) onPriceChange(normalised)
                            }
                        }
                )
            }
        }
    }
}

private val monetaryInputRegex = Regex("""^\d*\.?\d{0,2}$""")

private fun isValidMonetaryInput(input: String): Boolean =
    input.isEmpty() || input.matches(monetaryInputRegex)

/**
 * Strips leading zeros so user input "005.50" becomes "5.50" on blur, while
 * preserving "0", "0.50" and the empty string. Pure string transform — no
 * locale concerns since we already constrain input to ASCII digits + dot.
 */
private fun normalisePrice(raw: String): String {
    if (raw.isEmpty() || raw == "0") return raw
    if (raw.startsWith(".")) return "0$raw"
    val (whole, frac) = raw.split('.', limit = 2).let { it[0] to it.getOrNull(1) }
    val trimmedWhole = whole.trimStart('0').ifEmpty { "0" }
    return if (frac == null) trimmedWhole else "$trimmedWhole.$frac"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeasureUnitDropdown(
    selected: MeasureUnit,
    onSelected: (MeasureUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected.abbreviation,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true,
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            MeasureUnit.entries.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.abbreviation) },
                    onClick = {
                        onSelected(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}
