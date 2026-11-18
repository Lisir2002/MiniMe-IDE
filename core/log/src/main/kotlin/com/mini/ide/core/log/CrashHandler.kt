package com.mini.ide.core.log

import javax.inject.Inject
import javax.inject.Singleton

/**
 * 全局未捕获异常捕获器：崩溃时写一条 FATAL（含堆栈），再委托原 handler，
 * 保持系统默认崩溃行为不变。
 */
@Singleton
class CrashHandler @Inject constructor(private val logger: MiniLogger) {

    private var previous: Thread.UncaughtExceptionHandler? = null

    fun install() {
        if (Thread.getDefaultUncaughtExceptionHandler() is CrashExceptionHandler) return
        previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(
            CrashExceptionHandler(logger, previous)
        )
    }

    private class CrashExceptionHandler(
        private val logger: MiniLogger,
        private val previous: Thread.UncaughtExceptionHandler?,
    ) : Thread.UncaughtExceptionHandler {
        override fun uncaughtException(thread: Thread, throwable: Throwable) {
            runCatching {
                logger.fatal(
                    module = "crash",
                    message = "未捕获异常",
                    throwable = throwable,
                    "thread" to thread.name,
                    "type" to (throwable.javaClass.simpleName ?: "Unknown"),
                )
            }
            previous?.uncaughtException(thread, throwable)
        }
    }
}
