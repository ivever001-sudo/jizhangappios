package com.example.accountingapp.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.accountingapp.data.repository.AppRepository
import com.example.accountingapp.ui.components.BottomNavBar
import com.example.accountingapp.ui.screens.add.AddTransactionScreen
import com.example.accountingapp.ui.screens.add.AddTransactionViewModel
import com.example.accountingapp.ui.screens.category.CategoryScreen
import com.example.accountingapp.ui.screens.category.CategoryViewModel
import com.example.accountingapp.ui.screens.home.HomeScreen
import com.example.accountingapp.ui.screens.home.HomeViewModel
import com.example.accountingapp.ui.screens.stats.StatsScreen
import com.example.accountingapp.ui.screens.stats.StatsViewModel

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Stats : Screen("stats")
    data object Category : Screen("category")
    data object AddTransaction : Screen("add_transaction")
}

/**
 * 应用主导航图。
 *
 * 所有 ViewModel 在此处通过构造函数注入 AppRepository，
 * 彻底消除 AndroidViewModel(Application) 的平台依赖。
 */
@Composable
fun AppNavGraph(repository: AppRepository) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Stats.route,
        Screen.Category.route
    )

    // ViewModel 实例 — 通过 remember(repository) 保持生命周期内复用
    val homeViewModel = remember(repository) { HomeViewModel(repository) }
    val statsViewModel = remember(repository) { StatsViewModel(repository) }
    val categoryViewModel = remember(repository) { CategoryViewModel(repository) }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route
        ) {
            composable(Screen.Home.route) {
                Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()).fillMaxSize()) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onAddClick = { navController.navigate(Screen.AddTransaction.route) }
                    )
                }
            }
            composable(Screen.Stats.route) {
                Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()).fillMaxSize()) {
                    StatsScreen(viewModel = statsViewModel)
                }
            }
            composable(Screen.Category.route) {
                Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()).fillMaxSize()) {
                    CategoryScreen(viewModel = categoryViewModel)
                }
            }
            composable(Screen.AddTransaction.route) {
                val addVM = remember(repository) { AddTransactionViewModel(repository) }
                AddTransactionScreen(
                    viewModel = addVM,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
