package com.sonianurag.fiber

import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerRequest
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.net.UnixDomainSocketAddress

interface ServerContext {
  fun Request.remoteAddress(): SocketAddress?

  val Request.isSSL: Boolean

  fun respond(
    body: Body,
    statusCode: StatusCode = StatusCode.OK,
    headers: Headers = emptyHeaders(),
  ): Response
}

internal interface ServerContextInternal : ServerContext {
  val vertx: Vertx
  val vertxRequest: HttpServerRequest
  val request: Request

  override val Request.isSSL: Boolean
    get() = vertxRequest.isSSL

  override fun respond(body: Body, statusCode: StatusCode, headers: Headers): Response {
    return object : Response {
      override val statusCode: StatusCode = StatusCode.OK
      override val headers: Headers = emptyHeaders()
      override val version: Version = request.version
      override val body: Body = body
    }
  }
}

internal class VertxServerContext(
  override val vertxRequest: HttpServerRequest,
  override val vertx: Vertx,
  override val request: Request,
) : ServerContextInternal {
  private val remoteAddress: SocketAddress? =
    if (vertxRequest.remoteAddress().isInetSocket) {
      InetSocketAddress(vertxRequest.remoteAddress().host(), vertxRequest.remoteAddress().port())
    } else if (vertxRequest.remoteAddress().isDomainSocket) {
      UnixDomainSocketAddress.of(vertxRequest.remoteAddress().path())
    } else {
      null
    }

  override fun Request.remoteAddress(): SocketAddress? {
    return remoteAddress
  }
}
