package com.example.accountingapp

import androidx.compose.ui.window.ComposeUIViewController
import com.example.accountingapp.data.db.AppDatabase
import com.example.accountingapp.data.model.Category
import com.example.accountingapp.data.model.TransactionType
import com.example.accountingapp.data.repository.AppRepository
import com.example.accountingapp.platform.createAppDatabase
import com.example.accountingapp.ui.navigation.AppNavGraph
import com.example.accountingapp.ui.theme.AccountingTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// iOS 端全局单例（替代 Android 的 AccountingApp Application 类）
private val appDatabase by lazy { createAppDatabase() }
val appRepository by lazy { AppRepository(appDatabase) }

fun MainViewController() = ComposeUIViewController {
    // 首次启动时播种默认分类
    seedDefaultCategories()

    AccountingTheme {
        AppNavGraph(repository = appRepository)
    }
}

private var categoriesSeeded = false

private fun seedDefaultCategories() {
    if (categoriesSeeded) return
    categoriesSeeded = true

    CoroutineScope(Dispatchers.Main).launch {
        val existing = appDatabase.categoryDao().getAllOnce()
        if (existing.isNotEmpty()) return@launch

        defaultIncomeCategories.forEach { appDatabase.categoryDao().insert(it) }
        defaultExpenseCategories.forEach { appDatabase.categoryDao().insert(it) }
    }
}

private val defaultIncomeCategories = listOf(
    Category(name = "工资", emoji = "💰", type = TransactionType.INCOME),
    Category(name = "奖金", emoji = "🎁", type = TransactionType.INCOME),
    Category(name = "兼职", emoji = "💼", type = TransactionType.INCOME),
    Category(name = "投资", emoji = "📈", type = TransactionType.INCOME),
    Category(name = "其他", emoji = "📦", type = TransactionType.INCOME)
)

private val defaultExpenseCategories = listOf(
    Category(name = "餐饮", emoji = "🍜", type = TransactionType.EXPENSE),
    Category(name = "交通", emoji = "🚗", type = TransactionType.EXPENSE),
    Category(name = "购物", emoji = "🛍️", type = TransactionType.EXPENSE),
    Category(name = "住房", emoji = "🏠", type = TransactionType.EXPENSE),
    Category(name = "娱乐", emoji = "🎮", type = TransactionType.EXPENSE),
    Category(name = "医疗", emoji = "💊", type = TransactionType.EXPENSE),
    Category(name = "教育", emoji = "📚", type = TransactionType.EXPENSE),
    Category(name = "其他", emoji = "💸", type = TransactionType.EXPENSE)
)
