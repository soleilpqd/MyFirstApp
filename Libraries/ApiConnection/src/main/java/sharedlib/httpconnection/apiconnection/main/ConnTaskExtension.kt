package sharedlib.httpconnection.apiconnection

import android.net.Uri
import java.io.File
import java.net.URI

/** Convert URI to ConnUrl */
fun URI.toConnUrl(): ConnUrl {
    val result = ConnUrl(
        scheme = ConnURIScheme(rawValue = this.scheme),
        host = this.host.trim('/')
    )
    if (this.port > 0) {
        result.port = this.port.toUInt()
    }
    result.pathComponents = this.path.trim('/').split("/")
    result.query = this.query
    return result
}


/** Convert URI to ConnUrl */
fun Uri.toConnUrl(): ConnUrl {
    val result = ConnUrl(
        scheme = ConnURIScheme(rawValue = this.scheme ?: ""),
        host = (this.host ?: "").trim('/')
    )
    if (this.port > 0) {
        result.port = this.port.toUInt()
    }
    result.pathComponents = this.pathSegments
    result.query = this.query
    return result
}

/**
 * Shortcut to make ConnTask with:
 * - Method `GET`/`POST`
 * - Encode `params` with `form-url-encode` into URL query part or HTTP body
 * - Default ConnConnector
 */
fun ConnTask.Companion.requestFormUrlEncoded(
    /** Request data. It requires at least URL */
    request: ConnRequest,
    /** Data will be encoded */
    params: Any?,
    /** Encode `params` into body if `true`, or url query if `false` */
    isBody: Boolean = false,
    /** Object handles response data and return result */
    responseHandler: ConnTaskResponseHandler? = null
): ConnTask {
    return ConnTask(
        request = request,
        requestBuilder = ConnFormUrlEncodedRequestBuilder(
            isBody = isBody,
            params = params
        ),
        responseHandler = responseHandler
    )
}

/**
 * Shortcut to make ConnTask with:
 * - Method `GET`/`POST`
 * - Encode `params` with `form-url-encode` into URL query part or HTTP body
 * - ConnConnector is made from session settings.
 */
fun ConnSession.requestFormUrlEncoded(
    /** Request data. It requires at least URL */
    request: ConnRequest,
    /** Data will be encoded */
    params: Any?,
    /** Encode `params` into body if `true`, or url query if `false` */
    isBody: Boolean = false,
    /** Object handles response data and return result */
    responseHandler: ConnTaskResponseHandler? = null
): ConnTask {
    return ConnTask(
        request = request,
        requestBuilder = this.makeFormUrlEncodedRequestBuilder(
            isBody = isBody,
            params = params
        ),
        connector = this.makeHttpConnector(),
        responseHandler = responseHandler
    )
}

/**
 * Shortcut to make ConnTask with:
 * - Method `GET`/`POST`
 * - Encode `params` with `form-url-encode` into URL query part or HTTP body
 * - JSON response handler
 * - Default ConnConnector
 */
fun <SuccessType, FailureType> ConnTask.Companion.requestFormUrlEncodedJson(
    /** Request data. It requires at least URL */
    request: ConnRequest,
    /** Data will be encoded */
    params: Any?,
    /** Encode `params` into body if `true`, or url query if `false` */
    isBody: Boolean = false,
    /** Type to convert JSON success response data into object */
    successType: Class<SuccessType>,
    /** Type to convert JSON failure response data into object */
    failureType: Class<FailureType>? = null,
    /** Action on finish */
    completion: (ConnJsonResponseHandler<SuccessType, FailureType>) -> Unit
): ConnTask {
    return ConnTask(
        request = request,
        requestBuilder = ConnFormUrlEncodedRequestBuilder(
            isBody = isBody,
            params = params
        ),
        responseHandler = ConnJsonResponseHandler(
            successType = successType,
            failureType = failureType,
            completion = completion
        )
    )
}

/**
 * Shortcut to make ConnTask with:
 * - Method `GET`/`POST`
 * - Encode `params` with `form-url-encode` into URL query part or HTTP body
 * - JSON response handler
 * - ConnConnector is made from session settings.
 */
