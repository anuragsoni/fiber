package com.sonianurag.fiber.vertx

import io.netty.handler.codec.http.HttpHeaderValues
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpServerRequest

fun HttpServerRequest.hasBody(): Boolean {
  val contentLength = this.getHeader(HttpHeaders.CONTENT_LENGTH)?.toIntOrNull()
  return contentLength != null ||
    this.headers().contains(HttpHeaders.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED, false)
}
