package sharedlib.httpconnection.apiconnection

import java.nio.charset.Charset
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.text.CharCategory
import kotlin.text.StringBuilder

/** Setting for ConnUrlEncoder */
data class ConnUrlEncoderSetting(
    val charset: Charset = ConnUrlEncoder.defaultSetting.charset,
    val spaceAsPlus: Boolean = ConnUrlEncoder.defaultSetting.spaceAsPlus,
    val lowerCase: Boolean = ConnUrlEncoder.defaultSetting.lowerCase,
    val unreservedChars: String = ConnUrlEncoder.defaultSetting.unreservedChars,
    val unreservedCharCat: CharCategory? = ConnUrlEncoder.defaultSetting.unreservedCharCat
) {}

/**
 * URL Encoder (RFC 3986)
 */
class ConnUrlEncoder(
    var charset: Charset = defaultSetting.charset
) {

    companion object {
        var defaultSetting = ConnUrlEncoderSetting(
            charset = Charsets.UTF_8,
            spaceAsPlus = false,
            lowerCase = false,
            unreservedChars = ConnUrlCharacterCategory.QueryCharacters.removeCharactersIn("?/&="),
            unreservedCharCat = null
        )
    }

    /**
     * `true` to encode " " to "+" instead of "%20" and decode vice versa.
     */
    var spaceAsPlus = defaultSetting.spaceAsPlus

    /**
     * Encode to lower case
     */
    var lowerCase = defaultSetting.lowerCase

    /**
     * Unreserved ASCII characters (not to encode)
     */
    var unreservedChars: String = defaultSetting.unreservedChars
    /**
     * Unreserved ASCII characters (not to encode)
     */
    var unreservedCharCat: CharCategory? = defaultSetting.unreservedCharCat

    private fun isAscii(code: Byte): Boolean {
        return code in 32..126
    }

    private fun convertHex(hex: String): Byte? {
        return try {
            hex.toInt(16).toByte()
        } catch (exception: Exception) {
            null
        }
    }

    /**
     * URL encode
     */
    fun encode(text: String): String {
        val chars = text.toCharArray()
        var result = StringBuilder("")
        val hexFormat = if (lowerCase) { "%%%02x" } else { "%%%02X" }
        for (char in chars) {
            val charStr = char.toString()
            val bytes = charStr.toByteArray(charset = this.charset)
            var shouldEscape = true
            if (bytes.count() == 1 && isAscii(bytes.first())) {
                if (unreservedChars.contains(char)) {
                    shouldEscape = false
                }
                if (unreservedCharCat != null && unreservedCharCat!!.contains(char)) {
                    shouldEscape = false
                }
            }
            if (shouldEscape) {
                if (spaceAsPlus && char == ' ') {
                    result.append("+")
                } else {
                    for (byte in bytes) {
                        result.append(hexFormat.format(byte))
                    }
                }
            } else {
                result.append(char)
            }
        }
        return result.toString()
    }

    /**
     * URL decode
     */
    fun decode(text: String): String? {
        var isEncoding = false
        var currentWordBytes = mutableListOf<Byte>()
        val chars = text.toCharArray()
        var hex = StringBuilder("")
        var result = StringBuilder("")

        val flushCurrentWord: () -> Unit = {
            if (currentWordBytes.isNotEmpty()) {
                val byteArr = currentWordBytes.toByteArray()
                result.append(String(byteArr))
                currentWordBytes.clear()
            }
        }

        val handleCurChar: (Char) -> Unit = { char ->
            if (char == '%') {
                isEncoding = true
            } else {
                flushCurrentWord()
                if (char == '+' && spaceAsPlus) {
                    result.append(" ")
                } else {
                    result.append(char)
                }
            }
        }
        for (char in chars) {
            val charStr = char.toString()
            val bytes = charStr.toByteArray(charset = this.charset)
            if (bytes.count() == 1 && isAscii(bytes.first())) {
                if (isEncoding) {
                    if (ConnUrlCharacterCategory.HexaCharacters.contains(char) && hex.count() < 2) {
                        hex.append(char)
                    } else {
                        if (hex.count() < 2) {
                            return null
                        }
                        val byte = convertHex(hex.toString())
                        hex.clear()
                        if (byte == null) {
                            return null
                        }
                        currentWordBytes.add(byte)
                        isEncoding = false
                        handleCurChar(char)
                    }
                } else {
                    handleCurChar(char)
                }
            } else {
                return null
            }
        }
        if (hex.isNotEmpty()) {
            if (hex.count() < 2) {
                return null
            }
            val byte = convertHex(hex.toString())
            hex.clear()
            if (byte == null) {
                return null
            }
            currentWordBytes.add(byte)
        }
        flushCurrentWord()
        return result.toString()
    }

    /**
     * Build `x-www-form-data` from `Map`
     */
    fun encode(map: Map<*, *>): String {
        val result = StringBuilder()
        map.forEach { (key, value) ->
            val keyName = encode(text = key.toString())
            val valName = encode(text = value.toString())
            if (result.isNotEmpty()) {
                result.append("&")
            }
            result.append("${keyName}=${valName}")
        }
        return result.toString()
    }

    /**
     * Build `x-www-form-data` from `data` which its properties have `ConnField` annotation.
     * Use property `toString()` as value
     */
    fun encode(data: Any): String {
        if (data is String) {
            return encode(text = data)
        }
        if (data is Map<*, *>) {
            return encode(map = data)
        }
        val result = StringBuilder()
        val cls = data::class
        for (prop in cls.memberProperties) {
            if (prop.hasAnnotation<ConnField>()) {
                var paramName = prop.findAnnotation<ConnField>()?.name
                val propValue = (prop as? KProperty1<Any, *>)?.get(data)
                if (paramName != null && propValue != null) {
                    var paramValue = propValue.toString()
                    if (paramName.isEmpty()) {
                        paramName = prop.name
                    }
                    paramName = encode(text = paramName)
                    paramValue = encode(text = paramValue)
                    if (result.isNotEmpty()) {
                        result.append("&")
                    }
                    result.append("${paramName}=${paramValue}")
                }
            }
        }
        return result.toString()
    }

}

/** Set default settings for ConnUrlEncoder of current session */
fun ConnSession.setUrlEncoderSettings(settings: ConnUrlEncoderSetting) {
    this.setComponentSettings(settings)
}

/** Create ConnUrlEncoder with settings of current session */
fun ConnSession.makeUrlEncoder(
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
): ConnUrlEncoder {
    val settings = this.getComponentSettings(ConnUrlEncoderSetting::class.java)
    val result = ConnUrlEncoder(
        charset = charset ?: this.charSet
    )
    result.spaceAsPlus = spaceAsPlus ?: settings?.spaceAsPlus ?: ConnUrlEncoder.defaultSetting.spaceAsPlus
    result.lowerCase = lowerCase ?: settings?.lowerCase ?: ConnUrlEncoder.defaultSetting.lowerCase
    result.unreservedChars = unreservedChars ?: settings?.unreservedChars ?: ConnUrlEncoder.defaultSetting.unreservedChars
    result.unreservedCharCat = unreservedCharCat ?: settings?.unreservedCharCat ?: ConnUrlEncoder.defaultSetting.unreservedCharCat
    return result
}
