package sharedlib.httpconnection.apiconnection

import java.nio.charset.Charset

/**
 * Helper to make Form-URL-Encoded request.
 * This fills data into request (not overwrite - it ignores fields exiting in input request):
 *  - `method` `GET`/`POST`
 *  - `header` `Content-Type`, `Content-Length`
 *  - URL `query` for `GET`
 *  - `body` for `POST`. Not support `bodyFile`.
 * */
class ConnFormUrlEncodedRequestBuilder(
    /** `true` to put data into body, `false` to put data as url query */
    val isBody: Boolean,
    /**
     * Object to make request data. It can be (see `ConnUrlEncoder.kt`):
     *  - Map<*, *>: this uses `key.toString()` `value.toString()` to make `key=value` form-url-encoded data.
     *  - Custom class: this uses its properties which have annotation `@ConnField()` (see `ConnField.kt`)
     * */
    val params: Any?,
    /** URL encoder */
    val encoder: ConnUrlEncoder = defaultEncoder

): ConnTaskRequestBuilder {

    companion object {
        var defaultEncoder: ConnUrlEncoder = ConnUrlEncoder()
    }

    override suspend fun fillRequest(request: ConnRequest): ConnRequest {
        val url = request.url.duplicate()
        var method = request.method
        val headers = request.header?.toMutableMap() ?: mutableMapOf<ConnRequestHeaderField, List<String>>()
        var body = request.body
        val contentType = ConnContentType(
            mainType = ConnContentType.MainType.application,
            supType = ConnContentType.ApplicationSubType.xWwwForm,
            parameters = mapOf(ConnContentType.TextSubType.paramCharset to encoder.charset.name())
        )
        if (params != null) {
            if (isBody) {
                if (method == null) {
                    method = ConnMethod.post
                }
                if (body == null) {
                    val data = encoder.encode(params)
                    body = data.toByteArray(charset = encoder.charset)
                    if (!headers.containsKey(ConnRequestHeaderField.contentType)) {
                        headers[ConnRequestHeaderField.contentType] = listOf(contentType.body)
                    }
                    if (!headers.containsKey(ConnRequestHeaderField.contentLength)) {
                        headers[ConnRequestHeaderField.contentLength] = listOf("${body.size}")
                    }
                }
            } else {
                if (method == null) {
                    method = ConnMethod.get
                }
                if (url.query == null) {
                    url.query = encoder.encode(params)
                }
            }
        }
        return ConnRequest(
            url = url,
            method = method,
            header = headers,
            body = body,
            userInfo = request.userInfo
        )
    }

    override fun clean() {}

}

/** Create ConnFormUrlEncodedRequestBuilder with ConnUrlEncoder settings of current session */
fun ConnSession.makeFormUrlEncodedRequestBuilder(
    /** Where to put encoded data (`true` to body, `false` to URL query) */
    isBody: Boolean,
    /** Data to be encoded */
    params: Any?,
    /** Charset to encode */
    charset: Charset? = null,
    /** Encode space to '+' instead of '%20' */
    spaceAsPlus: Boolean? = null,
    /** Hexa charactor code upper/lower case */
    lowerCase: Boolean? = null,
    /** Force encode these characters  */
    unreservedChars: String? = null,
    /** Force encode these characters  */
    unreservedCharCat: CharCategory? = null
): ConnFormUrlEncodedRequestBuilder {
    return ConnFormUrlEncodedRequestBuilder(
        isBody = isBody,
        params = params,
        encoder = this.makeUrlEncoder(
            charset = charset,
            spaceAsPlus = spaceAsPlus,
            lowerCase = lowerCase,
            unreservedChars = unreservedChars,
            unreservedCharCat = unreservedCharCat
        )
    )
}
