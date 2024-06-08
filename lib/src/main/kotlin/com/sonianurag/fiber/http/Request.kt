package com.sonianurag.fiber.http

/** Interface representing an HTTP request. */
interface Request {
    /** HTTP version associated with the request. */
    val version: Version

    /** HTTP method associated with the request. */
    val method: Method

    /** URI associated with the request. */
    val uri: String

    /** Header map associated with the request. */
    val headers: Headers

    /** Body associated with the request. */
    val body: Body

    companion object {
        fun create(
            version: Version,
            method: Method,
            uri: String,
            headers: Headers = Headers(),
            body: Body = Body.empty
        ): Request {
            return object : Request {
                override val version: Version = version
                override val method: Method = method
                override val uri: String = uri
                override val headers: Headers = headers
                override val body: Body = body
            }
        }
    }
}
