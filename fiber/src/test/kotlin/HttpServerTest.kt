package com.sonianurag.fiber

import java.net.InetSocketAddress
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
      Http.createServer(InetSocketAddress("localhost", 0)) {
          when (it.method) {
            Method.GET -> respond("Hello World".toBody())
            else -> respond(statusCode = StatusCode.METHOD_NOT_ALLOWED)
          }
        }
        .use { server ->
          HttpClient.newHttpClient().use { client ->
            val request =
              HttpRequest.newBuilder().uri(URI.create("http:/${server.listeningOn}")).build()
            val response = client.send(request, BodyHandlers.ofString())
            assertEquals(expected = "Hello World", actual = response.body())
          }
        }
    }
  }

  @Test
  fun `can respond to request with bodies`() {
    runTest {
      Http.createServer(InetSocketAddress("localhost", 0)) {
          when (it.method) {
            Method.POST -> respond(body = it.body)
            else -> respond(statusCode = StatusCode.METHOD_NOT_ALLOWED)
          }
        }
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
