package com.example.accountingapp.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.accountingapp.data.db.CategorySummary
import com.example.accountingapp.data.model.Category
import com.example.accountingapp.data.model.Transaction
import com.example.accountingapp.data.model.TransactionType
import com.example.accountingapp.data.repository.AppRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

enum class TimeRange { DAY, WEEK, MONTH, YEAR }

data class CategoryStat(
    val category: Category,
    val total: Double,
    val percentage: Float
)

private data class TimeParams(
    val range: TimeRange,
    val year: Int,
    val month: Int,
    val day: Int,
    val weekOffset: Int,
    val yearOffset: Int
)

private data class StatsData(
    val income: Double,
    val expense: Double,
    val expenseSummary: List<CategorySummary>,
    val incomeSummary: List<CategorySummary>,
    val categories: List<Category>
)

data class StatsUiState(
    val timeRange: TimeRange = TimeRange.MONTH,
    val year: Int = 2024,
    val month: Int = 1,
    val day: Int = 1,
    val weekStart: Long = 0L,
    val weekEnd: Long = 0L,
    val weekLabel: String = "",
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val expenseCategories: List<CategoryStat> = emptyList(),
    val incomeCategories: List<CategoryStat> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val categoryFilter: Long? = null
)

class StatsViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val tz get() = TimeZone.currentSystemDefault()
    private val now = Clock.System.now().toLocalDateTime(tz)

    private val _timeRange = MutableStateFlow(TimeRange.MONTH)
    private val _year = MutableStateFlow(now.year)
    private val _month = MutableStateFlow(now.monthNumber)
    private val _day = MutableStateFlow(now.dayOfMonth)
    private val _weekOffset = MutableStateFlow(0)
    private val _yearOffset = MutableStateFlow(0)
    private val _customWeekAnchor = MutableStateFlow<Long?>(null)
    private val _categoryFilter = MutableStateFlow<Long?>(null)

    // ═══════════════════════════════════════════════════════
    // kotlinx-datetime 工具函数
    // ═══════════════════════════════════════════════════════

    /** LocalDate 的 00:00:00.000 毫秒时间戳 */
    private fun LocalDate.startOfDayMillis(): Long =
        atStartOfDayIn(tz).toEpochMilliseconds()

    /** LocalDate 的 23:59:59.999 毫秒时间戳 */
    private fun LocalDate.endOfDayMillis(): Long =
        atTime(LocalTime(23, 59, 59, 999_999_999))
            .toInstant(tz)
            .toEpochMilliseconds()

    /** 获取某年某月的天数 */
    private fun daysInMonth(year: Int, month: Int): Int {
        val firstOfMonth = LocalDate(year, month, 1)
        return firstOfMonth.plus(1, DateTimeUnit.MONTH)
            .minus(1, DateTimeUnit.DAY)
            .dayOfMonth
    }

    /** 获取本周一的日期 */
    private fun mondayOf(date: LocalDate): LocalDate {
        val daysSinceMonday = date.dayOfWeek.ordinal // MONDAY=0 ... SUNDAY=6
        return date.minus(daysSinceMonday, DateTimeUnit.DAY)
    }

    // ═══════════════════════════════════════════════════════
    // 日期范围计算
    // ═══════════════════════════════════════════════════════

    private fun getDayRange(year: Int, month: Int, day: Int): Pair<Long, Long> {
        val date = LocalDate(year, month, day)
        return date.startOfDayMillis() to date.endOfDayMillis()
    }

    private fun getWeekRange(): Pair<Long, Long> {
        val anchor = _customWeekAnchor.value
        if (anchor != null) {
            // 自定义 7 天块：从 anchor 日期开始
            val anchorDate = Instant.fromEpochMilliseconds(anchor).toLocalDateTime(tz).date
            val weekStart = anchorDate.plus(_weekOffset.value * 7, DateTimeUnit.DAY)
            val weekEnd = weekStart.plus(6, DateTimeUnit.DAY)
            return weekStart.startOfDayMillis() to weekEnd.endOfDayMillis()
        }
        // 默认 Mon-Sun
        val today = Clock.System.now().toLocalDateTime(tz).date
        val monday = mondayOf(today).plus(_weekOffset.value * 7, DateTimeUnit.DAY)
        val sunday = monday.plus(6, DateTimeUnit.DAY)
        return monday.startOfDayMillis() to sunday.endOfDayMillis()
    }

    private fun getMonthRange(year: Int, month: Int): Pair<Long, Long> {
        val firstDay = LocalDate(year, month, 1)
        val lastDay = LocalDate(year, month, daysInMonth(year, month))
        return firstDay.startOfDayMillis() to lastDay.endOfDayMillis()
    }

    private fun getYearRange(year: Int): Pair<Long, Long> {
        val firstDay = LocalDate(year, 1, 1)
        val lastDay = LocalDate(year, 12, 31)
        return firstDay.startOfDayMillis() to lastDay.endOfDayMillis()
    }

    private fun formatWeekLabel(startMs: Long, endMs: Long): String {
        val start = Instant.fromEpochMilliseconds(startMs).toLocalDateTime(tz).date
        val end = Instant.fromEpochMilliseconds(endMs).toLocalDateTime(tz).date
        val sm = start.monthNumber.toString().padStart(2, '0')
        val sd = start.dayOfMonth.toString().padStart(2, '0')
        val em = end.monthNumber.toString().padStart(2, '0')
        val ed = end.dayOfMonth.toString().padStart(2, '0')
        return "$sm/$sd - $em/$ed"
    }

    // ═══════════════════════════════════════════════════════
    // UI State
    // ═══════════════════════════════════════════════════════

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<StatsUiState> = combine(
        _timeRange, _year, _month, _day, _weekOffset, _yearOffset, _customWeekAnchor
    ) { vals: Array<Any?> ->
        TimeParams(
            range = vals[0] as TimeRange,
            year = vals[1] as Int,
            month = vals[2] as Int,
            day = vals[3] as Int,
            weekOffset = vals[4] as Int,
            yearOffset = vals[5] as Int
        )
    }.flatMapLatest { params ->
        val effectiveYear = now.year + params.yearOffset
        val (start, end) = when (params.range) {
            TimeRange.DAY -> getDayRange(params.year, params.month, params.day)
            TimeRange.WEEK -> getWeekRange()
            TimeRange.MONTH -> getMonthRange(params.year, params.month)
            TimeRange.YEAR -> getYearRange(effectiveYear)
        }
        val weekLabel = if (params.range == TimeRange.WEEK) {
            val (ws, we) = getWeekRange()
            formatWeekLabel(ws, we)
        } else ""

        val (ws, we) = if (params.range == TimeRange.WEEK) getWeekRange() else 0L to 0L

        combine(
            combine(
                repository.getTotalByType(TransactionType.INCOME, start, end),
                repository.getTotalByType(TransactionType.EXPENSE, start, end),
                repository.getCategorySummary(TransactionType.EXPENSE, start, end),
                repository.getCategorySummary(TransactionType.INCOME, start, end),
                repository.getAllCategories()
            ) { income, expense, expenseSummary, incomeSummary, categories ->
                StatsData(income, expense, expenseSummary, incomeSummary, categories)
            },
            repository.getTransactionsByDateRange(start, end),
            _categoryFilter
        ) { data, transactions, filter ->
            val filteredTx = if (filter == null) transactions
                else transactions.filter { it.categoryId == filter }

            StatsUiState(
                timeRange = params.range,
                year = if (params.range == TimeRange.YEAR) effectiveYear else params.year,
                month = params.month,
                day = params.day,
                weekStart = ws,
                weekEnd = we,
                weekLabel = weekLabel,
                totalIncome = data.income,
                totalExpense = data.expense,
                expenseCategories = buildCategoryStats(data.expenseSummary, data.categories, data.expense),
                incomeCategories = buildCategoryStats(data.incomeSummary, data.categories, data.income),
                transactions = filteredTx,
                categories = data.categories,
                categoryFilter = filter
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUiState())

    // ═══════════════════════════════════════════════════════
    // 导航方法
    // ═══════════════════════════════════════════════════════

    fun setTimeRange(range: TimeRange) {
        _timeRange.value = range
        when (range) {
            TimeRange.WEEK -> _weekOffset.value = 0
            TimeRange.YEAR -> _yearOffset.value = 0
            else -> {}
        }
    }

    fun previousDay() {
        val date = LocalDate(_year.value, _month.value, _day.value)
            .minus(1, DateTimeUnit.DAY)
        _year.value = date.year
        _month.value = date.monthNumber
        _day.value = date.dayOfMonth
    }

    fun nextDay() {
        val date = LocalDate(_year.value, _month.value, _day.value)
            .plus(1, DateTimeUnit.DAY)
        _year.value = date.year
        _month.value = date.monthNumber
        _day.value = date.dayOfMonth
    }

    fun previousWeek() { _weekOffset.value -= 1 }
    fun nextWeek() { _weekOffset.value += 1 }

    fun previousMonth() {
        if (_month.value == 1) { _month.value = 12; _year.value -= 1 }
        else { _month.value -= 1 }
    }

    fun nextMonth() {
        if (_month.value == 12) { _month.value = 1; _year.value += 1 }
        else { _month.value += 1 }
    }

    fun previousYear() { _yearOffset.value -= 1 }
    fun nextYear() { _yearOffset.value += 1 }

    fun setCategoryFilter(categoryId: Long?) { _categoryFilter.value = categoryId }

    fun setCustomWeekStart(timestamp: Long) {
        _customWeekAnchor.value = timestamp
        val nowMs = Clock.System.now().toEpochMilliseconds()
        val diffDays = (nowMs - timestamp) / (24 * 60 * 60 * 1000L)
        _weekOffset.value = (diffDays / 7).toInt()
        _timeRange.value = TimeRange.WEEK
    }

    fun selectDate(timestamp: Long) {
        val target = Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(tz)
        val targetDate = target.date

        when (_timeRange.value) {
            TimeRange.DAY -> {
                _year.value = targetDate.year
                _month.value = targetDate.monthNumber
                _day.value = targetDate.dayOfMonth
            }
            TimeRange.WEEK -> {
                val today = Clock.System.now().toLocalDateTime(tz).date
                val nowMonday = mondayOf(today)
                val targetMonday = mondayOf(targetDate)
                val diffDays = targetMonday.toEpochDays() - nowMonday.toEpochDays()  // Placeholder — will compute below
                // Use simple approach: date difference in days
                val nowEpochDays = nowMonday.atStartOfDayIn(tz).toEpochMilliseconds() / (24 * 60 * 60 * 1000L)
                val targetEpochDays = targetMonday.atStartOfDayIn(tz).toEpochMilliseconds() / (24 * 60 * 60 * 1000L)
                val diffWeeks = ((targetEpochDays - nowEpochDays) / 7).toInt()
                _weekOffset.value = diffWeeks
            }
            TimeRange.MONTH -> {
                _year.value = targetDate.year
                _month.value = targetDate.monthNumber
            }
            TimeRange.YEAR -> {
                _yearOffset.value = targetDate.year - now.year
            }
        }
    }

    init {
        _year.value = now.year
        _month.value = now.monthNumber
        _day.value = now.dayOfMonth
    }

    private fun buildCategoryStats(
        summary: List<CategorySummary>,
        categories: List<Category>,
        totalAmount: Double
    ): List<CategoryStat> {
        if (totalAmount <= 0) return emptyList()
        return summary.mapNotNull { s ->
            val cat = categories.find { it.id == s.categoryId } ?: return@mapNotNull null
            CategoryStat(category = cat, total = s.total, percentage = (s.total / totalAmount).toFloat())
        }.sortedByDescending { it.total }
    }
}
