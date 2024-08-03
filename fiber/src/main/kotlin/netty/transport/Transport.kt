package com.sonianurag.fiber.netty.transport

import io.netty.channel.Channel
import io.netty.channel.ChannelFactory
import io.netty.channel.EventLoopGroup
import io.netty.channel.ServerChannel
import io.netty.channel.epoll.*
import io.netty.channel.kqueue.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioDomainSocketChannel
import io.netty.channel.socket.nio.NioServerDomainSocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import java.util.concurrent.ThreadFactory

internal interface Transport {
  companion object {
    fun default(): Transport {
      return when {
        EpollTransport.isAvailable() -> EpollTransport
        KqueueTransport.isAvailable() -> KqueueTransport
        else -> NioTransport
      }
    }
  }

  fun domainSocketsAvailable(): Boolean

  fun isAvailable(): Boolean

  fun eventLoopGroup(
    threadCount: Int,
    threadFactory: ThreadFactory,
    ioRatio: Int = 50,
  ): EventLoopGroup

  fun channelFactory(domainSocket: Boolean): ChannelFactory<out Channel>

  fun serverChannelFactory(domainSocket: Boolean): ChannelFactory<out ServerChannel>
}

internal object KqueueTransport : Transport {
  override fun isAvailable(): Boolean {
    return KQueue.isAvailable()
  }

  override fun domainSocketsAvailable(): Boolean {
    return true
  }

  override fun eventLoopGroup(
    threadCount: Int,
    threadFactory: ThreadFactory,
    ioRatio: Int,
  ): EventLoopGroup {
    return KQueueEventLoopGroup(threadCount, threadFactory).also { it.setIoRatio(ioRatio) }
  }

  override fun channelFactory(domainSocket: Boolean): ChannelFactory<out Channel> {
    return if (domainSocket) {
      ChannelFactory { KQueueDomainSocketChannel() }
    } else {
      ChannelFactory { KQueueSocketChannel() }
    }
  }

  override fun serverChannelFactory(domainSocket: Boolean): ChannelFactory<out ServerChannel> {
    return if (domainSocket) {
      ChannelFactory { KQueueServerDomainSocketChannel() }
    } else {
      ChannelFactory { KQueueServerSocketChannel() }
    }
  }
}

internal object EpollTransport : Transport {
  override fun domainSocketsAvailable(): Boolean {
    return true
  }

  override fun isAvailable(): Boolean {
    return Epoll.isAvailable()
  }

  override fun eventLoopGroup(
    threadCount: Int,
    threadFactory: ThreadFactory,
    ioRatio: Int,
  ): EventLoopGroup {
    return EpollEventLoopGroup(threadCount, threadFactory).also { it.setIoRatio(ioRatio) }
  }

  override fun channelFactory(domainSocket: Boolean): ChannelFactory<out Channel> {
    return if (domainSocket) {
      ChannelFactory { EpollDomainSocketChannel() }
    } else {
      ChannelFactory { EpollSocketChannel() }
    }
  }

  override fun serverChannelFactory(domainSocket: Boolean): ChannelFactory<out ServerChannel> {
    return if (domainSocket) {
      ChannelFactory { EpollServerDomainSocketChannel() }
    } else {
      ChannelFactory { EpollServerSocketChannel() }
    }
  }
}

internal object NioTransport : Transport {
  override fun domainSocketsAvailable(): Boolean {
    return true
  }

  override fun isAvailable(): Boolean {
    return true
  }

  override fun eventLoopGroup(
    threadCount: Int,
    threadFactory: ThreadFactory,
    ioRatio: Int,
  ): EventLoopGroup {
    return NioEventLoopGroup(threadCount, threadFactory).also { it.setIoRatio(ioRatio) }
  }

  override fun channelFactory(domainSocket: Boolean): ChannelFactory<out Channel> {
    return if (domainSocket) {
      ChannelFactory { NioDomainSocketChannel() }
    } else {
      ChannelFactory { NioSocketChannel() }
    }
  }

  override fun serverChannelFactory(domainSocket: Boolean): ChannelFactory<out ServerChannel> {
    return if (domainSocket) {
      ChannelFactory { NioServerDomainSocketChannel() }
    } else {
      ChannelFactory { NioServerSocketChannel() }
    }
  }
}
