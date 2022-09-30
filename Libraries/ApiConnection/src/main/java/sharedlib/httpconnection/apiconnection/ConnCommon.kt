package sharedlib.httpconnection.apiconnection

import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

/** Value container */
open class ConnValueContainer<ValueType>(val rawValue: ValueType) {

    override fun equals(other: Any?): Boolean {
        if (other is ConnValueContainer<*>) {
            val value = other.rawValue
            if (value == null && rawValue == null) {
                return true
            }
            return rawValue?.equals(value) ?: false
        }
        return false
    }

    override fun hashCode(): Int {
        return rawValue.hashCode()
    }

    override fun toString(): String {
        return rawValue.toString()
    }

}

/**
https://tools.ietf.org/html/rfc2616

HTTP/1.1 clients and servers that parse the date value MUST accept
all three formats (for compatibility with HTTP/1.0), though they MUST
only generate the RFC 1123 format for representing HTTP-date values
in header fields. See section 19.3 for further information.

All HTTP date/time stamps MUST be represented in Greenwich Mean Time
(GMT), without exception. For the purposes of HTTP, GMT is exactly
equal to UTC (Coordinated Universal Time). This is indicated in the
first two formats by the inclusion of "GMT" as the three-letter
abbreviation for time zone, and MUST be assumed when reading the
asctime format. HTTP-date is public static let sensitive and MUST NOT include
additional LWS beyond that specifically included as SP in the
grammar.

HTTP-date    = rfc1123-date | rfc850-date | asctime-date
rfc1123-date = wkday "," SP date1 SP time SP "GMT"
rfc850-date  = weekday "," SP date2 SP time SP "GMT"
asctime-date = wkday SP date3 SP time SP 4DIGIT
date1        = 2DIGIT SP month SP 4DIGIT
; day month year (e.g., 02 Jun 1982)
date2        = 2DIGIT "-" month "-" 2DIGIT
; day-month-year (e.g., 02-Jun-82)
date3        = month SP ( 2DIGIT | ( SP 1DIGIT ))
; month day (e.g., Jun  2)
time         = 2DIGIT ":" 2DIGIT ":" 2DIGIT
; 00:00:00 - 23:59:59
wkday        = "Mon" | "Tue" | "Wed"
| "Thu" | "Fri" | "Sat" | "Sun"
weekday      = "Monday" | "Tuesday" | "Wednesday"
| "Thursday" | "Friday" | "Saturday" | "Sunday"
month        = "Jan" | "Feb" | "Mar" | "Apr"
| "May" | "Jun" | "Jul" | "Aug"
| "Sep" | "Oct" | "Nov" | "Dec"
 */
class ConnDateTimeFormat(rawValue: String): ConnValueContainer<String>(rawValue) {

    companion object {
        val rfc1123 = ConnDateTimeFormat("E, dd MMM yyyy HH:mm:ss z")
        /// The second format is in common use, but is based on the obsolete RFC 850 [12] date format and lacks a four-digit year.
        val rfc1036 = ConnDateTimeFormat("EEEE, dd-MMM-yy HH:mm:ss z")
        val asctime = ConnDateTimeFormat("E MMM d HH:mm:ss yyyy")
    }

    val formatter = SimpleDateFormat(rawValue)

    fun format(date: Date): String {
        return formatter.format(date)
    }

}

/**
 * Content coding values indicate an encoding transformation that has been or can be applied to an entity.
 */
class ConnContentCoding(rawValue: String): ConnValueContainer<String>(rawValue) {

    companion object {
        /**
         * An encoding format produced by the file compression program "gzip" (GNU zip) as described in RFC 1952 [25].
         * This format is a Lempel-Ziv coding (LZ77) with a 32 bit CRC.
         */
        var gzip = ConnContentCoding("gzip")
        /**
         * The encoding format produced by the common UNIX file compression program "compress". This format is an adaptive Lempel-Ziv-Welch coding (LZW).
         */
        var compress = ConnContentCoding("compress")
        /**
         * The "zlib" format defined in RFC 1950 [31] in combination with the "deflate" compression mechanism described in RFC 1951 [29].
         */
        var deflate = ConnContentCoding("deflate")
        /**
         * The default (identity) encoding; the use of no transformation whatsoever. This content-coding is used only in the Accept- Encoding header,
         * and SHOULD NOT be used in the Content-Encoding header.
         */
        var identity = ConnContentCoding("identity")
    }

}

