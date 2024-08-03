package com.sonianurag.fiber.netty

import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpUtil

internal fun HttpRequest.hasBody(): Boolean {
  return if (this.decoderResult().isFailure) {
    false
  } else {
    val contentLength =
      try {
        HttpUtil.getContentLength(this, -1)
      } catch (e: NumberFormatException) {
        -1
      }
    contentLength > 0 || HttpUtil.isTransferEncodingChunked(this)
  }
}
