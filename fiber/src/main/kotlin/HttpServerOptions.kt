package com.sonianurag.fiber

import kotlin.math.max
import kotlin.time.Duration

/**
 * Configuration options for HTTP servers.
 * - [backlog]: Number of clients that can have a connection pending.
 * - [workerThreads]: Number of worker threads that will run netty event loops.
 * - [tcpNoDelay]: A value of true disables
 *   [nagle's algorithm](https://en.wikipedia.org/wiki/Nagle%27s_algorithm).
 * - [preferNativeTransport]: Set to true to attempt using epoll/kqueue based native transports.
 *   False will use NIO.
 */
data class HttpServerOptions(
  val backlog: Int = 128,
  val workerThreads: Int = max(1, Runtime.getRuntime().availableProcessors()),
  val tcpNoDelay: Boolean = true,
  val preferNativeTransport: Boolean = true,
  val keepAlive: Boolean = true,
  val receiveBufferSize: Int = 65536,
  val sendBufferSize: Int = 65536,
  val connectTimeout: Duration? = null,
  val sslContext: SslContext? = null,
) {
  init {
    require(backlog > 0) { "backlog must be > 0" }
    require(workerThreads >= 1) { "Worker thread count must be >= 1" }
    require(receiveBufferSize > 0) { "receiveBufferSize must be > 0" }
    require(sendBufferSize > 0) { "sendBufferSize must be > 0" }
  }
}
