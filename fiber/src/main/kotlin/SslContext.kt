package com.sonianurag.fiber

import io.netty.handler.ssl.SslContext as NettySslContext
import io.netty.handler.ssl.SslContextBuilder
import javax.net.ssl.KeyManagerFactory

class SslContext private constructor(internal val nettyContext: NettySslContext) {
  sealed class Mode {

    class SslKeyManager(val keyManager: KeyManagerFactory) : Mode()
  }

  companion object {
    fun create(mode: Mode): SslContext {
      when (mode) {
        is Mode.SslKeyManager -> {
          return SslContext(SslContextBuilder.forServer(mode.keyManager).build())
        }
      }
    }
  }
}
