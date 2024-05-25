package com.sonianurag.fiber.http

/** HTTP Version */
enum class Version {
    /** HTTP/1.0 */
    Http10,

    /** HTTP/1.1 */
    Http11,

    /** HTTP/2.0 */
    H2,

    /** HTTP/3.0 */
    H3;

    override fun toString(): String {
        return when (this) {
            Http11 -> "HTTP/1.1"
            Http10 -> "HTTP/1.0"
            H2 -> "HTTP/2.0"
            H3 -> "HTTP/3.0"
        }
    }
}