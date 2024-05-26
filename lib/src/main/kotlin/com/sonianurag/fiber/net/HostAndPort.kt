package com.sonianurag.fiber.net

import io.netty.channel.unix.DomainSocketAddress
import java.net.InetSocketAddress
import java.net.SocketAddress

sealed class Address {
    data class HostAndPort(val host: String, val port: Int) : Address()

    data class Unix(val path: String) : Address()
}

fun Address.toSocketAddress(): SocketAddress {
    return when (this) {
        is Address.HostAndPort -> InetSocketAddress(this.host, this.port)
        is Address.Unix -> DomainSocketAddress(this.path)
    }
}