package com.sonianurag.fiber.http

import com.sonianurag.fiber.http.netty.ChannelStatsHandler
import com.sonianurag.fiber.http.netty.Http1ServerInitializer
import com.sonianurag.fiber.http.netty.SharedServerStatistics
import com.sonianurag.fiber.net.Address
import com.sonianurag.fiber.net.Server
import com.sonianurag.fiber.net.toSocketAddress
import com.sonianurag.fiber.ssl.SslContext
import com.sonianurag.fiber.transport.NettyTransport
import com.sonianurag.fiber.transport.socketChannelForAddress
import com.sonianurag.fiber.utilities.coAwait
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import java.net.BindException
import java.net.SocketAddress
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.slf4j.LoggerFactory

object Http {
    suspend fun createServer(
        whereToListen: Address,
        sslContext: SslContext? = null,
        backlog: Int = 128,
        workerThreads: Int = max(1, Runtime.getRuntime().availableProcessors()),
        tcpNoDelay: Boolean = true,
        connectTimeout: Duration? = null,
        keepAlive: Boolean = true,
        receiveBufferSize: Int = 65536,
        sendBufferSize: Int = 65536,
        service: Service
    ): Server {
        require(backlog > 0) { "backlog must be > 0" }
        require(receiveBufferSize > 0) { "receiveBufferSize must be > 0" }
        require(sendBufferSize > 0) { "sendBufferSize must be > 0" }
        require(workerThreads >= 0) { "workerThreads must either be >= 0" }
        val logger = LoggerFactory.getLogger("fiber/tcp")
        val bootstrap = ServerBootstrap()
        val transport = NettyTransport.default()
        logger.trace("Using transport {}", transport::class.java.name)
        val bossGroup = transport.eventLoopGroup(workerThreads, true, "fiber/server")
        bootstrap.group(bossGroup)
        bootstrap.option(ChannelOption.SO_BACKLOG, backlog)
        bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
        bootstrap.childOption(ChannelOption.TCP_NODELAY, tcpNoDelay)
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, keepAlive)
        connectTimeout?.let {
            bootstrap.option(
                ChannelOption.CONNECT_TIMEOUT_MILLIS,
                it.toInt(DurationUnit.MILLISECONDS)
            )
        }
        bootstrap.childOption(ChannelOption.SO_RCVBUF, receiveBufferSize)
        bootstrap.childOption(ChannelOption.SO_SNDBUF, sendBufferSize)
        val sharedStats = SharedServerStatistics()
        bootstrap.channelFactory(transport.socketChannelForAddress(whereToListen))

        bootstrap.childHandler(
            object : ChannelInitializer<Channel>() {
                override fun initChannel(ch: Channel) {
                    val pipeline = ch.pipeline()

                    if (sslContext != null) {
                        pipeline.addLast(sslContext.nettyContext.newHandler(ch.alloc()))
                    }

                    pipeline.addFirst(ChannelStatsHandler(sharedStats))
                    pipeline.addLast(Http1ServerInitializer(service))
                }
            }
        )
        val bindResult = bootstrap.bind(whereToListen.toSocketAddress()).also { it.coAwait() }
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
