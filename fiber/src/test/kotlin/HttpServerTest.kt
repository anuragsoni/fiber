package com.sonianurag.fiber

import com.sonianurag.fiber.netty.utilities.coAwait
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioDomainSocketChannel
import io.netty.handler.codec.http.*
import java.net.InetSocketAddress
import java.net.URI
import java.net.UnixDomainSocketAddress
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import kotlin.io.path.deleteIfExists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest

class HttpServerTest {
  @Test
  fun `can respond to get requests`() {
    runTest {
      Http.createServer(InetSocketAddress("localhost", 0)) {
          when (it.method) {
            Method.GET -> respond("Hello World".toBody())
            else -> respond(statusCode = StatusCode.METHOD_NOT_ALLOWED)
          }
        }
        .use { server ->
          HttpClient.newHttpClient().use { client ->
            val request =
              HttpRequest.newBuilder().uri(URI.create("http:/${server.listeningOn}")).build()
            val response = client.send(request, BodyHandlers.ofString())
            assertEquals(expected = "Hello World", actual = response.body())
          }
        }
    }
  }

  @Test
  fun `can listen on unix domain sockets`() {
    runTest {
      val temp = kotlin.io.path.createTempFile().also { it.deleteIfExists() }
      Http.createServer(UnixDomainSocketAddress.of(temp)) { respond("Hello World".toBody()) }
        .use {
          val content =
            NioEventLoopGroup().use { group ->
              val response = CompletableDeferred<String>()

              val clientInitializer =
                object : ChannelInitializer<Channel>() {
                  override fun initChannel(ch: Channel) {
                    val pipeline = ch.pipeline()
                    pipeline.addLast(HttpClientCodec())
                    pipeline.addLast(HttpObjectAggregator(Int.MAX_VALUE))
                    pipeline.addLast(
                      object : SimpleChannelInboundHandler<FullHttpResponse>() {
                        override fun channelRead0(
                          ctx: ChannelHandlerContext,
                          msg: FullHttpResponse,
                        ) {
                          response.complete(NettyBuf(msg.content()).decodeToString())
                          ctx.close()
                        }
                      }
                    )
                  }
                }

              val bootstrap = Bootstrap()
              bootstrap
                .group(group)
                .channel(NioDomainSocketChannel::class.java)
                .handler(clientInitializer)

              val fut = bootstrap.connect(UnixDomainSocketAddress.of(temp))
              val chan = fut.sync().channel()

              val request = DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/")
              chan.writeAndFlush(request).coAwait()
              response.await().also { chan.close() }
            }
          assertEquals(expected = "Hello World", actual = content)
        }
    }
  }

  @Test
  fun `can respond to request with bodies`() {
    runTest {
      Http.createServer(
          InetSocketAddress("localhost", 0),
          config = HttpServerOptions(preferNativeTransport = false),
        ) {
          when (it.method) {
            Method.POST -> respond(body = it.body)
            else -> respond(statusCode = StatusCode.METHOD_NOT_ALLOWED)
          }
        }
        .use { server ->
          HttpClient.newHttpClient().use { client ->
            val request =
              HttpRequest.newBuilder()
                .uri(URI.create("http:/${server.listeningOn}"))
                .method("POST", BodyPublishers.ofString("Hello World"))
                .build()
            val response = client.send(request, BodyHandlers.ofString())
            assertEquals(expected = "Hello World", actual = response.body())
          }
        }
    }
  }
}
