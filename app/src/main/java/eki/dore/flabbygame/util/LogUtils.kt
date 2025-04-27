package eki.dore.flabbygame.util

import android.util.Log

object LogUtils {
    fun d(message: String?) {
        val stackTraceElement = Throwable().stackTrace[1]
        if (eki.dore.flabbygame.BuildConfig.DEBUG) {
            Log.d(
                "class " + stackTraceElement.fileName + " in " + stackTraceElement.methodName
                        + " at line " + stackTraceElement.lineNumber, (message)!!
            )
        }
    }

    fun e(message: String?) {
        val stackTraceElement = Throwable().stackTrace[1]
        if (eki.dore.flabbygame.BuildConfig.DEBUG) {
            Log.e(
                "class " + stackTraceElement.fileName + " in " + stackTraceElement.methodName
                        + " at line " + stackTraceElement.lineNumber, (message)!!
            )
        }
    }

    fun i(message: String?) {
        val stackTraceElement = Throwable().stackTrace[1]
        if (eki.dore.flabbygame.BuildConfig.DEBUG) {
            Log.i(
                ("class " + stackTraceElement.fileName + " in " + stackTraceElement.methodName
                        + " at line " + stackTraceElement.lineNumber), (message)!!
            )
        }
    }
}