package sharedlib.httpconnection.apiconnection

import java.lang.Exception
import java.io.File
import java.net.URL

/** Data class to hold HTTP response data */
data class ConnResponse(
    /** Response status code */
    val status: ConnStatusCode? = null,
    /** Response header */
    val header: Map<ConnResponseHeaderField, List<String>>? = null,
    /** Response body binary */
    val body: ByteArray? = null,
    /** Resposne body file path */
    val bodyFile: File? = null,

    val originUrl: URL? = null,
    val finalUrl: URL? = null,
    /** Error */
    val exception: Exception? = null,
    /** Custom data */
    val userInfo: Map<String, Any>? = null

) {

    override fun toString(): String {
        val result = StringBuilder("HTTP RESPONSE:\n")
        if (originUrl != null) {
            result.append(" - URL (origin): ${originUrl.toString()}\n")
        }
        if (finalUrl != null) {
            result.append(" - URL (final): ${finalUrl.toString()}\n")
        }
        if (status != null) {
            result.append(" - STATUS: ${status.rawValue}\n")
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
            val str = String(bytes = body)
            if (!str.contains("�")) {
                result.append("\"${str}\"\n")
            }
        }
        if (bodyFile != null) {
            result.append(" - BODY FILE: ${bodyFile.absolutePath} (${bodyFile.length()})\n")
        }
        if (exception != null) {
            result.append(" - ERROR: ${exception.toString()}\n")
        }
        return result.toString()
    }

}
