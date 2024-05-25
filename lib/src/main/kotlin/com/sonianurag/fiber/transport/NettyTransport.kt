package com.sonianurag.fiber.transport

import io.netty.channel.ChannelFactory
import io.netty.channel.EventLoopGroup
import io.netty.channel.ServerChannel
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerDomainSocketChannel
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.kqueue.KQueue
import io.netty.channel.kqueue.KQueueEventLoopGroup
import io.netty.channel.kqueue.KQueueServerDomainSocketChannel
import io.netty.channel.kqueue.KQueueServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.util.concurrent.DefaultThreadFactory

interface NettyTransport {
    fun isAvailable(): Boolean

    val serverInetChannelFactory: ChannelFactory<out ServerChannel>
    val serverDomainChannelFactory: ChannelFactory<out ServerChannel>

    fun eventLoopGroup(threadCount: Int, daemon: Boolean, threadPoolName: String): EventLoopGroup

    companion object {
        fun default(): NettyTransport {
            return when {
                EpollTransport.isAvailable() -> EpollTransport
                KqueueTransport.isAvailable() -> KqueueTransport
                else -> NioTransport
            }
        }
    }
}

object EpollTransport : NettyTransport {
    override fun isAvailable(): Boolean {
        return Epoll.isAvailable()
    }

    override val serverInetChannelFactory: ChannelFactory<out ServerChannel> = ChannelFactory {
        EpollServerSocketChannel()
    }

    override val serverDomainChannelFactory: ChannelFactory<out ServerChannel> = ChannelFactory {
        EpollServerDomainSocketChannel()
    }

    override fun eventLoopGroup(
        threadCount: Int,
        daemon: Boolean,
        threadPoolName: String
    ): EventLoopGroup {
        val threadFactory = DefaultThreadFactory(threadPoolName, true)
        return EpollEventLoopGroup(threadCount, threadFactory)
    }
}

object KqueueTransport : NettyTransport {
    override fun isAvailable(): Boolean {
        return KQueue.isAvailable()
    }

    override val serverDomainChannelFactory: ChannelFactory<out ServerChannel> = ChannelFactory {
        KQueueServerDomainSocketChannel()
    }

    override val serverInetChannelFactory: ChannelFactory<out ServerChannel> = ChannelFactory {
        KQueueServerSocketChannel()
    }

    override fun eventLoopGroup(
        threadCount: Int,
        daemon: Boolean,
        threadPoolName: String
    ): EventLoopGroup {
        val threadFactory = DefaultThreadFactory(threadPoolName, true)
        return KQueueEventLoopGroup(threadCount, threadFactory)
    }
}

object NioTransport : NettyTransport {
    override fun isAvailable(): Boolean {
        return true
    }

    override val serverDomainChannelFactory: ChannelFactory<out ServerChannel> = ChannelFactory {
        NioServerSocketChannel()
    }

    override val serverInetChannelFactory: ChannelFactory<out ServerChannel> = ChannelFactory {
        NioServerSocketChannel()
    }

    override fun eventLoopGroup(
        threadCount: Int,
        daemon: Boolean,
        threadPoolName: String
    ): EventLoopGroup {
        val threadFactory = DefaultThreadFactory(threadPoolName, true)
        return NioEventLoopGroup(threadCount, threadFactory)
    }
}