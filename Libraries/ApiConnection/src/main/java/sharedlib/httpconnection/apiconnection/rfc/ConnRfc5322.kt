package sharedlib.httpconnection.apiconnection

/** Data format for HTTP header line from Rfc-5322. */
data class ConnHeaderLine(
    val name: String,
    val body: String
){

    companion object {

        val mimeEspecialsCharacters = "()<>@,;:\"/[]?.="
        /** Max length of `body` */
        val maxHeaderLineLength: Int = 998
        /** Check `text` should be quoted inside `""` */
        fun shouldQuote(text: String): Boolean {
            for (char in text) {
                if (mimeEspecialsCharacters.contains(char) || char == ' ') {
                    return true
                }
            }
            return false
        }

    }

    /** Make full line. Return `null` if the length is over limit (`maxHeaderLineLength`). */
    val fullLine: String?
        get() {
            if (body.length > maxHeaderLineLength) {
                return null
            }
            return "${name}: ${body}"
        }

}
