package com.sonianurag.fiber.net

import kotlinx.coroutines.Deferred
import java.net.SocketAddress

interface Server : AutoCloseable {
    /** Address that the server is listening on. */
    val listeningOn: SocketAddress

    /** [closed] becomes fulfilled once the underlying server has been shutdown. */
    fun closed(): Deferred<Unit>

    /** Returns the number of connections on the listening socket. */
    fun numberOfConnections(): Long
}