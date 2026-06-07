package com.example.accountingapp.ui.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.accountingapp.data.model.Category
import com.example.accountingapp.data.model.Transaction
import com.example.accountingapp.data.model.TransactionType
import com.example.accountingapp.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

data class AddTransactionUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val selectedCategoryId: Long? = null,
    val categories: List<Category> = emptyList(),
    val note: String = "",
    val date: Long = Clock.System.now().toEpochMilliseconds(),
    val saved: Boolean = false
)

class AddTransactionViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _state.asStateFlow()

    val allCategories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setType(type: TransactionType) {
        _state.update { it.copy(type = type, selectedCategoryId = null) }
    }

    fun setAmount(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _state.update { it.copy(amount = value) }
        }
    }

    fun selectCategory(categoryId: Long) {
        _state.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun setNote(value: String) {
        _state.update { it.copy(note = value) }
    }

    /**
     * 设置日期（不改变时间部分），将时间归零到当天 00:00:00
     */
    fun setDate(timestamp: Long) {
        val tz = TimeZone.currentSystemDefault()
        val targetDate = kotlinx.datetime.Instant
            .fromEpochMilliseconds(timestamp)
            .toLocalDateTime(tz)
            .date
        // 保留原状态中的时分秒
        val currentLdt = kotlinx.datetime.Instant
            .fromEpochMilliseconds(_state.value.date)
            .toLocalDateTime(tz)
        val newInstant = LocalDateTime(targetDate, currentLdt.time)
            .toInstant(tz)
        _state.update { it.copy(date = newInstant.toEpochMilliseconds()) }
    }

    /**
     * 设置时分（不改变日期部分）
     */
    fun setTime(hour: Int, minute: Int) {
        val tz = TimeZone.currentSystemDefault()
        val currentDate = kotlinx.datetime.Instant
            .fromEpochMilliseconds(_state.value.date)
            .toLocalDateTime(tz)
            .date
        val newInstant = LocalDateTime(currentDate, LocalTime(hour, minute))
            .toInstant(tz)
        _state.update { it.copy(date = newInstant.toEpochMilliseconds()) }
    }

    fun save() {
        val current = _state.value
        val amount = current.amount.toDoubleOrNull() ?: return
        val categoryId = current.selectedCategoryId ?: return
        if (amount <= 0) return

        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    amount = amount,
                    type = current.type,
                    categoryId = categoryId,
                    note = current.note,
                    date = current.date
                )
            )
            _state.update { it.copy(saved = true) }
        }
    }
}
