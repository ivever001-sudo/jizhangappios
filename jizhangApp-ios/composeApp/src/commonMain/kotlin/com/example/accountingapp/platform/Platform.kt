package com.example.accountingapp.platform

import androidx.compose.runtime.Composable
import com.example.accountingapp.data.db.AppDatabase

// ═══════════════════════════════════════════════════════
// 任务 1：平台状态栏颜色控制
//   Android → 通过 WindowCompat 设置 statusBarColor + 图标深浅
//   iOS     → 空实现（iOS 原生沉浸式透明，SafeArea 由 CMP 自动处理）
// ═══════════════════════════════════════════════════════
@Composable
expect fun PlatformStatusBarColors()

// ═══════════════════════════════════════════════════════
// 任务 2：平台 Toast / 提示
//   Android → Toast.makeText()
//   iOS     → UIAlertController 弹窗
// ═══════════════════════════════════════════════════════
expect fun showPlatformToast(message: String)

// ═══════════════════════════════════════════════════════
// 任务 4：Room Database 平台化构建
//   Android → Room.databaseBuilder(context, ...)
//   iOS     → Room.databaseBuilder(name, factory, driver)
// ═══════════════════════════════════════════════════════
expect fun createAppDatabase(): AppDatabase

// ═══════════════════════════════════════════════════════
// 附加：简单键值存储（替代 SharedPreferences）
//   仅用于 HomeViewModel 中 balance_type 的持久化
//   Android → SharedPreferences
//   iOS     → NSUserDefaults
// ═══════════════════════════════════════════════════════
expect class PlatformSettings {
    fun getString(key: String, default: String): String
    fun putString(key: String, value: String)
}

expect fun createPlatformSettings(name: String): PlatformSettings
