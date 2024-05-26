package com.sonianurag.fiber.http

interface Request {
    val version: Version
    val method: Method
    val uri: String
    val headers: Headers
    val body: Body
}

fun Request.respond(body: Body, statusCode: StatusCode = StatusCode.OK, headers: Headers = Headers()): Response {
    return object : Response {
        override val statusCode: StatusCode = statusCode
        override val headers: Headers = headers
        override val version: Version = this@respond.version
        override val body: Body = body
    }
}