fun <SuccessType, FailureType> ConnSession.requestFormUrlEncodedJson(
    /** Request data. It requires at least URL */
    request: ConnRequest,
    /** Data will be encoded */
    params: Any?,
    /** Encode `params` into body if `true`, or url query if `false` */
    isBody: Boolean = false,
    /** Type to convert JSON success response data into object */
    successType: Class<SuccessType>,
    /** Type to convert JSON failure response data into object */
    failureType: Class<FailureType>? = null,
    /** Action on finish */
    completion: (ConnJsonResponseHandler<SuccessType, FailureType>) -> Unit
): ConnTask {
    return ConnTask(
        request = request,
        requestBuilder = this.makeFormUrlEncodedRequestBuilder(
            isBody = isBody,
            params = params
        ),
        connector = this.makeHttpConnector(),
        responseHandler = ConnJsonResponseHandler(
            successType = successType,
            failureType = failureType,
            completion = completion
        )
    )
}

/**
 * Shortcut to make ConnTask with:
 * - Method `POST`
 * - Encode `params` with HTTP body JSON
 */
fun ConnTask.Companion.requestJson(
    /** Request data. It requires at least URL */
    request: ConnRequest,
    /** Data will be encoded */
    params: Any,
    /** Object handles response data and return result */
    responseHandler: ConnTaskResponseHandler? = null
): ConnTask {
    return ConnTask(
        request = request,
        requestBuilder = ConnJsonBodyRequestBuilder(
            params = params
        ),
        responseHandler = responseHandler
    )
}

/**
 * Shortcut to make ConnTask with:
 * - Method `POST`
 * - Encode `params` with HTTP body JSON
 * - ConnConnector is made from session settings
 */
fun ConnSession.requestJson(
    /** Request data. It requires at least URL */
    request: ConnRequest,
    /** Data will be encoded */
    params: Any,
    /** Object handles response data and return result */
    responseHandler: ConnTaskResponseHandler? = null
): ConnTask {
    return ConnTask(
        request = request,
        requestBuilder = ConnJsonBodyRequestBuilder(
            params = params,
            charset = this.charSet
        ),
        connector = this.makeHttpConnector(),
        responseHandler = responseHandler
    )
}

/**
 * Shortcut to make ConnTask with:
 * - Method `POST`
 * - Encode `params` with HTTP body JSON
 * - JSON response handler
 */
fun <SuccessType, FailureType> ConnTask.Companion.requestJson(
    /** Request data. It requires at least URL */
    request: ConnRequest,
    /** Data will be encoded into request URL query */
    params: Any,
    /** Type to convert JSON success response data into object */
    successType: Class<SuccessType>,
    /** Type to convert JSON failure response data into object */
    failureType: Class<FailureType>? = null,
    /** Action on finish */
    completion: (ConnJsonResponseHandler<SuccessType, FailureType>) -> Unit
): ConnTask {
    return ConnTask(
        request = request,
        requestBuilder = ConnJsonBodyRequestBuilder(
            params = params
        ),
        responseHandler = ConnJsonResponseHandler(
            successType = successType,
            failureType = failureType,
            completion = completion
        )
    )
}

/**
 * Shortcut to make ConnTask with:
 * - Method `POST`
 * - Encode `params` with HTTP body JSON
 * - JSON response handler
 * - ConnConnector is made from session settings
 */
fun <SuccessType, FailureType> ConnSession.requestJson(
    /** Request data. It requires at least URL */
    request: ConnRequest,
    /** Data will be encoded into request URL query */
    params: Any,
    /** Type to convert JSON success response data into object */
    successType: Class<SuccessType>,
    /** Type to convert JSON failure response data into object */
    failureType: Class<FailureType>? = null,
    /** Action on finish */
    completion: (ConnJsonResponseHandler<SuccessType, FailureType>) -> Unit
): ConnTask {
    return ConnTask(
        request = request,
        requestBuilder = ConnJsonBodyRequestBuilder(
            params = params,
            charset = this.charSet
        ),
        connector = this.makeHttpConnector(),
        responseHandler = ConnJsonResponseHandler(
            successType = successType,
            failureType = failureType,
            completion = completion
        )
    )
}

/**
 * Shortcut to make ConnTask with:
 * - Method `POST`
 * - Encode `params` with `multipart/form-data`
 */
