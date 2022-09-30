package sharedlib.httpconnection.apiconnection

import android.net.http.HttpResponseCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.CookieHandler
import java.net.CookieManager
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.coroutines.CoroutineContext

// TODO: authentication

/** Setting for ConnHttpUrlConnector */
data class ConnHttpUrlConnectorSetting(
    /** Connect-Timeout */
    val connectTimeout: Int? = ConnHttpUrlConnector.defaultSettings.connectTimeout,
    /** Read-Timeout */
    val readTimeout: Int? = ConnHttpUrlConnector.defaultSettings.readTimeout,
    /** Allow Cache */
    val enableCaching: Boolean? = ConnHttpUrlConnector.defaultSettings.enableCaching,
    /** Allow redirect */
    val redirect: Boolean? = ConnHttpUrlConnector.defaultSettings.redirect
) {}

/** Task connector using HttpUrlConnection */
open class ConnHttpUrlConnector(
    /** Write response data to specified file */
    val outputFile: File? = null,
    /** Connect-Timeout */
    val connectTimeout: Int? = defaultSettings.connectTimeout,
    /** Read-Timeout */
    val readTimeout: Int? = defaultSettings.readTimeout,
    /** Allow Cache */
    val enableCaching: Boolean? = defaultSettings.enableCaching,
    /** Allow redirect */
    val redirect: Boolean? = defaultSettings.redirect
): ConnTaskConnector {

    /** Progress data */
    data class ProgressInfo(
        /** Time when it starts the request */
        val begin: Date,
        /** Milliseconds since last moment read input */
        val timeInterval: Long,
        /** Just read bytes */
        val received: Int,
        /** Total read bytes */
        val totalReceived: Long,
        /** Supposed bytes */
        val supposedSize: Long?
    ){

        /** This is info when the request is finishing */
        val isLast: Boolean
            get() = (received == 0 && supposedSize != null && totalReceived == supposedSize)
    }

    companion object {
        /** HTTP redirection (`HttpURLConnection` supports only redirect between same scheme urls)*/
        var redirect: Boolean
            get() = HttpURLConnection.getFollowRedirects()
            set(value) = HttpURLConnection.setFollowRedirects(value)
        /** Default values for constructor */
        var defaultSettings = ConnHttpUrlConnectorSetting(
            connectTimeout = null,
            readTimeout = null,
            enableCaching = null,
            redirect = null
        )
        /** HttpResponseCache.install, IOException throwable  */
        fun configResponseCache(cacheDir: File, maxSize: Long) {
            HttpResponseCache.install(cacheDir, maxSize)
        }
        /** HttpResponseCache.getInstalled() */
        val installedCache: HttpResponseCache?
            get() = HttpResponseCache.getInstalled()
        /** CookieHandler.setDefault() */
        fun configCookieManager(cookieManager: CookieManager) {
            CookieHandler.setDefault(cookieManager)
        }
        /** CookieHandler.getDefault() */
        val defaultCookiesManager: CookieHandler?
            get() = CookieHandler.getDefault()
    }

    private var conn: HttpURLConnection? = null
    private var requestData: ConnRequest? = null
    private var originUrl: URL? = null
    private var timeBegin = Date()
    private var lastTime = Date()
    private var readBytes: Long = 0
    private var supposedSize: Long? = null

    /** Coroutine context to execute `downloadProgressAction` */
    var downloadProgressContext: CoroutineContext = Dispatchers.Main
    /**
     * Action after read a block of data.
     * Before the request finished (before return ConnResponse to ConnTask, it executes this action the last time
     * with `received` = 0, same `totalReceived` & `supposedSize`
     * */
    var downloadProgressAction: ((ProgressInfo) -> Unit)? = null

    private fun readStream(stream: InputStream, action: (ByteArray) -> Unit) {
        var stop = false
        var temp: ByteArray?
        while (!stop) {
            temp = stream.readBytes()
            if (temp.isNotEmpty()) {
                action(temp)
            } else {
                stop = true
            }
        }
    }

    private suspend fun prepare(task: ConnTask) {
        val request = requestData!!
        val url = URL(request.url.build())
        originUrl = url
        val con = url.openConnection() as HttpURLConnection
        conn = con
        if (connectTimeout != null) {
            con.connectTimeout = connectTimeout
        }
        if (readTimeout != null) {
            con.readTimeout = readTimeout
        }
        if (request.method != null) {
            con.requestMethod = request.method.rawValue
        }
        if (enableCaching != null) {
            con.useCaches = enableCaching
        }
        if (redirect != null) {
            con.instanceFollowRedirects = redirect
        }
        if (request.header != null && request.header.isNotEmpty()) {
            request.header.forEach { (key, value) ->
//                if (!key.rawValue.isHttpHeaderCompatible) {
//                    task.sendLog { "WARNING: Header contains non ISO-8859-1 character: \"${key.rawValue}\"" }
//                }
                value.forEach {
//                    if (!it.isHttpHeaderCompatible) {
//                        task.sendLog { "WARNING: Header contains non ISO-8859-1 character: \"${it}\"" }
//                    }
                    con.addRequestProperty(key.rawValue, it)
                }
            }
        }
        writeBody()
    }

    private fun writeBody() {
        val request = requestData!!
        val con = conn!!
        if (request.body != null || request.bodyFile != null) {
            if (request.body != null) {
                con.setFixedLengthStreamingMode(request.body.size)
                con.doOutput = true
                val stream = con.outputStream
                stream.write(request.body)
            } else if (request.bodyFile != null && request.bodyFile.exists()) {
                con.setFixedLengthStreamingMode(request.bodyFile.length())
                con.doOutput = true
                val stream = con.outputStream
                try {
                    val fileInput = request.bodyFile.inputStream()
                    readStream(stream = fileInput) { temp ->
                        stream.write(temp)
                    }
                    fileInput.close()
                } catch (error: Exception) {
                    con.disconnect()
                    conn = null
                    throw error
                }
            }
        }
    }

    private fun trackDlProgress(currentBytes: Int) {
        if (downloadProgressAction == null) { return }
        val last = lastTime
        lastTime = Date()
        val diff = lastTime.time - last.time
        readBytes += currentBytes
        runBlocking {
            withContext(downloadProgressContext) {
                downloadProgressAction!!.invoke(
                    ProgressInfo(
                        begin = timeBegin,
                        timeInterval = diff,
                        received = currentBytes,
                        totalReceived = readBytes,
                        supposedSize = supposedSize
                    )
                )
            }
        }
    }

    private fun send(): ConnResponse {
        val con = conn!!
        timeBegin = Date()
        lastTime = timeBegin
        readBytes = 0
        supposedSize = null
        try {
            val status = con.responseCode
            val header = con.headerFields
            val myHeader = mutableMapOf<ConnResponseHeaderField, List<String>>()
            header.forEach { (key, value) ->
                val keyStr = try {
                    key.toString()
                } catch (error: Exception) {
                    ""
                }
                val keyVal = ConnResponseHeaderField(rawValue = keyStr)
                myHeader[keyVal] = value
            }
            supposedSize = myHeader[ConnResponseHeaderField.contentLength]?.first()?.toLong()
            var inStream: InputStream? = try {
                con.inputStream
            } catch (error: Exception) {
                null
            }
            if (inStream == null) {
                inStream = try {
                    con.errorStream
                } catch (error: Exception) {
                    null
                }
            }
            var resBuffer: ByteArray? = null
            var resFile: File? = null
            if (inStream != null) {
                var outStream: OutputStream? = null
                if (outputFile != null) {
                    outStream = try {
                        outputFile.outputStream()
                    } catch (error: Exception) {
                        null
                    }
                }
                if (outStream != null) {
                    readStream(stream = inStream) { temp ->
                        outStream.write(temp)
                        trackDlProgress(temp.size)
                    }
                    outStream.close()
                    resFile = outputFile
                } else {
                    resBuffer = ByteArray(0)
                    readStream(stream = inStream) { temp ->
                        resBuffer += temp
                        trackDlProgress(temp.size)
                    }
                }
                supposedSize = readBytes
                trackDlProgress(0)
            }
            con.disconnect()
            conn = null
            return ConnResponse(
                status = ConnStatusCode(rawValue = status),
                header = myHeader,
                body = resBuffer,
                bodyFile = resFile,
                originUrl = originUrl,
                finalUrl = con.url
            )
        } catch (error: Exception) {
            con.disconnect()
            conn = null
            return ConnResponse(
                exception = error,
                originUrl = originUrl
            )
        }
    }

    override suspend fun perform(sender: ConnTask, request: ConnRequest): ConnResponse {
        requestData = request
        try {
            try {
                prepare(sender)
            } catch (error: Exception) {
                return ConnResponse(
                    exception = error,
                    originUrl = originUrl
                )
            }
            return send()
        } catch (error: Exception) {
            return ConnResponse(exception = error)
        }
    }

    override fun stop() {
        conn?.disconnect()
        conn = null
    }

}

