package com.example.accountingapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.accountingapp.data.model.Category
import com.example.accountingapp.data.model.Transaction
import com.example.accountingapp.data.model.TransactionType
import com.example.accountingapp.data.repository.AppRepository
import com.example.accountingapp.platform.createPlatformSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private data class HomeStatsData(
    val monthIncome: Double,
    val monthExpense: Double,
    val totalIncome: Double,
    val totalExpense: Double,
    val transactions: List<Transaction>
)

enum class BalanceType { MONTHLY, TOTAL }

data class HomeUiState(
    val monthIncome: Double = 0.0,
    val monthExpense: Double = 0.0,
    val monthBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalBalance: Double = 0.0,
    val recentTransactions: List<TransactionWithCategory> = emptyList(),
    val allCategories: List<Category> = emptyList(),
    val currentMonth: Int = 0,
    val currentYear: Int = 0,
    val selectedDate: Long? = null,
    val balanceType: BalanceType = BalanceType.MONTHLY,
    val showAlternate: Boolean = false
)

data class TransactionWithCategory(
    val transaction: Transaction,
    val categoryEmoji: String,
    val categoryName: String
)

class HomeViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val prefs = createPlatformSettings("home_prefs")
    private val _selectedDate = MutableStateFlow<Long?>(null)
    val selectedDate: StateFlow<Long?> = _selectedDate

    private val _balanceType = MutableStateFlow(
        when (prefs.getString("balance_type", "MONTHLY")) {
            "TOTAL" -> BalanceType.TOTAL
            else -> BalanceType.MONTHLY
        }
    )
    private val _showAlternate = MutableStateFlow(false)

    private val allTimeStart = 0L
    private val allTimeEnd = Clock.System.now().toEpochMilliseconds()

    // 当前自然月的起止毫秒时间戳
    private fun monthBounds(): Pair<Long, Long> {
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date
        val monthStart = LocalDate(today.year, today.monthNumber, 1)
            .atStartOfDayIn(tz)
            .toEpochMilliseconds()
        val nextMonthStart = monthStartDate(today.year, today.monthNumber)
            .plus(1, DateTimeUnit.MONTH)
        val monthEnd = nextMonthStart.atStartOfDayIn(tz).toEpochMilliseconds() - 1
        return monthStart to monthEnd
    }

    private fun monthStartDate(year: Int, month: Int): LocalDate =
        LocalDate(year, month, 1)

    private val allCategories: Flow<List<Category>> = repository.getAllCategories()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = combine(
        combine(
            repository.getTotalByType(TransactionType.INCOME, monthBounds().first, monthBounds().second),
            repository.getTotalByType(TransactionType.EXPENSE, monthBounds().first, monthBounds().second),
            repository.getTotalByType(TransactionType.INCOME, allTimeStart, allTimeEnd),
            repository.getTotalByType(TransactionType.EXPENSE, allTimeStart, allTimeEnd),
            _selectedDate.flatMapLatest { date ->
                if (date != null) {
                    repository.getTransactionsByDateRange(date, date + 86_399_999)
                } else {
                    repository.getRecentTransactions(200)
                }
            }
        ) { mi, me, ti, te, tx -> HomeStatsData(mi, me, ti, te, tx) },
        allCategories,
        _balanceType,
        _showAlternate
    ) { homeData, categories, balanceType, showAlternate ->
        val txWithCat = homeData.transactions.map { tx ->
            val cat = categories.find { it.id == tx.categoryId }
            TransactionWithCategory(
                transaction = tx,
                categoryEmoji = cat?.emoji ?: "📌",
                categoryName = cat?.name ?: "未分类"
            )
        }

        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        HomeUiState(
            monthIncome = homeData.monthIncome,
            monthExpense = homeData.monthExpense,
            monthBalance = homeData.monthIncome - homeData.monthExpense,
            totalIncome = homeData.totalIncome,
            totalExpense = homeData.totalExpense,
            totalBalance = homeData.totalIncome - homeData.totalExpense,
            recentTransactions = txWithCat,
            allCategories = categories,
            currentMonth = now.monthNumber,
            currentYear = now.year,
            selectedDate = _selectedDate.value,
            balanceType = balanceType,
            showAlternate = showAlternate
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun selectDate(timestamp: Long) {
        _selectedDate.value = timestamp
    }

    fun goToToday() {
        _selectedDate.value = null
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun toggleBalance() {
        _showAlternate.value = !_showAlternate.value
    }

    fun switchDefaultBalance() {
        _showAlternate.value = false
        val newType = when (_balanceType.value) {
            BalanceType.MONTHLY -> BalanceType.TOTAL
            BalanceType.TOTAL -> BalanceType.MONTHLY
        }
        _balanceType.value = newType
        prefs.putString("balance_type", newType.name)
    }
}
