package sharedlib.httpconnection.apiconnection

import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

interface ConnTaskRequestBuilder {
    /** Complete the request data */
    suspend fun fillRequest(request: ConnRequest): ConnRequest
    /** Clean when request finishes, eg. delete temp file */
    fun clean()
}

interface ConnTaskConnector {
    /** Perform request with data in `request`, return result data */
    suspend fun perform(sender: ConnTask, request: ConnRequest): ConnResponse
    /** Disconnect */
    fun stop()
}

interface ConnTaskResponseHandler {
    /** Request finished; parse data and notify receiver */
    suspend fun handleResponse(response: ConnResponse)
}

/** Task */
class ConnTask(
    /** The object manages the Coroutine scope */
    val session: ConnSession = ConnSession.default,
    /** Request data. It requires at least the URL */
    val request: ConnRequest,
    /** The object fills the request data (above) with app data */
    val requestBuilder: ConnTaskRequestBuilder? = null,
    /** The object makes the connection */
    val connector: ConnTaskConnector = ConnHttpUrlConnector(),
    /** The object to process response data and return result */
    val responseHandler: ConnTaskResponseHandler?
) {

    companion object {
        var defaultLogAction: ((String) -> Unit)? = null
        var defaultLogContext: CoroutineContext? = null
    }

    internal var job: Job? = null

    var logAction: ((String) -> Unit)? = ConnTask.defaultLogAction
    var logContext: CoroutineContext? = ConnTask.defaultLogContext

    override fun equals(other: Any?): Boolean {
        if (other is ConnTask) {
            return this === other
        }
        return false
    }

    fun start() {
        session.start(this)
    }

    fun stop() {
        session.stop(this)
        connector.stop()
    }

    internal suspend fun sendLog(content: () -> String) {
        if (logAction == null) { return }
        if (logContext != null) {
            withContext(logContext!!) {
                logAction!!(content())
            }
        } else {
            logAction!!(content())
        }
    }

    internal suspend fun perform() {
        var rqData = request
        val additionalHeader = session.additionalHeaders?.toMutableMap()
        if (additionalHeader != null && additionalHeader.isNotEmpty()) {
            val header = request.header
            if (header != null && header.isNotEmpty()) {
                header.forEach { key, value ->
                    val mutableValue = value.toMutableList()
                    val additionalValue = additionalHeader[key]
                    if (additionalValue != null) {
                        mutableValue.addAll(additionalValue)
                    }
                    additionalHeader[key] = mutableValue.toList()
                }
            }
            rqData = ConnRequest(
                url = rqData.url,
                header = additionalHeader,
                method = rqData.method,
                body = rqData.body,
                bodyFile = rqData.bodyFile,
                userInfo = rqData.userInfo,
                description = rqData.description
            )
        }
        if (requestBuilder != null) {
            rqData = requestBuilder.fillRequest(rqData)
        }
        sendLog { rqData.toString() }
        val response = connector.perform(sender = this, request = rqData)
        sendLog { response.toString() }
        requestBuilder?.clean()
        responseHandler?.handleResponse(response = response)
    }

}
