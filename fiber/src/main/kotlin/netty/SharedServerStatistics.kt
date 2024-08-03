package com.sonianurag.fiber.netty

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import java.util.concurrent.atomic.LongAdder

internal class SharedServerStatistics {
  private val numberOfConnections = LongAdder()

  fun incrementConnections() {
    numberOfConnections.increment()
  }

  fun decrementConnections() {
    numberOfConnections.decrement()
  }

  fun totalConnections() = numberOfConnections.sum()
}

internal class ChannelStatsHandler(private val sharedServerStatistics: SharedServerStatistics) :
  ChannelInboundHandlerAdapter() {
  private var isActive = false

  override fun channelActive(ctx: ChannelHandlerContext) {
    sharedServerStatistics.incrementConnections()
    isActive = true
    super.channelActive(ctx)
  }

  override fun channelInactive(ctx: ChannelHandlerContext) {
    if (isActive) {
      sharedServerStatistics.decrementConnections()
      isActive = false
    }
    super.channelInactive(ctx)
  }
}
