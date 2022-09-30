package sharedlib.httpconnection.apiconnection

import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

/** Data class for a section of multipart/form-data */
data class ConnMultipartFormDataSection(
    /** Content-Dispositon */
    val contentDisposition: ConnContentDisposition,
    /** Content-Type */
    val contentType: ConnContentType,
    /**
     * Section data:
     * - ByteArray
     * - InputStream
     * - File
     * - Other: `toString()`
     */
    val data: Any?,
    /** Charset, should be same in `contentDisposition` & `contentType` */
    val charset: Charset = Charsets.UTF_8
) {

    companion object {
        fun makeFromFile(
            /** Field name */
            name: String,
            /** File to append into request body */
            data: File,
            /** This section data type */
            type: ConnContentType = ConnContentType.applicationOctetStream,
            /** Charset */
            charset: Charset = Charsets.UTF_8
        ): ConnMultipartFormDataSection {
            val disposition = ConnContentDisposition.formData(
                name = name,
                fileName = data.name,
                charset = charset
            )
            return ConnMultipartFormDataSection(
                contentDisposition = disposition,
                contentType = type,
                data = data,
                charset = charset
            )
        }
    }

    /** Write encoded data input `stream` and return log text */
    fun write(stream: OutputStream): String {
        val logBuilder = StringBuilder()

        val contentDispositionLine = contentDisposition.fullLine
        logBuilder.append("${contentDispositionLine}\n")
        stream.write("${contentDispositionLine}${String.CRLF}".toByteArray(charset = charset))

        val contentTypeLine = contentType.fullLine
        logBuilder.append("${contentTypeLine}\n")
        stream.write("${contentTypeLine}${String.CRLF}".toByteArray(charset = charset))

        logBuilder.append("\n")
        stream.write(String.CRLF.toByteArray(charset = charset))

        when (data) {
            is ByteArray -> {
                logBuilder.append("ByteArray: ${data.size}")
                stream.write(data)
            }
            is InputStream -> {
                val len = copyStream(inputStream = data, outputStream = stream)
                logBuilder.append("InputStream: ${len}")
            }
            is File -> {
                logBuilder.append("File: ${data.absolutePath} (${data.length()})")
                val inputStream = data.inputStream()
                copyStream(inputStream = inputStream, outputStream = stream)
            }
            else -> {
                val value = data.toString()
                logBuilder.append("${value}")
                stream.write(value.toByteArray(charset = charset))
            }
        }
        return logBuilder.toString()
    }

    private fun copyStream(inputStream: InputStream, outputStream: OutputStream): Int {
        var stop = false
        var temp: ByteArray?
        var len = 0
        while (!stop) {
            temp = inputStream.readBytes()
            len += temp.size
            if (temp.isNotEmpty()) {
                outputStream.write(temp)
            } else {
                stop = true
            }
        }
        return len
    }

}
