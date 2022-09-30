package sharedlib.httpconnection.apiconnection

import java.nio.charset.Charset
import java.util.*

/**
 * Data class for Content-Disposition header field.
 * Use `ConnContentDisposition.make` or `ConnContentDisposition.formData` instead of default constructor.
 *
 * `Content-Disposition: type; name=field_name filename=file_name ...`
 */
class ConnContentDisposition(
    /** Field name */
    name: String,
    /** Field value */
    value: String,
    /** Field parameters */
    params: Map<String, String>
): ConnABNFHeaderLine(
    name = name,
    value = value,
    parameters = params
) {

    companion object {
        val paramName = "name"
        val paramFileName = "filename"
        val paramCreationDate = "creation-date"
        val paramModificationDate = "modification-date"
        val paramReadDate = "read-date"
        val paramSize = "size"

        fun make(
            type: String,
            name: String? = null,
            fileName: String? = null,
            creationDate: Date? = null,
            modificationDate: Date? = null,
            readDate: Date? = null,
            size: UInt? = null,
            charset: Charset = Charsets.UTF_8
        ): ConnContentDisposition {
            val encoder = ConnUrlEncoder(
                charset = charset
            )
            encoder.unreservedChars = ConnUrlCharacterCategory.UnreservedCharacters
            val params = mutableMapOf<String, String>()
            if (name != null && name.isNotEmpty()) {
                params[paramName] = name
            }
            if (fileName != null && fileName.isNotEmpty()) {
                params[paramFileName] = encoder.encode(text = fileName)
            }
            val dateIso = ConnDateTimeFormat.rfc1123
            if (creationDate != null) {
                params[paramCreationDate] = dateIso.format(creationDate)
            }
            if (modificationDate != null) {
                params[paramModificationDate] = dateIso.format(modificationDate)
            }
            if (readDate != null) {
                params[paramReadDate] = dateIso.format(readDate)
            }
            if (size != null) {
                params[paramSize] = "${size}"
            }
            return ConnContentDisposition(
                name = ConnRequestHeaderField.contentDisposition.rawValue,
                value = type,
                params = params
            )
        }

        /** Make ContentDisposition for multipart/form-data */
        fun formData(
            /** Field name */
            name: String,
            /** File name */
            fileName: String?,
            /** Charset */
            charset: Charset = Charsets.UTF_8
        ): ConnContentDisposition {
            return make(
                name = name,
                fileName = fileName,
                type = ConnContentType.MultipartSubType.formData,
                charset = charset
            )
        }
    }

}