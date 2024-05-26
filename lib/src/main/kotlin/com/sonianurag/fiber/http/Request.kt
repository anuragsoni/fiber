package com.sonianurag.fiber.http

interface Request {
    val version: Version
    val method: Method
    val uri: String
    val headers: Headers
    val body: Body
}