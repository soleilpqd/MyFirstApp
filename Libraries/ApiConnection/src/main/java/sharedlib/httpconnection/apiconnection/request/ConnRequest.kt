package sharedlib.httpconnection.apiconnection

import java.io.File
import kotlin.text.StringBuilder

/** Data class to hold HTTP request data */
data class ConnRequest(
    /** Request URL */
    val url: ConnUrl,
    /** Request method */
    val method: ConnMethod? = defaultMethod,
    /** Request header */
    val header: Map<ConnRequestHeaderField, List<String>>? = defaultHeader,
    /** Body */
    val body: ByteArray? = null,
    /** Path to file as body */
    val bodyFile: File? = null,
    /** Custom data */
    val userInfo: Map<String, Any>? = null,
    /** For log purpose */
    val description: String? = null

) {

    companion object {
        var defaultMethod: ConnMethod? = null
        var defaultHeader: Map<ConnRequestHeaderField, List<String>>? = null
    }

    override fun toString(): String {
        val result = StringBuilder("HTTP REQUEST:\n")
        result.append(" - URL: ${url.toString()}\n")
        if (method != null) {
            result.append(" - METHOD: ${method.rawValue}\n")
        }
        if (header != null && header.isNotEmpty()) {
            result.append(" - HEADER:\n")
            header.forEach { (key, value) ->
                value.forEach {
                    result.append("    + \"${key.rawValue}\" = \"${it}\"\n")
                }
            }
        }
        if (body != null) {
            result.append(" - BODY BYTES: ${body.size}\n")
            try {
                val str = String(bytes = body)
                if (!str.contains("�")) {
                    result.append("\"${str}\"\n")
                }
            } catch (error: Exception) {

            }

        }
        if (bodyFile != null) {
            result.append(" - BODY FILE: ${bodyFile.absolutePath} (${bodyFile.length()})\n")
        }
        if (description != null) {
            result.append(description)
        }
        return result.toString()
    }

}
