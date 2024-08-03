package com.sonianurag.fiber.netty.utilities

import io.netty.channel.Channel
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher

internal class NettyDispatcher(private val channel: Channel) : CoroutineDispatcher() {
  override fun isDispatchNeeded(context: CoroutineContext): Boolean {
    return !channel.eventLoop().inEventLoop()
  }

  override fun dispatch(context: CoroutineContext, block: Runnable) {
    channel.eventLoop().execute(block)
  }
}
