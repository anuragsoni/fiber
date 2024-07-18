package com.sonianurag.fiber

import kotlin.test.Test
import kotlin.test.assertTrue

class StatusCodeTest {
  @Test
  fun `verify that we match netty status class`() {
    (200..<600).forEach { code ->
      val fiberCode = StatusCode(code)
      when (code) {
        in 100..<200 -> assertTrue { fiberCode.isInformational() }
        in 200..<300 -> assertTrue { fiberCode.isSuccess() }
        in 300..<400 -> assertTrue { fiberCode.isRedirection() }
        in 400..<500 -> assertTrue { fiberCode.isClientError() }
        in 500..<600 -> assertTrue { fiberCode.isServerError() }
        else -> TODO()
      }
    }
  }
}
