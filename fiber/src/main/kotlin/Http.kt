package com.sonianurag.fiber

import com.sonianurag.fiber.netty.ChannelStatsHandler
import com.sonianurag.fiber.netty.SharedServerStatistics
import com.sonianurag.fiber.netty.handlers.http1.Http1ServerInitializer
import com.sonianurag.fiber.netty.transport.NioTransport
import com.sonianurag.fiber.netty.transport.Transport
import com.sonianurag.fiber.netty.utilities.coAwait
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.util.concurrent.DefaultThreadFactory
import java.net.BindException
import java.net.SocketAddress
import java.net.UnixDomainSocketAddress
import kotlin.time.DurationUnit
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.slf4j.LoggerFactory

object Http {
  suspend fun createServer(
    whereToListen: SocketAddress,
    config: HttpServerOptions = HttpServerOptions(),
    service: suspend ServerContext.(Request) -> Response,
  ): Server {
    val logger = LoggerFactory.getLogger("fiber/http")
    val bootstrap = ServerBootstrap()
    val transport =
      if (config.preferNativeTransport) {
        Transport.default()
      } else {
        NioTransport
      }

    logger.trace("Using transport {}", transport::class.java)

    val bossGroup =
      transport.eventLoopGroup(config.workerThreads, DefaultThreadFactory("fiber/server", true))

    bootstrap
      .group(bossGroup)
      .option(ChannelOption.SO_BACKLOG, config.backlog)
      .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
      .childOption(ChannelOption.TCP_NODELAY, config.tcpNoDelay)
      .childOption(ChannelOption.SO_KEEPALIVE, config.keepAlive)
      .childOption(ChannelOption.SO_RCVBUF, config.receiveBufferSize)
      .childOption(ChannelOption.SO_SNDBUF, config.sendBufferSize)
      .also { builder ->
        config.connectTimeout?.let {
          builder.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, it.toInt(DurationUnit.MILLISECONDS))
        }
      }

    val sharedStats = SharedServerStatistics()
    bootstrap.channelFactory(
      transport.serverChannelFactory(whereToListen is UnixDomainSocketAddress)
    )

    bootstrap.childHandler(
      object : ChannelInitializer<Channel>() {
        override fun initChannel(ch: Channel) {
          val pipeline = ch.pipeline()
          if (config.sslContext != null) {
            pipeline.addLast(config.sslContext.nettyContext.newHandler(ch.alloc()))
          }
          pipeline.addFirst(ChannelStatsHandler(sharedStats))
          pipeline.addLast(Http1ServerInitializer(service))
        }
      }
    )
    val bindResult = bootstrap.bind(whereToListen).also { it.coAwait() }
    if (!bindResult.isSuccess) {
      throw BindException("Failed to bind to $whereToListen: ${bindResult.cause().message}")
    }
    return object : Server {

      private val isClosed = CompletableDeferred<Unit>()

      override fun close() {
        bossGroup.shutdownGracefully()
        bindResult.channel().close().addListener { isClosed.complete(Unit) }
      }

      override val listeningOn: SocketAddress = bindResult.channel().localAddress()

      override fun closed(): Deferred<Unit> {
        return isClosed
      }

      override fun numberOfConnections(): Long {
        return sharedStats.totalConnections()
      }
    }
  }
}
