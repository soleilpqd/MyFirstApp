package sharedlib.httpconnection.apiconnection

import kotlin.text.StringBuilder

/** Setting for ConnUrl */
data class ConnUrlSetting(
    /** Scheme, default `https` */
    val scheme: ConnURIScheme = ConnUrl.defaultSetting.scheme,
    /** Host, must not empty */
    val host: String = ConnUrl.defaultSetting.host,
    /** Port */
    val port: UInt? = ConnUrl.defaultSetting.port,
    /** User name */
    val userName: String? = ConnUrl.defaultSetting.userName,
    /** Password */
    val password: String? = ConnUrl.defaultSetting.password
) {}

/**
 * Data class to make URL
 * `scheme://username:password@host:port/path/components/?query1=value1#fragment`
 */
data class ConnUrl(
    /** Scheme, default `https` */
    var scheme: ConnURIScheme = defaultSetting.scheme,
    /** Host, must not empty */
    var host: String = defaultSetting.host,
) {

    companion object {
        /** Common Port of HTTP */
        val commonHttpPort: UInt = 80u
        /** Common Port of HTTPS */
        val commonHttpsPort: UInt = 443u
        /** Default value for constructor */
        val defaultSetting = ConnUrlSetting(
            scheme = ConnURIScheme.https,
            host = "",
            port = null,
            userName = null,
            password = null
        )
    }
    /** Port */
    var port: UInt? = defaultSetting.port
    /** Path components */
    var pathComponents: List<String>? = null
    var forcePathEndWithSlash = false
    /** Raw query (not URL encoded) */
    var queries: Map<String, String>? = null
    /** Encoded query */
    var query: String? = null
    /** User name */
    var userName: String? = defaultSetting.userName
    /** Password */
    var password: String? = defaultSetting.password

//    /**
//     * Fragment
//     */
//    var fragment: String? = null

    fun duplicate(): ConnUrl {
        val result = ConnUrl(scheme = this.scheme, host = this.host)
        result.port = port
        result.pathComponents = pathComponents
        result.forcePathEndWithSlash = forcePathEndWithSlash
        result.queries = queries
        result.query = query
        result.userName = userName
        result.password = password
        return result
    }

    internal fun build(): String {
        val encoder = ConnUrlEncoder()
        val result = StringBuilder()
        result.append(scheme.rawValue)
        result.append("://")
        if (userName != null && password != null) {
            val uName = encoder.encode(text = userName!!)
            val pss = encoder.encode(text = password!!)
            result.append("${uName}:${pss}@")
        }
        result.append(host.trim('/'))
        if (port != null) {
            result.append(":${port.toString()}")
        }

        val pathPart = StringBuilder()
        if (pathComponents != null && pathComponents!!.isNotEmpty()) {
            for (item in pathComponents!!) {
                val path = encoder.encode(text = item)
                pathPart.append("/${path}")
            }
            if (forcePathEndWithSlash) {
                pathPart.append("/")
            }
        }

        val queryPart = StringBuilder()
        if (queries != null && queries!!.isNotEmpty()) {
            queryPart.append(encoder.encode(map = queries!!))
        }
        if (query != null && query!!.isNotEmpty()) {
            if (queryPart.isNotEmpty()) {
                queryPart.append("&")
            }
            queryPart.append(query!!)
        }
        if (pathPart.isNotEmpty()) {
            result.append("${pathPart.toString()}")
            if (queryPart.isNotEmpty()) {
                result.append("?${queryPart.toString()}")
            }
        } else if (queryPart.isNotEmpty()) {
            result.append("/?${queryPart.toString()}")
        } else if (forcePathEndWithSlash) {
            result.append("/")
        }

        return result.toString()
    }

    override fun toString(): String {
        return build()
    }

}

/** Set default settings for ConnUrl of current session */
fun ConnSession.setConnUrlSettings(settings: ConnUrlSetting) {
    this.setComponentSettings(settings)
}

/** Create ConnUrl with settings of current session */
fun ConnSession.makeConnUrl(
    /** Scheme */
    scheme: ConnURIScheme? = null,
    /** Host, must not empty */
    host: String? = null,
    /** Port */
    port: UInt? = null,
    /** User name */
    userName: String? = null,
    /** Password */
    password: String? = null
): ConnUrl {
    val settings = this.getComponentSettings(ConnUrlSetting::class.java)
    val result = ConnUrl(
        scheme = scheme ?: settings?.scheme ?: ConnUrl.defaultSetting.scheme,
        host = host ?: settings?.host ?: ConnUrl.defaultSetting.host
    )
    result.port = port ?: settings?.port ?: ConnUrl.defaultSetting.port
    result.userName = userName ?: settings?.userName ?: ConnUrl.defaultSetting.userName
    result.password = password ?: settings?.password ?: ConnUrl.defaultSetting.password
    return result
}
