package sharedlib.httpconnection.apiconnection

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/** Basic response handler */
class ConnBasicResponseHandler(
    val receiverContext: CoroutineContext = Dispatchers.Main,
    val completion: (ConnBasicResponseHandler) -> Unit
): ConnTaskResponseHandler {

    /** `true` if response status code is between 200..299 */
    val isSuccess: Boolean?
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
    private var _response: ConnResponse? = null
    /** Response data */
    val response: ConnResponse?
        get() = _response

    override suspend fun handleResponse(response: ConnResponse) {
        _response = response
        val mySelf = this
        withContext(receiverContext) {
            completion(mySelf)
        }
    }

}