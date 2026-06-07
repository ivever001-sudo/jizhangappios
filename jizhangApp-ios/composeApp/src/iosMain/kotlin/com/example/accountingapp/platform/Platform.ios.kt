package com.example.accountingapp.platform

import androidx.compose.runtime.Composable
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.accountingapp.data.db.AppDatabase
import com.example.accountingapp.data.db.AppDatabase.Companion.MIGRATION_1_2
import com.example.accountingapp.data.db.AppDatabase.Companion.MIGRATION_2_3
import platform.Foundation.NSHomeDirectory
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIApplication

// ═══════════════════════════════════════════════════════
// 任务 1：iOS 状态栏 — 空实现
//   iOS 原生就是沉浸式透明状态栏，SafeArea 由 CMP 的
//   statusBarsPadding() / WindowInsets 自动映射为
//   iOS 原生 SafeArea，无需任何手动配置。
// ═══════════════════════════════════════════════════════
@Composable
actual fun PlatformStatusBarColors() {
    // iOS 默认全局沉浸式透明，由系统 SafeArea 接管
    // CMP 底层自动将 WindowInsets.statusBars 映射为 iOS SafeArea
}

// ═══════════════════════════════════════════════════════
// 任务 2：iOS Toast — 使用 UIAlertController
// ═══════════════════════════════════════════════════════
actual fun showPlatformToast(message: String) {
    val alert = UIAlertController.alertControllerWithTitle(
        title = null,
        message = message,
        preferredStyle = UIAlertControllerStyleAlert
    )
    val rootViewController = UIApplication.sharedApplication
        .keyWindow?.rootViewController
    rootViewController?.presentViewController(alert, animated = true, completion = null)

    // 1.2 秒后自动消失
    platform.Foundation.NSTimer.scheduledTimerWithTimeInterval(
        1.2,
        repeats = false
    ) { _ ->
        alert.dismissViewControllerAnimated(true, completion = null)
    }
}

// ═══════════════════════════════════════════════════════
// 任务 4：iOS Room Database 构建
//   使用 NSHomeDirectory 获取沙盒路径 + BundledSQLiteDriver
// ═══════════════════════════════════════════════════════
@Volatile
private var iosDbInstance: AppDatabase? = null

actual fun createAppDatabase(): AppDatabase {
    return iosDbInstance ?: synchronized(this) {
        iosDbInstance ?: run {
            val dbFilePath = NSHomeDirectory() + "/Documents/accounting_db.sqlite"
            Room.databaseBuilder<AppDatabase>(
                name = dbFilePath,
            )
                .setDriver(BundledSQLiteDriver())
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()
                .also { iosDbInstance = it }
        }
    }
}

// ═══════════════════════════════════════════════════════
// 附加：iOS NSUserDefaults 实现
// ═══════════════════════════════════════════════════════
actual class PlatformSettings(private val name: String) {
    private val defaults = platform.Foundation.NSUserDefaults(suiteName = name)

    actual fun getString(key: String, default: String): String {
        return defaults.stringForKey(key) ?: default
    }

    actual fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
        defaults.synchronize()
    }
}

actual fun createPlatformSettings(name: String): PlatformSettings {
    return PlatformSettings(name)
}
