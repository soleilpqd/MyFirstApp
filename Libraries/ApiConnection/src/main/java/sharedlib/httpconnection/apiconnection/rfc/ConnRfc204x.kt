package sharedlib.httpconnection.apiconnection

import java.io.File
import kotlin.text.StringBuilder

// https://tools.ietf.org/html/rfc2045
// https://tools.ietf.org/html/rfc2046
// https://tools.ietf.org/html/rfc2047

/** Data class for ABNF header field `name: value; param1=param_value1; param2="param value 2"` */
open class ConnABNFHeaderLine(
    /** Field name */
    val name: String,
    /** Field value */
    val value: String,
    /** Field parameters */
    val parameters: Map<String, String>?
    ) {

    val body: String
        get() {
            val result = StringBuilder("${value}")
            if (parameters != null && parameters.count() > 0) {
                result.append(";")
                var index = 0
                parameters.forEach { key, value ->
                    val suffix = if (index == parameters.count() - 1) "" else ";"
                    if (ConnHeaderLine.shouldQuote(value)) {
                        result.append(" ${key}=\"${value}\"${suffix}")
                    } else {
                        result.append(" ${key}=${value}${suffix}")
                    }
                    index += 1
                }
            }
            return result.toString()
        }

    val fullLine: String
        get() = "${name}: ${body}"

}

