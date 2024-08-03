package com.sonianurag.fiber

import io.netty.channel.Channel
import java.net.SocketAddress

interface ServerContext {
  fun Request.remoteAddress(): SocketAddress?

  fun respond(
    body: Body = Body.empty,
    statusCode: StatusCode = StatusCode.OK,
    headers: Headers = emptyHeaders(),
  ): Response
}

internal interface ServerContextInternal : ServerContext {
  val nettyChannel: Channel
  val request: Request

  override fun respond(body: Body, statusCode: StatusCode, headers: Headers): Response {
    return object : Response {
      override val statusCode: StatusCode = StatusCode.OK
      override val headers: Headers = emptyHeaders()
      override val version: Version = request.version
      override val body: Body = body
    }
  }
}

internal class NettyHttp1ServerContext(
  override val request: Request,
  override val nettyChannel: Channel,
) : ServerContextInternal {
  private val remoteAddress: SocketAddress? = nettyChannel.remoteAddress()

  override fun Request.remoteAddress(): SocketAddress? {
    return remoteAddress
  }
}
