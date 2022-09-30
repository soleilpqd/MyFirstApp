package sharedlib.httpconnection.apiconnection

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.*
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties

/** Helper to make request with format `multipart/form-data` */
class ConnMultipartFormDataRequestBuilder(
    /**
     * Request parameters. It must one of below options:
     * - `Map<String, Any>`: with `Any` is one of below types.
     * - `List<ConnMultipartFormDataSection>`: pass list of `ConnMultipartFormDataSection` if you want make your own multipart section.
     * - Custom class which its properties are marked with `@ConnField` and have type in below list.
     *
     * Field type should be:
     * - File
     * - ByteArray
     * - InputStream
     * - ConnMultipartFormDataSection
     * - Other type: `toString()`
     */
    val params: Any?,
    /** Write to this file (if `output` is directory, file will be auto-generated). Recommend with big size data. */
    val output: File? = defaultOutputDir,
    /** Delete the `output` when request finishes */
    val autoDeleteOutput: Boolean = defaultAutoDeleteOutput,
    /** Multipart form-data boundary. Leave `null` to auto-generate */
    val boundary: String? = null,
    /** Chatset. Default UTF-8. */
    val charset: Charset = defaultCharset,
    /** Make log data */
    val enableLog: Boolean = true
): ConnTaskRequestBuilder {

    companion object {
        public var defaultAutoDeleteOutput: Boolean = true
        public var defaultOutputDir: File? = null
        public var defaultCharset: Charset = Charsets.UTF_8
    }

    private var boundaryStr = ""
    private var resolvedOutput: File? = null

    private fun resolveOutput() {
        resolvedOutput = null
        if (output != null) {
            if (output.isDirectory) {
                var idx = 0
                var temp = File(output, "${idx}")
                while (temp.exists()) {
                    idx += 1
                    temp = File(output, "${idx}")
                }
                resolvedOutput = temp
            } else {
                resolvedOutput = output
            }
        }
    }

    private fun makeSection(name: String, data: Any?, fileName: String? = null): ConnMultipartFormDataSection {
        var fName = fileName
        if ((fName == null || fName.isEmpty()) && data is File) {
            fName = data.name
        }
        val contentDisposition = ConnContentDisposition.formData(
            name = name,
            fileName = fName,
            charset = charset
        )
        var contentType: ConnContentType? = when (data) {
            is ByteArray, is InputStream -> null
            is File -> data.httpContentType
            is ConnMultipartFormDataSection -> data.contentType
            else -> ConnContentType(
                mainType = ConnContentType.MainType.text,
                supType = ConnContentType.TextSubType.plain,
                parameters = mapOf(ConnContentType.TextSubType.paramCharset to charset.name())
            )
        }
        if (contentType == null) {
            if (fName != null) {
                val ffile = File(fName)
                contentType = ffile.httpContentType ?: ConnContentType.applicationOctetStream
            } else {
                contentType = ConnContentType.applicationOctetStream
            }
        }
        return  ConnMultipartFormDataSection(
            contentDisposition = contentDisposition,
            contentType = contentType,
            data = data,
            charset = charset
        )
    }

    private fun buildSection(map: Map<String, Any>): List<ConnMultipartFormDataSection> {
        val result = mutableListOf<ConnMultipartFormDataSection>()
        map.forEach { key, value ->
            result.add(makeSection(name = key, data = value))
        }
        return result
    }

    private fun buildSection(`object`: Any): List<ConnMultipartFormDataSection> {
        val result = mutableListOf<ConnMultipartFormDataSection>()
        val cls = `object`::class
        for (prop in cls.memberProperties) {
            if (prop.hasAnnotation<ConnField>()) {
                val annotation = prop.findAnnotation<ConnField>()!!
                val propValue = (prop as? KProperty1<Any, *>)?.get(`object`)
                var propName = annotation.name
                if (propName.isEmpty()) {
                    propName = prop.name
                }
                result.add(
                    makeSection(
                        name = propName,
                        data = propValue,
                        fileName = annotation.fileName
                    )
                )
            }
        }
        return result
    }

    override suspend fun fillRequest(request: ConnRequest): ConnRequest {
        val logBuilder: StringBuilder? = if (enableLog) { StringBuilder() } else { null }
        resolveOutput()
        val outputStream: OutputStream = resolvedOutput?.outputStream() ?: ByteArrayOutputStream()
        boundaryStr = if (boundary != null && boundary.isNotEmpty()) {
            boundary
        } else {
            "xxx${ConnDateTimeFormat(rawValue = "yyyyMMddHHmmss").format(Date())}xxx"
        }
        val contentType = ConnContentType.multipartFormData(boundaryStr)
        val sections = when (params) {
            null -> listOf<ConnMultipartFormDataSection>()
            is Map<*, *> -> {
                buildSection(map = params as Map<String, Any>)
            }
            is List<*> -> {
                params as List<ConnMultipartFormDataSection>
            }
            else -> {
                buildSection(`object` = params)
            }
        }
        boundaryStr = "--${boundaryStr}"
        val boundaryData = boundaryStr.toByteArray(charset = charset)
        val crlf = String.CRLF.toByteArray(charset = charset)
        sections.forEach { section ->
            outputStream.write(boundaryData)
            outputStream.write(crlf)
            logBuilder?.append("${boundaryStr}\n")
            val log = section.write(outputStream)
            logBuilder?.append(log)
            outputStream.write(crlf)
            logBuilder?.append("\n")
        }
        outputStream.write(boundaryData)
        outputStream.write("--".toByteArray(charset = charset))
        logBuilder?.append("${boundaryStr}--")
        outputStream.flush()
        outputStream.close()

        val outputBytes: ByteArray? = if (outputStream is ByteArrayOutputStream) {
            outputStream.toByteArray()
        } else {
            null
        }

        val headers = request.header?.toMutableMap() ?: mutableMapOf<ConnRequestHeaderField, List<String>>()
        headers[ConnRequestHeaderField(rawValue = contentType.name)] = listOf(contentType.body)
        val size = if (outputBytes != null) {
            outputBytes.size
        } else {
            resolvedOutput?.length() ?: 0
        }
        headers[ConnRequestHeaderField.contentLength] = listOf("${size}")

        if (outputStream is ByteArrayOutputStream) {
            return ConnRequest(
                url = request.url,
                method = request.method ?: ConnMethod.post,
                header = headers,
                body = outputBytes,
                userInfo = request.userInfo,
                description = logBuilder?.toString()
            )
        }
        return ConnRequest(
            url = request.url,
            method = request.method ?: ConnMethod.post,
            header = headers,
            bodyFile = resolvedOutput,
            userInfo = request.userInfo,
            description = logBuilder?.toString()
        )
    }

    override fun clean() {
        if (autoDeleteOutput && resolvedOutput != null) {
            try {
                resolvedOutput?.delete()
            } catch (error: Exception) {

            }
        }
    }

}
