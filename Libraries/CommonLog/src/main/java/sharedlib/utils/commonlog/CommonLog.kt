package sharedlib.utils.commonlog

import android.os.Looper
import android.util.Log

/** Log level */
public enum class CMLogLevel {
    LogVerbose, LogDebug, LogInfo, LogWarning, LogError, LogAssert
}

/** Enable log */
public var CMLogEnable = true

/** `Log` from `android.util.Log` with more info */
public fun CMLog(vararg contents: Any?, tag: String = "", level: CMLogLevel = CMLogLevel.LogInfo) {
    if (!CMLogEnable) { return }
    var tagg = if (tag.isNotEmpty()) tag else "LOG"
    var text = ""
    val stacks = Exception().stackTrace
    if (stacks.count() > 2) {
        val stack = stacks[2]
        text = "FILE=${stack.fileName}[${stack.lineNumber}]; FUNC=${stack.methodName}; "
        val currentThread = Thread.currentThread()
        var isMain = "main"
        if (currentThread != Looper.getMainLooper().thread) isMain = "non-main"
        text += "THREAD=[${currentThread.id} ${currentThread.name} ${isMain}]:\n"
        if (tag.isEmpty()) {
            tagg = stack.className
        }
    } else {
        text = ">\n"
    }
    for (item in contents) {
        text += "${item}\n"
    }
    when (level) {
        CMLogLevel.LogVerbose -> Log.v(tagg, text)
        CMLogLevel.LogDebug -> Log.d(tagg, text)
        CMLogLevel.LogInfo -> Log.i(tagg, text)
        CMLogLevel.LogWarning -> Log.w(tagg, text)
        CMLogLevel.LogError -> Log.e(tagg, text)
        CMLogLevel.LogAssert -> Log.wtf(tagg, text)
    }
}
