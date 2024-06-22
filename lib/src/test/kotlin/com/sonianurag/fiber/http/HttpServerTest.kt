package com.sonianurag.fiber.http

import com.sonianurag.fiber.net.Address
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class HttpServerTest {
    @Test
    fun `can respond to get requests`() {
        runTest {
            val service = Service("Hello") { Response.create(Body.fromString("Hello World")) }
            Http.createServer(Address.HostAndPort(host = "localhost", port = 0), service = service)
                .use { server ->
                    HttpClient.newHttpClient().use { client ->
                        val request =
                            HttpRequest.newBuilder()
                                .uri(URI.create("http:/${server.listeningOn}"))
                                .build()
                        val response = client.send(request, BodyHandlers.ofString())
                        assertEquals(expected = "Hello World", actual = response.body())
                    }
                }
        }
    }

    @Test
    fun `can respond to request with bodies`() {
        runTest {
            val service = Service("hello") { Response.create(it.body) }
            Http.createServer(Address.HostAndPort(host = "localhost", port = 0), service = service)
                .use { server ->
                    HttpClient.newHttpClient().use { client ->
                        val request =
                            HttpRequest.newBuilder()
                                .uri(URI.create("http:/${server.listeningOn}"))
                                .method("POST", BodyPublishers.ofString("Hello World"))
                                .build()
                        val response = client.send(request, BodyHandlers.ofString())
                        assertEquals(expected = "Hello World", actual = response.body())
                    }
                }
        }
    }
}
