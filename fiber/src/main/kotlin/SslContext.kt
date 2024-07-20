package com.sonianurag.fiber

import javax.net.ssl.KeyManagerFactory

class SslContext(val keyManagerFactory: KeyManagerFactory, val enableAlpn: Boolean = false)