class ConnMethod(rawValue: String): ConnValueContainer<String>(rawValue) {

    companion object {
        val get = ConnMethod("GET")
        val head = ConnMethod("HEAD")
        val post = ConnMethod("POST")
        val put = ConnMethod("PUT")
        val delete = ConnMethod("DELETE")
        val trace = ConnMethod("TRACE")
        val connect = ConnMethod("CONNECT")
    }

}

class ConnStatusCode(rawValue: Int): ConnValueContainer<Int>(rawValue) {

    companion object {
        /**
         *  100 - The client SHOULD continue with its request.
         */
        val continue_100 = ConnStatusCode(100)
        /**
         *  101 - The server understands and is willing to comply with the client’s request, via the Upgrade message header field (section 14.42), for a change in the application protocol being used on this connection. The server will switch protocols to those defined by the response’s Upgrade header field immediately after the empty line which terminates the 101 response.
         */
        val switchingProtocols_101 = ConnStatusCode(101)
        /**
         *  200 - The request has succeeded.
         */
        val ok_200 = ConnStatusCode(200)
        /**
         *  201 - The request has been fulfilled and resulted in a new resource being created.
         */
        val created_201 = ConnStatusCode(201)
        /**
         *  202 - The request has been accepted for processing, but the processing has not been completed.
         */
        val accepted_202 = ConnStatusCode(202)
        /**
         *  203 - The returned metainformation in the entity-header is not the definitive set as available from the origin server, but is gathered from a local or a third-party copy.
         */
        val nonAuthoritativeInformation_203 = ConnStatusCode(203)
        /**
         *  204 - The server has fulfilled the request but does not need to return an entity-body, and might want to return updated metainformation.
         */
        val noContent_204 = ConnStatusCode(204)
        /**
         *  205 - The server has fulfilled the request and the user agent SHOULD reset the document view which caused the request to be sent.
         */
        val resetContent_205 = ConnStatusCode(205)
        /**
         *  206 - The server has fulfilled the partial GET request for the resource.
         */
        val partialContent_206 = ConnStatusCode(206)
        /**
         *  300 - The requested resource corresponds to any one of a set of representations, each with its own specific location, and agent-driven negotiation information (section 12) is being provided so that the user (or user agent) can select a preferred representation and redirect its request to that location.
         */
        val multipleChoices_300 = ConnStatusCode(300)
        /**
         *  301 - The requested resource has been assigned a new permanent URI and any future references to this resource SHOULD use one of the returned URIs.
         */
        val movedPermanently_301 = ConnStatusCode(301)
        /**
         *  302 - The requested resource resides temporarily under a different URI.
         */
        val found_302 = ConnStatusCode(302)
        /**
         *  303 - The response to the request can be found under a different URI and SHOULD be retrieved using a GET method on that resource.
         */
        val seeOther_303 = ConnStatusCode(303)
        /**
         *  304 - If the client has performed a conditional GET request and access is allowed, but the document has not been modified, the server SHOULD respond with this status code.
         */
        val notModified_304 = ConnStatusCode(304)
        /**
         *  305 - The requested resource MUST be accessed through the proxy given by the Location field.
         */
        val useProxy_305 = ConnStatusCode(305)
        /**
         *  307 - The requested resource resides temporarily under a different URI.
         */
        val temporaryRedirect_307 = ConnStatusCode(307)
        /**
         *  400 - The request could not be understood by the server due to malformed syntax.
         */
        val badRequest_400 = ConnStatusCode(400)
        /**
         *  401 - The request requires user authentication.
         */
        val unauthorized_401 = ConnStatusCode(401)
        /**
         *  402 - This code is reserved for future use.
         */
        val paymentRequired_402 = ConnStatusCode(402)
        /**
         *  403 - The server understood the request, but is refusing to fulfill it. Authorization will not help and the request SHOULD NOT be repeated.
         */
        val forbidden_403 = ConnStatusCode(403)
        /**
         *  404 - The server has not found anything matching the Request-URI.
         */
        val notFound_404 = ConnStatusCode(404)
        /**
         *  405 - The method specified in the Request-Line is not allowed for the resource identified by the Request-URI.
         */
        val methodNotAllowed_405 = ConnStatusCode(405)
        /**
         *  406 - The resource identified by the request is only capable of generating response entities which have content characteristics not acceptable according to the accept headers sent in the request.
         */
        val notAcceptable_406 = ConnStatusCode(406)
        /**
         *  407 - This code is similar to 401 (Unauthorized), but indicates that the client must first authenticate itself with the proxy.
         */
        val proxyAuthenticationRequired_407 = ConnStatusCode(407)
        /**
         *  408 - The client did not produce a request within the time that the server was prepared to wait.
         */
        val requestTimeout_408 = ConnStatusCode(408)
        /**
         *  409 - The request could not be completed due to a conflict with the current state of the resource.
         */
        val conflict_409 = ConnStatusCode(409)
        /**
         *  410 - The requested resource is no longer available at the server and no forwarding address is known.
         */
        val gone_410 = ConnStatusCode(410)
        /**
         *  411 - The server refuses to accept the request without a definedContent-Length.
         */
        val lengthRequired_411 = ConnStatusCode(411)
        /**
         *  412 - The precondition given in one or more of the request-header fields evaluated to false when it was tested on the server.
         */
        val preconditionFailed_412 = ConnStatusCode(412)
        /**
         *  413 - The server is refusing to process a request because the request entity is larger than the server is willing or able to process.
         */
        val requestEntityTooLarge_413 = ConnStatusCode(413)
        /**
         *  414 - The server is refusing to service the request because the Request-URI is longer than the server is willing to interpret.
         */
        val requestURITooLong_414 = ConnStatusCode(414)
        /**
         *  415 - The server is refusing to service the request because the entity of the request is in a format not supported by the requested resource for the requested method.
         */
        val unsupportedMediaType_415 = ConnStatusCode(415)
        /**
         *  416 - A server SHOULD return a response with this status code if a request included a Range request-header field (section 14.35) , and none of the range-specifier values in this field overlap the current extent of the selected resource, and the request did not include an If-Range request-header field.
         */
        val requestedRangeNotSatisfiable_416 = ConnStatusCode(416)
        /**
         *  417 - The expectation given in an Expect request-header field (see section 14.20) could not be met by this server, or, if the server is a proxy, the server has unambiguous evidence that the request could not be met by the next-hop server.
         */
        val expectationFailed_417 = ConnStatusCode(417)
        /**
         *  500 - The server encountered an unexpected condition which prevented it from fulfilling the request.
         */
        val internalServerError_500 = ConnStatusCode(500)
        /**
         *  501 - The server does not support the functionality required to fulfill the request.
         */
        val notImplemented_501 = ConnStatusCode(501)
        /**
         *  502 - The server, while acting as a gateway or proxy, received an invalid response from the upstream server it accessed in attempting to fulfill the request.
         */
        val badGateway_502 = ConnStatusCode(502)
        /**
         *  503 - The server is currently unable to handle the request due to a temporary overloading or maintenance of the server.
         */
        val serviceUnavailable_503 = ConnStatusCode(503)
        /**
         *  504 - The server, while acting as a gateway or proxy, did not receive a timely response from the upstream server specified by the URI (e.g. HTTP, FTP, LDAP) or some other auxiliary server (e.g. DNS) it needed to access in attempting to complete the request.
         */
        val gatewayTimeout_504 = ConnStatusCode(504)
        /**
         *  505 - The server does not support, or refuses to support, the HTTP protocol version that was used in the request message.
         */
        val httpVersionNotSupported_505 = ConnStatusCode(505)
    }

}

