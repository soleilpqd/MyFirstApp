package sharedlib.httpconnection.apiconnection

import com.google.gson.Gson
import java.nio.charset.Charset

/** Helper to make request with POST method and JSON body data */
class ConnJsonBodyRequestBuilder(
    val params: Any,
    val encoder: Gson = defaultJsonEncoder,
    val charset: Charset = defaultCharset
): ConnTaskRequestBuilder {

    companion object {
        var defaultJsonEncoder: Gson = Gson()
        var defaultCharset: Charset = Charsets.UTF_8
    }

    override suspend fun fillRequest(request: ConnRequest): ConnRequest {
        val headers = request.header?.toMutableMap() ?: mutableMapOf<ConnRequestHeaderField, List<String>>()
        val json = encoder.toJson(params)
        val data = json.toByteArray(charset = charset)
        val contentType = ConnContentType(
            mainType = ConnContentType.MainType.application,
            supType = ConnContentType.ApplicationSubType.json,
            parameters = mapOf(ConnContentType.TextSubType.paramCharset to charset.name())
        )
        if (!headers.containsKey(ConnRequestHeaderField.contentType)) {
            headers[ConnRequestHeaderField.contentType] = listOf(contentType.body)
        }
        if (!headers.containsKey(ConnRequestHeaderField.contentLength)) {
            headers[ConnRequestHeaderField.contentLength] = listOf("${data.size}")
        }
        return ConnRequest(
            url = request.url,
            method = request.method ?: ConnMethod.post,
            header = headers,
            body = data,
            userInfo = request.userInfo
        )
    }

    override fun clean() { }

}