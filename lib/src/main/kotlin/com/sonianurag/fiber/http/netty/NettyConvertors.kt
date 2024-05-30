package com.sonianurag.fiber.http.netty

import com.sonianurag.fiber.buffer.Buf
import com.sonianurag.fiber.http.*
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*

internal fun Buf.toNettyByteBuf(ctx: ChannelHandlerContext): ByteBuf {
    val buffer = ctx.alloc().ioBuffer(this.size)
    buffer.writerIndex(this.size)
    this.copyTo(buffer.nioBuffer())
    return buffer
}

internal fun HttpVersion.toVersion(): Version {
    return when (this) {
        HttpVersion.HTTP_1_1 -> Version.Http11
        else -> throw IllegalArgumentException("Unexpected http version: ${this.text()}")
    }
}

internal fun Version.toNettyVersion(): HttpVersion {
    return when (this) {
        Version.Http11 -> HttpVersion.HTTP_1_1
        else -> throw IllegalArgumentException("Http version not supported")
    }
}

internal fun HttpRequest.toRequest(body: Body): Request {
    return object : Request {
        override val version: Version = this@toRequest.protocolVersion().toVersion()
        override val method: Method = this@toRequest.method().name().toHttpMethod()
        override val uri: String = this@toRequest.uri()
        override val headers: Headers =
            Headers().also { headers ->
                this@toRequest.headers().forEach { entry -> headers.add(entry.key, entry.value) }
            }
        override val body: Body = body
    }
}

internal fun Response.toNettyResponse(): HttpResponse {
    val nettyResponse =
        DefaultHttpResponse(
            this.version.toNettyVersion(),
            HttpResponseStatus.valueOf(this.statusCode.code)
        )
    this.headers.forEach { entry -> nettyResponse.headers().add(entry.key, entry.value) }
    return nettyResponse
}

internal fun Response.toFullNettyResponse(content: ByteBuf): HttpResponse {
    val nettyResponse =
        DefaultFullHttpResponse(
            this.version.toNettyVersion(),
            HttpResponseStatus.valueOf(this.statusCode.code),
            content
        )
    this.headers.forEach { entry -> nettyResponse.headers().add(entry.key, entry.value) }
    return nettyResponse
}
