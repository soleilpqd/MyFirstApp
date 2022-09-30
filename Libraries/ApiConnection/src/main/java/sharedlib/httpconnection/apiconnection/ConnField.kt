package sharedlib.httpconnection.apiconnection

/**
 * Specify Class property is request parameter
 *
 * `@ConnField("param_1") var param1: String`
 *
 * Optional: leave it empty if property and parameter name are the same
 * `@ConnField("") var param1: String`
 */
@Target(AnnotationTarget.PROPERTY)
annotation class ConnField(
    /** Field name */
    val name: String = "",
    /** File name for multipart/form-data */
    val fileName: String = ""
)