/** Data class for Content-Type header field `Content-Type: mainType/subtype; param1=param_value1; ...`*/
class ConnContentType(
    val mainType: String,
    val supType: String,
    parameters: Map<String, String>? = null
): ConnABNFHeaderLine(
    name = ConnRequestHeaderField.contentType.rawValue,
    value = "${mainType}/${supType}",
    parameters = parameters
) {

    class MainType {
        companion object {
            val application = "application"
            val text = "text"
            val image = "image"
            val audio = "audio"
            val video = "video"
            val message = "message"
            val multipart = "multipart"
        }
    }

    class ApplicationSubType {
        companion object {
            /** Atom feeds */
            val atomXml = "atom+xml"

            /** ECMAScript/JavaScript; Defined in RFC 4329 (equivalent to application/javascript but with stricter processing rules) */
            val emacScript = "ecmascript"

            /** EDI X12 data; Defined in RFC 1767 */
            val ediX12 = "EDI-X12"

            /** EDI EDIFACT data; Defined in RFC 1767 */
            val edifact = "EDIFACT"

            /** JavaScript Object Notation JSON; Defined in RFC 4627 */
            val json = "json"

            /** ECMAScript/JavaScript; Defined in RFC 4329 (equivalent to application/ecmascript but with looser processing rules) It is not accepted in IE 8 or earlier - text/javascript is accepted but it is defined as obsolete in RFC 4329. The "type" attribute of the <script> tag in HTML5 is optional. In practice, omitting the media type of JavaScript programs is the most interoperable solution, since all browsers have always assumed the correct default even before HTML5. */
            val javaScript = "javascript"

            /** Arbitrary binary data.[6] Generally speaking this type identifies files that are not associated with a specific application. Contrary to past assumptions by software packages such as Apache this is not a type that should be applied to unknown files. In such a case, a server or application should not indicate a content type, as it may be incorrect, but rather, should omit the type in order to allow the recipient to guess the type.[7] */
            val octetStream = "octet-stream"

            /** Ogg, a multimedia bitstream container format; Defined in RFC 5334 */
            val ogg = "ogg"

            /** Portable Document Format, PDF has been in use for document exchange on the Internet since 1993; Defined in RFC 3778 */
            val pdf = "pdf"

            /** PostScript; Defined in RFC 2046 */
            val postSrcipt = "postscript"

            /** Resource Description Framework; Defined by RFC 3870 */
            val rdfXml = "rdf+xml"

            /** RSS feeds */
            val rssXml = "rss+xml"

            /** SOAP; Defined by RFC 3902 */
            val soapXml = "soap+xml"

            /** Web Open Font Format; (candidate recommendation; use application/x-font-woff until standard is official) */
            val fontWorff = "font-woff"

            /** XHTML; Defined by RFC 3236 */
            val xhtmlXml = "xhtml+xml"

            /** XML files; Defined by RFC 3023 */
            val xml = "xml"

            /** DTD files; Defined by RFC 3023 */
            val xmlDtd = "xml-dtd"

            /** XOP */
            val xopXml = "xop+xml"

            /** ZIP archive files; Registered[8] */
            val zip = "zip"

            /** Gzip, Defined in RFC 6713 */
            val gzip = "gzip"

            /** example in documentation, Defined in RFC 4735 */
            val example = "example"

            /** for Native Client modules the type must be “application/x-nacl” */
            val xNacl = "x-nacl"

            /** OpenDocument Text; Registered[13] */
            val vndOpendocText = "vnd.oasis.opendocument.text"

            /** OpenDocument Spreadsheet; Registered[14] */
            val vndOpendocSpreadsheet = "vnd.oasis.opendocument.spreadsheet"

            /** OpenDocument Presentation; Registered[15] */
            val vndOpendocPresentation = "vnd.oasis.opendocument.presentation"

            /** OpenDocument Graphics; Registered[16] */
            val vndOpendocGraphics = "vnd.oasis.opendocument.graphics"

            /** Microsoft Excel files */
            val vndMsExcel = "vnd.ms-excel"

            /** Microsoft Excel 2007 files */
            val vndOpenxmlSpreadsheet =
                "vnd.openxmlformats-officedocument.spreadsheetml.sheet"

            /** Microsoft Powerpoint files */
            val vndMsPowerpoint = "vnd.ms-powerpoint"

            /** Microsoft Powerpoint 2007 files */
            val vndOpenxmlPresentation =
                "vnd.openxmlformats-officedocument.presentationml.presentation"

            /** Microsoft Word 2007 files */
            val vndOpenxmlWord =
                "vnd.openxmlformats-officedocument.wordprocessingml.document"

            /** Mozilla XUL files */
            val vndMozillaXul = "vnd.mozilla.xul+xml"

            /** KML files (e.g. for Google Earth)[17] */
            val vndGgEarthKml = "vnd.google-earth.kml+xml"

            /** KMZ files (e.g. for Google Earth)[18] */
            val vndGgEarthKmz = "vnd.google-earth.kmz"

            /** Dart files [19] */
            val dart = "dart"

            /** For download apk files. */
            val vndApk = "vnd.android.package-archive"

            /** XPS document[20] */
            val vndXps = "vnd.ms-xpsdocument"

            /** 7-Zip compression format. */
            val x7z = "x-7z-compressed"

            /** Google Chrome/Chrome OS extension, app, or theme package [23] */
            val xChromeExt = "x-chrome-extension"

            /** deb (file format), a software package format used by the Debian project */
            val xDeb = "x-deb"

            /** device-independent document in DVI format */
            val xDvi = "x-dvi"

            /** TrueType Font No registered MIME type, but this is the most commonly used */
            val xTtf = "x-font-ttf"

            /** #define MIME_TYPE_APP_X_JAVASCRIPT  @"application/x-javascript" */
            val xJavascript = "x-javascript"

            /** LaTeX files */
            val xLatex = "x-latex"

            /** .m3u8 variant playlist */
            val xM3u8 = "x-mpegURL"

            /** RAR archive files */
            val xRar = "x-rar-compressed"

            /** Adobe Flash files for example with the extension .swf */
            val xSwf = "x-shockwave-flash"

            /** StuffIt archive files */
            val xStuffit = "x-stuffit"

            /** Tarball files */
            val xTar = "x-tar"

            /** Form Encoded Data; Documented in HTML 4.01 Specification, Section 17.13.4.1 */
            val xWwwForm = "x-www-form-urlencoded"

            /** Add-ons to Mozilla applications (Firefox, Thunderbird, SeaMonkey, and the discontinued Sunbird) */
            val xXpi = "x-xpinstall"

            /** a variant of PKCS standard files */
            val txtXPkcs12 = "x-pkcs12"
        }
    }

    class TextSubType {
        companion object {
            val paramCharset = "charset"

            /** commands; subtype resident in Gecko browsers like Firefox 3.5 */
            val cmd = "cmd"

            /** Cascading Style Sheets; Defined in RFC 2318 */
            val css = "css"

            /** Comma-separated values; Defined in RFC 4180 */
            val csv = "csv"

            /** HTML; Defined in RFC 2854 */
            val html = "html"

            /** JavaScript; Defined in and made obsolete in RFC 4329 in order to discourage its usage in favor of application/javascript. However, text/javascript is allowed in HTML 4 and 5 and, unlike application/javascript, has cross-browser support. The "type" attribute of the <script> tag in HTML5 is optional and there is no need to use it at all since all browsers have always assumed the correct default (even in HTML 4 where it was required by the specification). */
            val javascript = "javascript (Obsolete)"

            /** Textual data; Defined in RFC 2046 and RFC 3676 */
            val plain = "plain"

            /** RTF; Defined by Paul Lindner */
            val rtf = "rtf"

            /** vCard (contact information); Defined in RFC 6350 */
            val vcard = "vcard"

            /** Extensible Markup Language; Defined in RFC 3023 */
            val xml = "xml"

            /** example in documentation, Defined in RFC 4735 */
            val example = "example"

            /** ABC music notation; Registered[11] */
            val abc = "vnd.abc"

            /** GoogleWebToolkit data */
            val txtXGwtRpc = "x-gwt-rpc"

            /** jQuery template data */
            val txtXJqueryTmpl = "x-jquery-tmpl"

            /** Markdown formatted text */
            val txtXMarkdown = "x-markdown"
        }
    }

    class ImageSubType {
        companion object {
            /** GIF image; Defined in RFC 2045 and RFC 2046 */
            val gif = "gif"

            /** JPEG JFIF image; Defined in RFC 2045 and RFC 2046 */
            val jpeg = "jpeg"

            /** JPEG JFIF image; Associated with Internet Explorer; Listed in ms775147(v=vs.85) - Progressive JPEG, initiated before global browser support for progressive JPEGs (Microsoft and Firefox). */
            val pjpeg = "pjpeg"

            /** Portable Network Graphics; Registered,[10] Defined in RFC 2083 */
            val png = "png"

            /** SVG vector image; Defined in SVG Tiny 1.2 Specification Appendix M */
            val svg = "svg+xml"

            /** example in documentation, Defined in RFC 4735 */
            val example = "example"

            /** GIMP image file */
            val xcf = "x-xcf"
        }
    }

    class AudioSubType {
        companion object {
            /** μ-law audio at 8 kHz, 1 channel; Defined in RFC 2046 */
            val basic = "basic"

            /** 24bit Linear PCM audio at 8–48 kHz, 1-N channels; Defined in RFC 3190 */
            val l24 = "L24"

            /** MP4 audio */
            val mp4 = "mp4"

            /** MP3 or other MPEG audio; Defined in RFC 3003 */
            val mpeg = "mpeg"

            /** Ogg Vorbis, Speex, Flac and other audio; Defined in RFC 5334 */
            val ogg = "ogg"

            /** Opus audio */
            val opus = "opus"

            /** Vorbis encoded audio; Defined in RFC 5215 */
            val vorbis = "vorbis"

            /** RealAudio; Documented in RealPlayer Help[9] */
            val real = "vnd.rn-realaudio"

            /** WAV audio; Defined in RFC 2361 */
            val wave = "vnd.wave"

            /** WebM open media format */
            val webm = "webm"

            /** example in documentation, Defined in RFC 4735 */
            val example = "example"

            /** .aac audio files */
            val xAac = "x-aac"

            /** Apple's CAF audio files */
            val xCaf = "x-caf"
        }
    }

    class VideoSubType {
        companion object {
            val mpeg = "mpeg"

            /** MP4 video; Defined in RFC 4337 */
            val mp4 = "mp4"

            /** Ogg Theora or other video (with audio); Defined in RFC 5334 */
            val ogg = "ogg"

            /** QuickTime video; Registered[12] */
            val quicktime = "quicktime"

            /** WebM Matroska-based open media format */
            val webm = "webm"

            /** Matroska open media format */
            val mkv = "x-matroska"

            /** Windows Media Video; Documented in Microsoft KB 288102 */
            val wmv = "x-ms-wmv"

            /** Flash video (FLV files) */
            val flv = "x-flv"

            /** example in documentation, Defined in RFC 4735 */
            val example = "example"
        }
    }

    class MessageSubType {
        companion object {
            /** Defined in RFC 2616 */
            val http = "http"

            /** IMDN Instant Message Disposition Notification; Defined in RFC 5438 */
            val imdn = "imdn+xml"

            /** Email; Defined in RFC 2045 and RFC 2046 */
            val partial = "partial"

            /** Email; EML files, MIME files, MHT files, MHTML files; Defined in RFC 2045 and RFC 2046 */
            val rfc822 = "rfc822"

            /** example in documentation, Defined in RFC 4735 */
            val example = "example"
        }
    }

    class MultipartSubType {
        companion object {
            val paramBoundary = "boundary"

            /** MIME Email; Defined in RFC 2045 and RFC 2046 */
            val mixed = "mixed"

            /** MIME Email; Defined in RFC 2045 and RFC 2046 */
            val alternative = "alternative"

            /** MIME Email; Defined in RFC 2387 and used by MHTML (HTML mail) */
            val related = "related"

            /** MIME Webform; Defined in RFC 2388 */
            val formData = "form-data"

            /** Defined in RFC 1847 */
            val signed = "signed"

            /** Defined in RFC 1847 */
            val encrypted = "encrypted"

            /** example in documentation, Defined in RFC 4735 */
            val example = "example"
        }
    }

    companion object {
        val applicationOctetStream: ConnContentType
            get() = ConnContentType(
                mainType = MainType.application,
                supType = ApplicationSubType.octetStream
            )
        val plainTextUtf8: ConnContentType
            get() = ConnContentType(
                mainType = MainType.text,
                supType = TextSubType.plain,
                parameters = mapOf(TextSubType.paramCharset to Charsets.UTF_8.name())
            )
        fun multipart(supType: String, boundary: String): ConnContentType =
            ConnContentType(
                mainType = MainType.multipart,
                supType = supType,
                parameters = mapOf(MultipartSubType.paramBoundary to boundary)
            )
        fun multipartFormData(boundary: String): ConnContentType =
            ConnContentType.multipart(
                supType = MultipartSubType.formData,
                boundary = boundary
            )
    }

}

