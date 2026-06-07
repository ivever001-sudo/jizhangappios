package com.example.accountingapp.ui.screens.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.accountingapp.data.model.Category
import com.example.accountingapp.data.model.TransactionType
import com.example.accountingapp.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryUiState(
    val incomeCategories: List<Category> = emptyList(),
    val expenseCategories: List<Category> = emptyList(),
    val showAddDialog: Boolean = false,
    val addDialogType: TransactionType = TransactionType.EXPENSE
)

/**
 * 重构：AndroidViewModel(application) → ViewModel() + 构造函数注入 AppRepository
 */
class CategoryViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _categories = combine(
        repository.getCategoriesByType(TransactionType.INCOME),
        repository.getCategoriesByType(TransactionType.EXPENSE)
    ) { income, expense -> income to expense }

    private val _showAddDialog = MutableStateFlow(false)
    private val _addDialogType = MutableStateFlow(TransactionType.EXPENSE)

    val uiState: StateFlow<CategoryUiState> = combine(
        _categories, _showAddDialog, _addDialogType
    ) { (income, expense), showDialog, dialogType ->
        CategoryUiState(
            incomeCategories = income,
            expenseCategories = expense,
            showAddDialog = showDialog,
            addDialogType = dialogType
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CategoryUiState())

    fun showAddDialog(type: TransactionType) {
        _addDialogType.value = type
        _showAddDialog.value = true
    }

    fun dismissAddDialog() {
        _showAddDialog.value = false
    }

    fun addCategory(name: String, emoji: String, type: TransactionType) {
        viewModelScope.launch {
            repository.insertCategory(
                Category(name = name, emoji = emoji, type = type)
            )
            _showAddDialog.value = false
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun swapCategories(cat1: Category, cat2: Category) {
        viewModelScope.launch {
            val tmp = cat1.sortOrder
            repository.updateCategory(cat1.copy(sortOrder = cat2.sortOrder))
            repository.updateCategory(cat2.copy(sortOrder = tmp))
        }
    }

    fun updateCategoryOrder(categories: List<Category>) {
        viewModelScope.launch {
            categories.forEachIndexed { index, category ->
                repository.updateCategory(category.copy(sortOrder = index))
            }
        }
    }
}