class ConnRequestHeaderField(rawValue: String): ConnValueContainer<String>(rawValue) {

    companion object {
        /**
         *  The `Accept` request-header field can be used to specify certain media types which are acceptable for the response.
         */
        val accept = ConnRequestHeaderField("Accept")
        /**
         *  The `Accept-Charset` request-header field can be used to indicate what character sets are acceptable for the response.
         */
        val acceptCharset = ConnRequestHeaderField("Accept-Charset")
        /**
         *  The `Accept-Encoding` request-header field is similar to Accept, but restricts the content-codings (section 3.5) that are acceptable in the response.
         */
        val acceptEncoding = ConnRequestHeaderField("Accept-Encoding")
        /**
         *  The `Accept-Language` request-header field is similar to Accept, but restricts the set of natural languages that are preferred as a response to the request.
         */
        val acceptLanguage = ConnRequestHeaderField("Accept-Language")
        /**
         *  The `Allow` entity-header field lists the set of methods supported by the resource identified by the Request-URI.
         */
        val allow = ConnRequestHeaderField("Allow")
        /**
         *  A user agent that wishes to authenticate itself with a server--usually, but not necessarily, after receiving a 401 response--does so by including an `Authorization` request-header field with the request.
         */
        val authorization = ConnRequestHeaderField("Authorization")
        /**
         *  The `Cache-Control` general-header field is used to specify directives that MUST be obeyed by all caching mechanisms along the request/response chain.
         */
        val cacheControl = ConnRequestHeaderField("Cache-Control")
        /**
         *  The `Connection` general-header field allows the sender to specify options that are desired for that particular connection and MUST NOT be communicated by proxies over further connections.
         */
        val connection = ConnRequestHeaderField("Connection")
        /**
         *  The `Content-Encoding` entity-header field is used as a modifier to the media-type.
         */
        val contentEncoding = ConnRequestHeaderField("Content-Encoding")
        /**
         *  The `Content-Language` entity-header field describes the natural language(s) of the intended audience for the enclosed entity.
         */
        val contentLanguage = ConnRequestHeaderField("Content-Language")
        /**
         *  The `Content-Length` entity-header field indicates the size of the entity-body, in decimal number of OCTETs, sent to the recipient or, in the case of the HEAD method, the size of the entity-body that would have been sent had the request been a GET.
         */
        val contentLength = ConnRequestHeaderField("Content-Length")
        /**
         *  The `Content-Location` entity-header field MAY be used to supply the resource location for the entity enclosed in the message when that entity is accessible from a location separate from the requested resource’s URI.
         */
        val contentLocation = ConnRequestHeaderField("Content-Location")
        /**
         *  The `Content-MD5` entity-header field, as defined in RFC 1864 [23], is an MD5 digest of the entity-body for the purpose of providing an end-to-end message integrity check (MIC) of the entity-body.
         */
        val contentMD5 = ConnRequestHeaderField("Content-MD5")
        /**
         *  The `Content-Range` entity-header is sent with a partial entity-body to specify where in the full entity-body the partial body should be applied.
         */
        val contentRange = ConnRequestHeaderField("Content-Range")
        /**
         *  The `Content-Type` entity-header field indicates the media type of the entity-body sent to the recipient or, in the case of the HEAD method, the media type that would have been sent had the request been a GET.
         */
        val contentType = ConnRequestHeaderField("Content-Type")
        /**
         *  The `Date` general-header field represents the date and time at which the message was originated, having the same semantics as orig-date in RFC 822.
         */
        val date = ConnRequestHeaderField("Date")
        /**
         *  The `Expect` request-header field is used to indicate that particular server behaviors are required by the client.
         */
        val expect = ConnRequestHeaderField("Expect")
        /**
         *  The `Expires` entity-header field gives the date/time after which the response is considered stale.
         */
        val expires = ConnRequestHeaderField("Expires")
        /**
         *  The `From` request-header field, if given, SHOULD contain an Internet e-mail address for the human user who controls the requesting user agent.
         */
        val from = ConnRequestHeaderField("From")
        /**
         *  The `Host` request-header field specifies the Internet host and port number of the resource being requested, as obtained from the original URI given by the user or referring resource (generally an HTTP URL, as described in section 3.2.2).
         */
        val host = ConnRequestHeaderField("Host")
        /**
         *  The `If-Match` request-header field is used with a method to make it conditional.
         */
        val ifMatch = ConnRequestHeaderField("If-Match")
        /**
         *  The `If-Modified-Since` request-header field is used with a method to make it conditional: if the requested variant has not been modified since the time specified in this field, an entity will not be returned from the server; instead, a 304 (not modified) response will be returned without any message-body.
         */
        val ifModifiedSince = ConnRequestHeaderField("If-Modified-Since")
        /**
         *  The `If-None-Match` request-header field is used with a method to make it conditional.
         */
        val ifNoneMatch = ConnRequestHeaderField("If-None-Match")
        /**
         *  The `If-Range` header allows a client to “short-circuit” the second request.
         */
        val ifRange = ConnRequestHeaderField("If-Range")
        /**
         *  The `If-Unmodified-Since` request-header field is used with a method to make it conditional.
         */
        val ifUnmodifiedSince = ConnRequestHeaderField("If-Unmodified-Since")
        /**
         *  The `Last-Modified` entity-header field indicates the date and time at which the origin server believes the variant was last modified.
         */
        val lastModified = ConnRequestHeaderField("Last-Modified")
        /**
         *  The `Max-Forwards` request-header field provides a mechanism with the TRACE (section 9.8) and OPTIONS (section 9.2) methods to limit the number of proxies or gateways that can forward the request to the next inbound server.
         */
        val maxForwards = ConnRequestHeaderField("Max-Forwards")
        /**
         *  The `Pragma` general-header field is used to include implementation-specific directives that might apply to any recipient along the request/response chain.
         */
        val pragma = ConnRequestHeaderField("Pragma")
        /**
         *  The `Proxy-Authorization` request-header field allows the client to identify itself (or its user) to a proxy which requires authentication.
         */
        val proxyAuthorization = ConnRequestHeaderField("Proxy-Authorization")
        /**
         *  Byte range specifications in HTTP apply to the sequence of bytes in the entity-body
         */
        val range = ConnRequestHeaderField("Range")
        /**
         *  The `Referer`[sic] request-header field allows the client to specify, for the server’s benefit, the address (URI) of the resource from which the Request-URI was obtained (the “referrer”, although the header field is misspelled.)
         */
        val referer = ConnRequestHeaderField("Referer")
        /**
         *  The `TE` request-header field indicates what extension transfer-codings it is willing to accept in the response and whether or not it is willing to accept trailer fields in a chunked transfer-coding.
         */
        val transferCoding = ConnRequestHeaderField("TE")
        /**
         *  The `Trailer` general field value indicates that the given set of header fields is present in the trailer of a message encoded with chunked transfer-coding.
         */
        val trailer = ConnRequestHeaderField("Trailer")
        /**
         *  The `Transfer-Encoding` general-header field indicates what (if any) type of transformation has been applied to the message body in order to safely transfer it between the sender and the recipient.
         */
        val transferEncoding = ConnRequestHeaderField("Transfer-Encoding")
        /**
         *  The `Upgrade` general-header allows the client to specify what additional communication protocols it supports and would like to use if the server finds it appropriate to switch protocols.
         */
        val upgrade = ConnRequestHeaderField("Upgrade")
        /**
         *  The `User-Agent` request-header field contains information about the user agent originating the request.
         */
        val userAgent = ConnRequestHeaderField("User-Agent")
        /**
         *  The `Vary` field value indicates the set of request-header fields that fully determines, while the response is fresh, whether a cache is permitted to use the response to reply to a subsequent request without revalidation.
         */
        val vary = ConnRequestHeaderField("Vary")
        /**
         *  The `Via` general-header field MUST be used by gateways and proxies to indicate the intermediate protocols and recipients between the user agent and the server on requests, and between the origin server and the client on responses.
         */
        val via = ConnRequestHeaderField("Via")
        /**
         *  The `Warning` general-header field is used to carry additional information about the status or transformation of a message which might not be reflected in the message.
         */
        val warning = ConnRequestHeaderField("Warning")
        // From Wikipedia https://en.wikipedia.org/wiki/List_of_HTTP_header_fields
        /**
         *  Acceptable version in time
         */
        val acceptDatetime = ConnRequestHeaderField("Accept-Datetime")
        /**
         *  An HTTP cookie previously sent by the server with Set-Cookie
         */
        val cookie = ConnRequestHeaderField("Cookie")
        /**
         *  No description (Used in multipart)
         */
        val contentDisposition = ConnRequestHeaderField("Content-Disposition")
    }

}

