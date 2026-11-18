package com.mini.ide

import android.app.Application
import com.mini.ide.core.log.CrashHandler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MiniMeApp : Application() {

    @Inject
    lateinit var crashHandler: CrashHandler

    override fun onCreate() {
        super.onCreate()
        // 全局未捕获异常捕获：崩溃时写 FATAL 日志（含堆栈）后委托系统默认处理。
        // 文件 writer 此时可能尚未由 MainActivity 装配，日志至少进 logcat；
        // attach 完成后的崩溃即落盘到 MINIIDE/logs。
        crashHandler.install()
    }
}
