package com.nameless.cartio.features.shopping.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nameless.cartio.features.shopping.data.ShoppingListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListDetailScreen(
    onNavigateUp: () -> Unit,
    viewModel: ShoppingListDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listDeleted by viewModel.listDeleted.collectAsStateWithLifecycle()

    LaunchedEffect(listDeleted) {
        if (listDeleted) onNavigateUp()
    }

    var showRenameDialog by rememberSaveable { mutableStateOf(false) }
    var showMoreMenu by rememberSaveable { mutableStateOf(false) }
    var itemInputText by rememberSaveable { mutableStateOf("") }

    val totalCount = uiState.activeItems.size + uiState.checkedItems.size
    val progressFraction = if (totalCount > 0) uiState.checkedItems.size.toFloat() / totalCount else 0f

    if (showRenameDialog) {
        RenameListDialog(
            currentName = uiState.listName,
            onConfirm = { newName ->
                viewModel.renameList(newName)
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false }
        )
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = uiState.listName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Rounded.Edit,
                                    contentDescription = "Rename list",
                                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { showRenameDialog = true }
                                )
                            }
                            Text(
                                text = if (totalCount == 0) "Empty list"
                                else "${uiState.checkedItems.size} of $totalCount picked up",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                    },
                    actions = {
                        val sortIcon = when (uiState.sortOrder) {
                            SortOrder.ALPHA_DESC -> Icons.AutoMirrored.Rounded.Sort
                            else -> Icons.AutoMirrored.Rounded.Sort
                        }
                        val sortDescription = when (uiState.sortOrder) {
                            SortOrder.DEFAULT -> "Sort A to Z"
                            SortOrder.ALPHA_ASC -> "Sort Z to A"
                            SortOrder.ALPHA_DESC -> "Remove sort"
                        }
                        IconButton(onClick = { viewModel.toggleSort() }) {
                            Icon(
                                sortIcon,
                                contentDescription = sortDescription,
                                tint = if (uiState.sortOrder != SortOrder.DEFAULT)
                                    Color(0xFFFFB300)
                                else
                                    MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Box {
                            IconButton(onClick = { showMoreMenu = true }) {
                                Icon(
                                    Icons.Rounded.MoreVert,
                                    contentDescription = "More options",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Delete list") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Rounded.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        viewModel.deleteList()
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = Color(0xFFFFB300),
                    trackColor = MaterialTheme.colorScheme.primary,
                    strokeCap = StrokeCap.Square
                )
            }
        },
        bottomBar = {
            AddItemBar(
                text = itemInputText,
                onTextChange = { itemInputText = it },
                onAdd = {
                    if (itemInputText.isNotBlank()) {
                        viewModel.addItem(itemInputText)
                        itemInputText = ""
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { scaffoldPadding ->
        if (!uiState.isLoading && totalCount == 0) {
            EmptyShoppingListState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding)
            ) {
                items(uiState.activeItems, key = { it.id }) { item ->
                    SwipeableItemRow(
                        item = item,
                        onCheck = { viewModel.checkItem(item.id, true) },
                        onDelete = { viewModel.deleteItem(item.id) },
                        onCheckboxTap = { viewModel.checkItem(item.id, true) },
                        onIncrement = { viewModel.updateQuantity(item.id, item.quantity + 1) },
                        onDecrement = {
                            if (item.quantity > 1) viewModel.updateQuantity(item.id, item.quantity - 1)
                            else viewModel.deleteItem(item.id)
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }

                if (uiState.checkedItems.isNotEmpty()) {
                    item(key = "done_header") {
                        DoneSectionHeader(count = uiState.checkedItems.size)
                    }
                    items(uiState.checkedItems, key = { it.id }) { item ->
                        SwipeableItemRow(
                            item = item,
                            isCompleted = true,
                            onCheck = { viewModel.checkItem(item.id, false) },
                            onDelete = { viewModel.deleteItem(item.id) },
                            onCheckboxTap = { viewModel.checkItem(item.id, false) },
                            onIncrement = { viewModel.updateQuantity(item.id, item.quantity + 1) },
                            onDecrement = {
                                if (item.quantity > 1) viewModel.updateQuantity(item.id, item.quantity - 1)
                                else viewModel.deleteItem(item.id)
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableItemRow(
    item: ShoppingListItem,
    isCompleted: Boolean = false,
    onCheck: () -> Unit,
    onDelete: () -> Unit,
    onCheckboxTap: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> { onCheck(); true }
                SwipeToDismissBoxValue.EndToStart -> { onDelete(); true }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val target = dismissState.targetValue
            val isDeleteSide = target == SwipeToDismissBoxValue.EndToStart
            val bgColor by animateColorAsState(
                targetValue = when (target) {
                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50)
                    SwipeToDismissBoxValue.EndToStart -> Color(0xFFE53935)
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                },
                label = "swipe_bg"
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgColor)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = if (isDeleteSide) Arrangement.End else Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isDeleteSide) {
                    Text(
                        "DELETE",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Rounded.Delete, contentDescription = null, tint = Color.White)
                } else {
                    Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "CHECK",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    ) {
        ShoppingListItemRow(
            item = item,
            isCompleted = isCompleted,
            onCheckboxTap = onCheckboxTap,
            onIncrement = onIncrement,
            onDecrement = onDecrement
        )
    }
}

@Composable
private fun ShoppingListItemRow(
    item: ShoppingListItem,
    isCompleted: Boolean,
    onCheckboxTap: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleCheckbox(checked = isCompleted, onClick = onCheckboxTap)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = item.productName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
            color = if (isCompleted) MaterialTheme.colorScheme.outline
            else MaterialTheme.colorScheme.onBackground
        )
        QuantityStepper(
            quantity = item.quantity,
            isCompleted = isCompleted,
            onIncrement = onIncrement,
            onDecrement = onDecrement
        )
    }
}

@Composable
private fun CircleCheckbox(checked: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(
                if (checked) MaterialTheme.colorScheme.primary else Color.Transparent
            )
            .border(
                width = if (checked) 0.dp else 1.5.dp,
                color = if (checked) Color.Transparent else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                Icons.Rounded.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun QuantityStepper(
    quantity: Int,
    isCompleted: Boolean,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val alpha = if (isCompleted) 0.4f else 1f
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = alpha), CircleShape)
                .clickable(onClick = onDecrement),
            contentAlignment = Alignment.Center
        ) {
            if (!isCompleted && quantity == 1) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Remove item",
                    tint = Color(0xFFE53935).copy(alpha = alpha),
                    modifier = Modifier.size(14.dp)
                )
            } else {
                Text(
                    "−",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = alpha)
                )
            }
        }
        Text(
            text = "$quantity",
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = alpha)
        )
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = alpha), CircleShape)
                .clickable(onClick = onIncrement),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "+",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = alpha)
            )
        }
    }
}

@Composable
private fun DoneSectionHeader(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "DONE · $count",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.width(8.dp))
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun AddItemBar(
    text: String,
    onTextChange: (String) -> Unit,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(36.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(36.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.ShoppingCart,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(
                    "Add item...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onAdd() }),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onAdd),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Add,
                contentDescription = "Add item",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun EmptyShoppingListState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Rounded.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nothing here yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Type items below to start building your list.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun RenameListDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by rememberSaveable { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename list") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                label = { Text("List name") }
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }, enabled = text.isNotBlank()) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