val String.httpContentType: ConnContentType?
    get() {
        return when (this.lowercase()) {
            "txt" -> ConnContentType(mainType = ConnContentType.MainType.text, supType = ConnContentType.TextSubType.plain)
            "csv" -> ConnContentType(mainType = ConnContentType.MainType.text, supType = ConnContentType.TextSubType.csv)
            "html" -> ConnContentType(mainType = ConnContentType.MainType.text, supType = ConnContentType.TextSubType.html)
            "htm" -> ConnContentType(mainType = ConnContentType.MainType.text, supType = ConnContentType.TextSubType.html)
            "jpg" -> ConnContentType(mainType = ConnContentType.MainType.image, supType = ConnContentType.ImageSubType.jpeg)
            "jpeg" -> ConnContentType(mainType = ConnContentType.MainType.image, supType = ConnContentType.ImageSubType.jpeg)
            "png" -> ConnContentType(mainType = ConnContentType.MainType.image, supType = ConnContentType.ImageSubType.png)
            "gif" -> ConnContentType(mainType = ConnContentType.MainType.image, supType = ConnContentType.ImageSubType.gif)
            "mp3" -> ConnContentType(mainType = ConnContentType.MainType.audio, supType = ConnContentType.AudioSubType.mpeg)
            "mp4" -> ConnContentType(mainType = ConnContentType.MainType.video, supType = ConnContentType.VideoSubType.mp4)
            "mpeg" -> ConnContentType(mainType = ConnContentType.MainType.video, supType = ConnContentType.VideoSubType.mpeg)
            "mpg" -> ConnContentType(mainType = ConnContentType.MainType.video, supType = ConnContentType.VideoSubType.mpeg)
            "mov" -> ConnContentType(mainType = ConnContentType.MainType.video, supType = ConnContentType.VideoSubType.quicktime)
            "xml" -> ConnContentType(mainType = ConnContentType.MainType.application, supType = ConnContentType.ApplicationSubType.xml)
            "pdf" -> ConnContentType(mainType = ConnContentType.MainType.application, supType = ConnContentType.ApplicationSubType.pdf)
            "gz" -> ConnContentType(mainType = ConnContentType.MainType.application, supType = ConnContentType.ApplicationSubType.gzip)
            "zip" -> ConnContentType(mainType = ConnContentType.MainType.application, supType = ConnContentType.ApplicationSubType.zip)
            "7z" -> ConnContentType(mainType = ConnContentType.MainType.application, supType = ConnContentType.ApplicationSubType.x7z)
            else -> null
        }
    }

/** Get file Content-Type from its extension */
val File.httpContentType: ConnContentType?
    get() = this.extension.httpContentType
