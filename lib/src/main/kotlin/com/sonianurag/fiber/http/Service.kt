package com.sonianurag.fiber.http

import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface ServiceContext {
    val logger: Logger
}

/**
 * Service represents an operation that takes an HTTP Request and responds with an HTTP Response.
 *
 * A service can represent both servers and clients. When running as a server it receives a request,
 * and when running as a client a service will forward the request to an upstream service.
 */
class Service(val name: String, handler: suspend ServiceContext.(Request) -> Response) {
    private var userHandler = handler
    private val serviceContext =
        object : ServiceContext {
            override val logger: Logger = LoggerFactory.getLogger(name)
        }

    suspend fun run(request: Request): Response {
        return with(serviceContext) { userHandler(request) }
    }
}
