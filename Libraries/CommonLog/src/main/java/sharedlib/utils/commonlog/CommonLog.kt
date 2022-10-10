package sharedlib.utils.commonlog

import android.os.Looper
import android.util.Log
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

/** Log level */
enum class CMLogLevel {
    LogVerbose, LogDebug, LogInfo, LogWarning, LogError, LogAssert
}

/** Enable log */
var CMLogEnable = true

/** `Log` from `android.util.Log` with more info */
fun CMLog(vararg contents: Any?, tag: String = "", level: CMLogLevel = CMLogLevel.LogInfo) {
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

private fun cmMakePrefix(level: Int, indent: String): String {
    val prefixBuilder = StringBuilder("┊")
    for (idx in 0..level) {
        prefixBuilder.append(indent)
    }
    return prefixBuilder.toString()
}

private fun cmLogGetIteratorDebugDescription(target: Iterator<*>, level: Int, limit: Int, ignorePrivate: Boolean, indent: String): Any {
    val prefix = cmMakePrefix(level = level - 1, indent = indent)
    val prefix1 = prefix + indent
    val result = mutableListOf<String>()
    while (target.hasNext()) {
        val item = target.next()
        if (item == null) {
            result.add("${prefix1}NULL,")
        } else {
            val desc = cmLogGetDebugDescription(
                target = item,
                level = level,
                limit = limit,
                ignorePrivate = ignorePrivate,
                indent = indent
            )
            if (desc is String) {
                result.add("${prefix1}${desc},")
            } else {
                val list = desc as List<String>
                result.addAll(list)
            }
        }
    }
    if (result.isNotEmpty()) {
        result.add(index = 0, element = "${prefix}[")
        result.add("${prefix}]")
        return result.toList()
    }
    return "<0> []"
}

private fun cmLogGetMapDebugDescription(target: Map<*, *>, level: Int, limit: Int, ignorePrivate: Boolean, indent: String): Any {
    if (target.isNotEmpty()) {
        val prefix = cmMakePrefix(level = level - 1, indent = indent)
        val prefix1 = prefix + indent
        val result = mutableListOf<String>()
        result.add("${prefix}{")
        target.forEach { (key, value) ->
            val keyDesc = cmLogGetDebugDescription(
                target = key,
                level = level,
                limit = level,
                ignorePrivate = ignorePrivate,
                indent = indent
            )
            val valueDesc = cmLogGetDebugDescription(
                target = value,
                level = level,
                limit = limit,
                ignorePrivate = ignorePrivate,
                indent = indent
            )
            if (valueDesc is String) {
                result.add("${prefix1}${keyDesc} = ${valueDesc}")
            } else {
                result.add("${prefix1}${keyDesc} =")
                result.addAll(valueDesc as List<String>)
            }
        }
        result.add("${prefix}}")
        return result.toList()
    }
    return "<0> {}"
}

private fun cmLogGetDebugDescription(target: Any?, level: Int, limit: Int, ignorePrivate: Boolean, indent: String): Any {
    if (target == null) {
        return "<NULL>"
    }
    val cls = target::class
    val prefix = cmMakePrefix(level = level, indent = indent)
    if (cls == String::class) {
        return "\"$target\""
    } else if ((cls == Any::class) || (limit in (1..level))) {
        return "`$target`"
    } else if (target is Array<*>) {
        return cmLogGetIteratorDebugDescription(
            target = target.iterator(),
            level = level + 1,
            limit = limit,
            ignorePrivate = ignorePrivate,
            indent = indent
        )
    } else if (target is Iterable<*>) {
        return cmLogGetIteratorDebugDescription(
            target = target.iterator(),
            level = level + 1,
            limit = limit,
            ignorePrivate = ignorePrivate,
            indent = indent
        )
    } else if (target is Map<*, *>) {
        return cmLogGetMapDebugDescription(
            target = target,
            level = level + 1,
            limit = limit,
            ignorePrivate = ignorePrivate,
            indent = indent
        )
    }

    var membersDesc = mutableListOf<String>()
    try {
        val members = cls.memberProperties
        if (members.isNotEmpty()) {
            members.forEachIndexed { index, prop ->
                val curPrefix = if (index == members.count() - 1) "${prefix}└┈" else "${prefix}├┈"
                try {
                    val propValue = (prop as? KProperty1<Any, *>)?.get(target)
                    if (propValue == null) {
                        membersDesc.add("${curPrefix}`${prop.name}` = <NULL>")
                    } else if (propValue is String) {
                        membersDesc.add("${curPrefix}`${prop.name}` = \"${propValue}\"")
                    } else {
                        if (limit <= 0 || level < limit - 1) {
                            val propDesc = cmLogGetDebugDescription(
                                target = propValue,
                                level = level + 1,
                                limit = limit,
                                ignorePrivate = ignorePrivate,
                                indent = indent
                            )
                            if (propDesc is String) {
                                membersDesc.add("${curPrefix}`${prop.name}` = ${propDesc}")
                            } else {
                                val propDescList = propDesc as List<String>
                                membersDesc.add("${curPrefix}`${prop.name}` =")
                                membersDesc.addAll(propDescList)
                            }
                        } else {
                            membersDesc.add("${curPrefix}`${prop.name}` = `${propValue}`")
                        }
                    }
                } catch (error: Exception) {
                    if (!ignorePrivate) {
                        membersDesc.add("${curPrefix}`${prop.name}` = <FORBIDDEN $error>")
                    }
                }
            }
        }
    } catch (error: Exception) {

    }
    if (membersDesc.isNotEmpty()) {
        return membersDesc.toList()
    }

    return "`$target`"
}

/**
 * Get object description
 *
  * - `limit`: limit level; 0 to unlimit
 * - `ignorePrivate`: `false` to show private (unaccessible) fields
 */
fun Any.getDebugDescription(identifier: String? = null, limit: Int = 1, ignorePrivate: Boolean = false, indent: String = "  "): String {
    val desc = cmLogGetDebugDescription(
        target = this,
        level = 0,
        limit = limit,
        ignorePrivate = ignorePrivate,
        indent = indent
    )
    val result = StringBuilder(identifier ?: this::class.simpleName ?: this.javaClass.name)
    if (desc is String) {
        result.append(" = $desc")
    } else {
        result.append("\n")
        val list = desc as List<String>
        result.append(list.joinToString(separator = "\n"))
    }
    return result.toString()
}

val Any.debugDescription: String
    get() = getDebugDescription()
