package com.example.accountingapp.platform

import android.content.Context

/**
 * Android 全局 Context 持有者。
 * 由 [com.example.accountingapp.AccountingApp.onCreate] 初始化。
 *
 * 用途：供 actual 层（Toast、Room Builder）获取 Application Context，
 * 避免在 ViewModel 层直接依赖 android.app.Application。
 */
object AppContextHolder {
    lateinit var context: Context
        private set

    fun init(appContext: Context) {
        context = appContext.applicationContext
    }
}
