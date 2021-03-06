package vn.payme.sdk.api


/**
 * Builder class for constructing a query for downloading a font.
 */
internal class QueryBuilder(val familyName: String,
                            val width: Float? = null,
                            val weight: Int? = null,
                            val italic: Float? = null,
                            val besteffort: Boolean? = null) {

    fun build(): String {
        if (weight == null && width == null && italic == null && besteffort == null) {
            return familyName
        }
        val builder = StringBuilder()
        builder.append("name=").append(familyName)
        weight?.let { builder.append("&weight=").append(weight) }
        width?.let { builder.append("&width=").append(width) }
        italic?.let { builder.append("&italic=").append(italic) }
        besteffort?.let { builder.append("&besteffort=").append(besteffort) }
        return builder.toString()
    }
}