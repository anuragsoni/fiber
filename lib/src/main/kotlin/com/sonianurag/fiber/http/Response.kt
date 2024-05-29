package com.sonianurag.fiber.http

interface Response {
    val version: Version
    val statusCode: StatusCode
    val headers: Headers
    val body: Body

    companion object {
        fun create(
            body: Body = Body.empty,
            version: Version = Version.Http11,
            statusCode: StatusCode = StatusCode.OK,
            headers: Headers = Headers(),
        ): Response {
            return object : Response {
                override val statusCode: StatusCode = statusCode
                override val headers: Headers = headers
                override val version: Version = version
                override val body: Body = body
            }
        }
    }
}
