package com.sonianurag.fiber.vertx

import com.sonianurag.fiber.*
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpVersion
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.net.UnixDomainSocketAddress

internal fun HttpVersion.toVersion(): Version =
  when (this) {
    HttpVersion.HTTP_1_1 -> Version.Http11
    HttpVersion.HTTP_2 -> Version.H2
    else -> throw IllegalStateException("Unexpected http version")
  }

internal fun HttpMethod.toMethod(): Method = this.name().toHttpMethod()

internal fun SocketAddress.toVertxAddress(): io.vertx.core.net.SocketAddress {
  return when (this) {
    is InetSocketAddress -> io.vertx.core.net.SocketAddress.inetSocketAddress(this)
    is UnixDomainSocketAddress ->
      io.vertx.core.net.SocketAddress.domainSocketAddress(this.path.toString())
    else -> throw IllegalStateException("Unexpected socket address")
  }
}

internal fun HttpServerRequest.toRequest(body: Body = Body.empty): Request {
  return object : Request {
    override val method: Method = this@toRequest.method().toMethod()
    override val version: Version = this@toRequest.version().toVersion()
    override val body: Body = body
    override val uri: String = this@toRequest.uri()
    override val headers: Headers =
      Headers.create().also {
        this@toRequest.headers().forEach { key, value -> it.add(key, value) }
      }
  }
}

internal fun Buf.toBuffer(): Buffer =
  when (this) {
    is VertxBuffer -> this.vertxBuf
    else -> {
      val byteArray = ByteArray(this.size)
      this.copyTo(byteArray, 0)
      Buffer.buffer(byteArray)
    }
  }
