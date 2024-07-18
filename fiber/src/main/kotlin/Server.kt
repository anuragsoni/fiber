package com.sonianurag.fiber

import kotlinx.coroutines.Deferred

interface Server : AutoCloseable {

  /** [closed] becomes fulfilled once the underlying server has been shutdown. */
  fun closed(): Deferred<Unit>

  val actualPort: Int

  /** Returns the number of connections on the listening socket. */
  fun numberOfConnections(): Long
}
