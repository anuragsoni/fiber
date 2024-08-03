package com.sonianurag.fiber

import java.net.SocketAddress
import kotlinx.coroutines.Deferred

interface Server : AutoCloseable {
  /** Address that the server is listening on. */
  val listeningOn: SocketAddress

  /** [closed] becomes fulfilled once the underlying server has been shutdown. */
  fun closed(): Deferred<Unit>

  /** Returns the number of connections on the listening socket. */
  fun numberOfConnections(): Long
}
