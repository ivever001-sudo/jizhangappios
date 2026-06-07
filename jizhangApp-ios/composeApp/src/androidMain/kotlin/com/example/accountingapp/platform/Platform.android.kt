package com.example.accountingapp.platform

import android.app.Activity
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.room.Room
import com.example.accountingapp.data.db.AppDatabase
import com.example.accountingapp.data.db.AppDatabase.Companion.MIGRATION_1_2
import com.example.accountingapp.data.db.AppDatabase.Companion.MIGRATION_2_3
import com.example.accountingapp.ui.theme.Cream

// ═══════════════════════════════════════════════════════
// 任务 1：Android 沉浸式状态栏
//   原 Theme.kt 中 SideEffect 逻辑完整迁移至此
// ═══════════════════════════════════════════════════════
@Composable
actual fun PlatformStatusBarColors() {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Cream.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }
}

// ═══════════════════════════════════════════════════════
// 任务 2：Android Toast
// ═══════════════════════════════════════════════════════
actual fun showPlatformToast(message: String) {
    Toast.makeText(AppContextHolder.context, message, Toast.LENGTH_SHORT).show()
}

// ═══════════════════════════════════════════════════════
// 任务 4：Android Room Database 构建
//   包含完整的 Migration 链 + 单例缓存
// ═══════════════════════════════════════════════════════
@Volatile
private var androidDbInstance: AppDatabase? = null

actual fun createAppDatabase(): AppDatabase {
    return androidDbInstance ?: synchronized(this) {
        androidDbInstance ?: Room.databaseBuilder(
            AppContextHolder.context,
            AppDatabase::class.java,
            "accounting_db"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .fallbackToDestructiveMigration()
            .build()
            .also { androidDbInstance = it }
    }
}

// ═══════════════════════════════════════════════════════
// 附加：Android SharedPreferences 实现
// ═══════════════════════════════════════════════════════
actual class PlatformSettings(private val name: String) {
    private val prefs by lazy {
        AppContextHolder.context.getSharedPreferences(name, 0)
    }

    actual fun getString(key: String, default: String): String {
        return prefs.getString(key, default) ?: default
    }

    actual fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
}

actual fun createPlatformSettings(name: String): PlatformSettings {
    return PlatformSettings(name)
}