/** Set default settings for ConnHttpConnector of current session */
fun ConnSession.setHttpConnectorSettings(settings: ConnHttpUrlConnectorSetting) {
    this.setComponentSettings(settings)
}

/** Create ConnHttpConnector with settings of current session */
fun ConnSession.makeHttpConnector(
    /** Write response data to specified file */
    outputFile: File? = null,
    /** Connect-Timeout */
    connectTimeout: Int? = null,
    /** Read-Timeout */
    readTimeout: Int? = null,
    /** Allow Cache */
    enableCaching: Boolean? = null,
    /** Allow redirect */
    redirect: Boolean? = null
): ConnHttpUrlConnector {
    val settings = this.getComponentSettings(ConnHttpUrlConnectorSetting::class.java)
    return ConnHttpUrlConnector(
        outputFile = outputFile,
        connectTimeout = connectTimeout ?: settings?.connectTimeout ?: ConnHttpUrlConnector.defaultSettings.connectTimeout,
        readTimeout = readTimeout ?: settings?.readTimeout ?: ConnHttpUrlConnector.defaultSettings.readTimeout,
        enableCaching = enableCaching ?: settings?.enableCaching ?: ConnHttpUrlConnector.defaultSettings.enableCaching,
        redirect = redirect ?: settings?.redirect ?: ConnHttpUrlConnector.defaultSettings.redirect
    )
}
