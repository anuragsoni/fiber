package com.sonianurag.fiber.http

import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpStatusClass
import kotlin.test.Test
import kotlin.test.assertTrue

class StatusCodeTest {
    @Test
    fun `verify that we match netty status class`() {
        (200..600).forEach { code ->
            val nettyCode = HttpResponseStatus.valueOf(code)
            val fiberCode = StatusCode(code)
            when (nettyCode.codeClass()) {
                HttpStatusClass.SUCCESS -> assertTrue { fiberCode.isSuccess() }
                HttpStatusClass.CLIENT_ERROR -> assertTrue { fiberCode.isClientError() }
                HttpStatusClass.SERVER_ERROR -> assertTrue { fiberCode.isServerError() }
                HttpStatusClass.INFORMATIONAL -> assertTrue { fiberCode.isInformational() }
                HttpStatusClass.REDIRECTION -> assertTrue { fiberCode.isRedirection() }
                else -> {}
            }
        }
    }
}