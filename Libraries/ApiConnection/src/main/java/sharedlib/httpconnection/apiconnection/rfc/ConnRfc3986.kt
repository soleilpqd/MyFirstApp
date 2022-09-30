package sharedlib.httpconnection.apiconnection

import kotlin.text.StringBuilder

/** HTTP characters categories from Rfc-3986 */
object ConnUrlCharacterCategory {
    /**
     * Unreversed characters from https://tools.ietf.org/html/rfc1808 (obsoleted)
     */
    const val URLUnreservedSafeExtraCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~$-_.+!*'(),"
    /**
     * Reversed characters from https://tools.ietf.org/html/rfc1808 (obsoleted)
     */
    const val URLReservedCharacters = ";/?:@&="

    const val AlphaHighCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    const val AlphaLowCharacters = "abcdefghijklmnopqrstuvwxyz"
    const val DigitCharacters = "0123456789"
    const val AlphaNumericCharacters = AlphaHighCharacters + AlphaLowCharacters + DigitCharacters
    /**
     * Unreversed characters from https://tools.ietf.org/html/rfc3986
     */
    const val UnreservedCharacters = "-._~" + AlphaNumericCharacters
    const val GenDelimsCharacters = ":/?#[]@"
    const val SubDelimsCharacters = "!$&'()*+;="
    /**
     * Reversed characters from https://tools.ietf.org/html/rfc3986
     */
    const val ReservedCharacters = GenDelimsCharacters + SubDelimsCharacters
    /**
     * Valid characters for URI scheme from https://tools.ietf.org/html/rfc3986, not allow other characters
     */
    const val SchemeCharacters = "+-." + AlphaNumericCharacters
    /**
     * Non-required escaping characters for URI UserInfo from https://tools.ietf.org/html/rfc3986.
     * Password is not allowed in URI (but I still use `UnreservedCharacters` to URL encode password because NSURL still supports).
     */
    const val UserInfoCharacters = UnreservedCharacters + SubDelimsCharacters + ":"
    /**
     * Non-required escaping characters for URI Host Domain Name from https://tools.ietf.org/html/rfc3986.
     */
    const val DomainNamCharacters = UnreservedCharacters + SubDelimsCharacters
    /**
     * Only digits (~ UInt)
     */
    const val PortCharacters = DigitCharacters
    /**
     * Non-required escaping characters for URI Path from https://tools.ietf.org/html/rfc3986.
     */
    const val PathComponentCharacters = UnreservedCharacters + SubDelimsCharacters + "@:"
    /**
     * Non-required escaping characters for URI Query from https://tools.ietf.org/html/rfc3986.
     */
    const val QueryCharacters = PathComponentCharacters + "/?"
    /**
     * Non-required escaping characters for URI Fragment from https://tools.ietf.org/html/rfc3986.
     */
    const val FragmentCharacters = QueryCharacters

    const val HexaCharacters = "ABCDEFabcdef" + DigitCharacters

}

fun String.removeCharactersIn(target: String): String {
    var result = this
    for (char in target) {
        result = result.replace(char.toString(), "")
    }
    return result
}

fun String.appendCharactersIn(target: String): String {
    var result = StringBuilder(this)
    for (char in target) {
        if (!result.contains(char)) {
            result.append(char)
        }
    }
    return result.toString()
}