class ConnResponseHeaderField(rawValue: String): ConnValueContainer<String>(rawValue) {

    companion object {
        /**
         *  The `Accept-Ranges` response-header field allows the server to indicate its acceptance of range requests for a resource
         */
        val acceptRanges = ConnResponseHeaderField("Accept-Ranges")
        /**
         *  The `Age` response-header field conveys the sender's estimate of the amount of time since the response (or its revalidation) was generated at the origin server.
         */
        val age = ConnResponseHeaderField("Age")
        /**
         *  The `Allow` entity-header field lists the set of methods supported by the resource identified by the Request-URI.
         */
        val allow = ConnResponseHeaderField("Allow")
        /**
         *  The `Cache-Control` general-header field is used to specify directives that MUST be obeyed by all caching mechanisms along the request/response chain.
         */
        val cacheControl = ConnResponseHeaderField("Cache-Control")
        /**
         *  The `Connection` general-header field allows the sender to specify options that are desired for that particular connection and MUST NOT be communicated by proxies over further connections.
         */
        val connection = ConnResponseHeaderField("Connection")
        /**
         *  The `Content-Encoding` entity-header field is used as a modifier to the media-type.
         */
        val contentEncoding = ConnResponseHeaderField("Content-Encoding")
        /**
         *  The `Content-Language` entity-header field describes the natural language(s) of the intended audience for the enclosed entity.
         */
        val contentLanguage = ConnResponseHeaderField("Content-Language")
        /**
         *  The `Content-Length` entity-header field indicates the size of the entity-body, in decimal number of OCTETs, sent to the recipient or, in the case of the HEAD method, the size of the entity-body that would have been sent had the request been a GET.
         */
        val contentLength = ConnResponseHeaderField("Content-Length")
        /**
         *  The `Content-Location` entity-header field MAY be used to supply the resource location for the entity enclosed in the message when that entity is accessible from a location separate from the requested resource’s URI.
         */
        val contentLocation = ConnResponseHeaderField("Content-Location")
        /**
         *  The `Content-MD5` entity-header field, as defined in RFC 1864 [23], is an MD5 digest of the entity-body for the purpose of providing an end-to-end message integrity check (MIC) of the entity-body.
         */
        val contentMD5 = ConnResponseHeaderField("Content-MD5")
        /**
         *  The `Content-Range` entity-header is sent with a partial entity-body to specify where in the full entity-body the partial body should be applied.
         */
        val contentRange = ConnResponseHeaderField("Content-Range")
        /**
         *  The `Content-Type` entity-header field indicates the media type of the entity-body sent to the recipient or, in the case of the HEAD method, the media type that would have been sent had the request been a GET.
         */
        val contentType = ConnResponseHeaderField("Content-Type")
        /**
         *  The `Date` general-header field represents the date and time at which the message was originated, having the same semantics as orig-date in RFC 822.
         */
        val date = ConnResponseHeaderField("Date")
        /**
         *  The `ETag` response-header field provides the current value of the entity tag for the requested variant.
         */
        val etag = ConnResponseHeaderField("ETag")
        /**
         *  The `Expires` entity-header field gives the date/time after which the response is considered stale.
         */
        val expires = ConnResponseHeaderField("Expires")
        /**
         *  The `Last-Modified` entity-header field indicates the date and time at which the origin server believes the variant was last modified.
         */
        val lastModified = ConnResponseHeaderField("Last-Modified")
        /**
         *  The `Location` response-header field is used to redirect the recipient to a location other than the Request-URI for completion of the request or identification of a new resource.
         */
        val location = ConnResponseHeaderField("Location")
        /**
         *  The `Pragma` general-header field is used to include implementation-specific directives that might apply to any recipient along the request/response chain.
         */
        val pragma = ConnResponseHeaderField("Pragma")
        /**
         *  The `Proxy-Authenticate` response-header field MUST be included as part of a 407 (Proxy Authentication Required) response.
         */
        val proxyAuthenticate = ConnResponseHeaderField("Proxy-Authenticate")
        /**
         *  Byte range specifications in HTTP apply to the sequence of bytes in the entity-body
         */
        val range = ConnResponseHeaderField("Range")
        /**
         *  The `Retry-After` response-header field can be used with a 503 (Service Unavailable) response to indicate how long the service is expected to be unavailable to the requesting client.
         */
        val retryAfter = ConnResponseHeaderField("Retry-After")
        /**
         *  The `Server` response-header field contains information about the software used by the origin server to handle the request.
         */
        val server = ConnResponseHeaderField("Server")
        /**
         *  The `Trailer` general field value indicates that the given set of header fields is present in the trailer of a message encoded with chunked transfer-coding.
         */
        val trailer = ConnResponseHeaderField("Trailer")
        /**
         *  The `Transfer-Encoding` general-header field indicates what (if any) type of transformation has been applied to the message body in order to safely transfer it between the sender and the recipient.
         */
        val transferEncoding = ConnResponseHeaderField("Transfer-Encoding")
        /**
         *  The `Upgrade` general-header allows the client to specify what additional communication protocols it supports and would like to use if the server finds it appropriate to switch protocols.
         */
        val upgrade = ConnResponseHeaderField("Upgrade")
        /**
         *  The `Via` general-header field MUST be used by gateways and proxies to indicate the intermediate protocols and recipients between the user agent and the server on requests, and between the origin server and the client on responses.
         */
        val via = ConnResponseHeaderField("Via")
        /**
         *  The `Warning` general-header field is used to carry additional information about the status or transformation of a message which might not be reflected in the message.
         */
        val warning = ConnResponseHeaderField("Warning")
        /**
         *  The `WWW-Authenticate` response-header field MUST be included in 401 (Unauthorized) response messages.
         */
        val wwAuthenticate = ConnResponseHeaderField("WWW-Authenticate")
        /**
         *  An opportunity to raise a "File Download" dialogue box for a known MIME type with binary format or suggest a filename for dynamic content. Quotes are necessary with special characters.
         */
        val contentDisposition = ConnResponseHeaderField("Content-Disposition")
        // From Wikipedia
        /**
         *  An HTTP cookie
         */
        val setCookie = ConnResponseHeaderField("Set-Cookie")
    }

}

class ConnURIScheme(rawValue: String): ConnValueContainer<String>(rawValue) {

    companion object {
        val http = ConnURIScheme("http")
        val https = ConnURIScheme("https")
        val file = ConnURIScheme("file")
        val ftp = ConnURIScheme("ftp")
        val samba = ConnURIScheme("smb")
    }

}

/** Check if this string can be encoded into data with ISO-8859-1 charset (so it can be used in HTTP header) */
val String.isHttpHeaderCompatible: Boolean
    get() {
        val charset = Charsets.ISO_8859_1
        val tmp = this.toByteArray(charset = charset).toString(charset = charset)
        return tmp == this
    }

/** "\r\n" (line separator in HTTP headers) */
val String.Companion.CRLF: String
    get() = "\r\n"
