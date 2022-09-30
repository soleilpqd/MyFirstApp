package sharedlib.httpconnection.apiconnection

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.charset.Charset
import kotlin.coroutines.CoroutineContext

/** Response handler converts `response.body` (or `response.bodyFile`) to specified type object */
class ConnJsonResponseHandler<SuccessType, FailureType>(
    val successType: Class<SuccessType>,
    val failureType: Class<FailureType>? = null,
    val decoder: Gson = defaultJsonDecoder,
    val receiverContext: CoroutineContext = Dispatchers.Main,
    val completion: (ConnJsonResponseHandler<SuccessType, FailureType>) -> Unit
): ConnTaskResponseHandler  {

    companion object {
         var defaultJsonDecoder: Gson = Gson()
    }

    private val _isSuccess: Boolean?
        get() {
            if (_response != null) {
                if (_response!!.exception != null) {
                    return  false
                }
                if (_response!!.status != null) {
                    return _response!!.status!!.rawValue in 200..299
                }
                return false
            }
            return null
        }
    /** `true` if response status code is between 200..299 & jsonError == null */
    val isSuccess: Boolean
        get() = (_isSuccess ?: false) && jsonError == null
    private var _response: ConnResponse? = null
    val response: ConnResponse?
        get() = _response
    private var _successObj: SuccessType? = null
    val successObject: SuccessType?
        get() = _successObj
    private var _failureObj: FailureType? = null
    val failureObject: FailureType?
        get() = _failureObj
    private var _jsonError: Exception? = null
    val jsonError: Exception?
        get() = _jsonError

    private fun findCharset(): Charset {
        val resp = _response!!
        val contentTypeLine = resp.header?.get(ConnResponseHeaderField.contentType)?.first()
        if (contentTypeLine != null) {

        }
        return Charsets.UTF_8
    }

    private fun getJsonString(): String {
        val charset = findCharset()
        val resp = _response!!
        var buffer = resp.body
        if (buffer == null && resp.bodyFile != null) {
            buffer = ByteArray(0)
            var temp = ByteArray(0)
            var stop = false
            val inputStream = resp.bodyFile.inputStream()
            while (!stop) {
                temp = inputStream.readBytes()
                if (temp.isNotEmpty()) {
                    buffer += temp
                } else {
                    stop = true
                }
            }
            inputStream.close()
        }
        if (buffer != null) {
            return buffer.toString(charset = charset)
        }
        return ""
    }

    override suspend fun handleResponse(response: ConnResponse) {
        _response = response
        val json = getJsonString()
        _jsonError = null
        _successObj = null
        _failureObj = null
        try {
            if (_isSuccess == true) {
                _successObj = decoder.fromJson(json, successType)
            } else if (failureType != null) {
                _failureObj = decoder.fromJson(json, failureType)
            }
        } catch (error: Exception) {
            _jsonError = error
        }
        val mySelf = this
        withContext(receiverContext) {
            completion(mySelf)
        }
    }

}