fun ConnTask.Companion.requestMultipart(
    /** Request data. It requires at least URL */
    request: ConnRequest,
    /** Data will be encoded */
    params: Any,
    /** Object handles response data and return result */
    responseHandler: ConnTaskResponseHandler? = null
): ConnTask {
    return ConnTask(
        request = request,
        requestBuilder = ConnMultipartFormDataRequestBuilder(
            params = params,
        ),
        responseHandler = responseHandler
    )
}

/**
 * Shortcut to make ConnTask with:
 * - Method `POST`
 * - Encode `params` with `multipart/form-data`
 * - ConnConnector is made from session settings
 */
fun ConnSession.requestMultipart(
    /** Request data. It requires at least URL */
    request: ConnRequest,
    /** Data will be encoded */
    params: Any,
    /** Object handles response data and return result */
    responseHandler: ConnTaskResponseHandler? = null
): ConnTask {
    return ConnTask(
        request = request,
        requestBuilder = ConnMultipartFormDataRequestBuilder(
            params = params,
            charset = this.charSet
        ),
        connector = this.makeHttpConnector(),
        responseHandler = responseHandler
    )
}

/**
 * Shortcut to make ConnTask with:
 * - Method `POST`
 * - Encode `params` with HTTP body JSON
 * - JSON response handler
 */
fun <SuccessType, FailureType> ConnTask.Companion.requestMultipart(
    /** Request data. It requires at least URL */
    request: ConnRequest,
    /** Data will be encoded into request URL query */
    params: Any,
    /** Type to convert JSON success response data into object */
    successType: Class<SuccessType>,
    /** Type to convert JSON failure response data into object */
    failureType: Class<FailureType>? = null,
    /** Action on finish */
    completion: (ConnJsonResponseHandler<SuccessType, FailureType>) -> Unit
): ConnTask {
    return ConnTask(
        request = request,
        requestBuilder = ConnMultipartFormDataRequestBuilder(
            params = params
        ),
        responseHandler = ConnJsonResponseHandler(
            successType = successType,
            failureType = failureType,
            completion = completion
        )
    )
}

/**
 * Shortcut to make ConnTask with:
 * - Method `POST`
 * - Encode `params` with HTTP body JSON
 * - JSON response handler
 * - ConnConnector is made from session settings
 */
fun <SuccessType, FailureType> ConnSession.requestMultipart(
    /** Request data. It requires at least URL */
    request: ConnRequest,
    /** Data will be encoded into request URL query */
    params: Any,
    /** Type to convert JSON success response data into object */
    successType: Class<SuccessType>,
    /** Type to convert JSON failure response data into object */
    failureType: Class<FailureType>? = null,
    /** Action on finish */
    completion: (ConnJsonResponseHandler<SuccessType, FailureType>) -> Unit
): ConnTask {
    return ConnTask(
        request = request,
        requestBuilder = ConnMultipartFormDataRequestBuilder(
            params = params,
            charset = this.charSet
        ),
        connector = this.makeHttpConnector(),
        responseHandler = ConnJsonResponseHandler(
            successType = successType,
            failureType = failureType,
            completion = completion
        )
    )
}

/**
 * Shortcut to make ConnTask with:
 * - Method `GET`/`POST`
 * - Encode `params` with `multipart/form-data`
 */
fun ConnTask.Companion.requestDownload(
    /** File to write downloaded data */
    destination: File,
    /** Download progress action */
    progressAction: ((ConnHttpUrlConnector.ProgressInfo) -> Unit)? = null,
    /** Request data. It requires at least URL */
    request: ConnRequest,
    /** Data will be encoded */
    params: Any?,
    /** Encode `params` into body if `true`, or url query if `false` */
    isBody: Boolean = false,
    /** Object handles response data and return result */
    responseHandler: ConnTaskResponseHandler? = null
): ConnTask {
    val connector = ConnHttpUrlConnector(outputFile = destination)
    connector.downloadProgressAction = progressAction
    return ConnTask(
        request = request,
        requestBuilder = ConnFormUrlEncodedRequestBuilder(
            params = params,
            isBody = isBody
        ),
        connector = connector,
        responseHandler = responseHandler
    )
}
