package com.sonianurag.fiber.http

interface Response {
    val version: Version
    val statusCode: StatusCode
    val headers: Headers
    val body: Body
}
