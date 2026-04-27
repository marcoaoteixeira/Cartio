package com.nameless.cartio.features.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nameless.cartio.features.expenses.data.ExpenseRepository
import com.nameless.cartio.features.reports.domain.GetSpendingReportUseCase
import com.nameless.cartio.features.reports.domain.SpendingReport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val getSpendingReport: GetSpendingReportUseCase
) : ViewModel() {

    val report: StateFlow<SpendingReport?> = getSpendingReport()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), null)

    init {
        viewModelScope.launch {
            val cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
            expenseRepository.purgeOlderThan(cutoff)
        }
    }
}
