package com.sonianurag.fiber.http

import com.sonianurag.fiber.net.Address
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class HttpServerTest {
    @Test
    fun `can respond to get requests`() {
        runTest {
            Http.createServer(Address.HostAndPort(host = "localhost", port = 0)) {
                    Response.create(Body.fromString("Hello World"))
                }
                .use { server ->
                    val client = OkHttpClient()
                    val response =
                        client
                            .newCall(Request.Builder().url("http:/${server.listeningOn}").build())
                            .execute()
                    assertEquals(expected = "Hello World", actual = response.body?.string())
                }
        }
    }

    @Test
    fun `can respond to request with bodies`() {
        runTest {
            Http.createServer(Address.HostAndPort(host = "localhost", port = 0)) {
                    Response.create(it.body)
                }
                .use { server ->
                    val client = OkHttpClient()
                    val response =
                        client
                            .newCall(
                                Request.Builder()
                                    .url("http:/${server.listeningOn}")
                                    .post("Hello World".toRequestBody())
                                    .build()
                            )
                            .execute()
                    assertEquals(expected = "Hello World", actual = response.body?.string())
                }
        }
    }
}
