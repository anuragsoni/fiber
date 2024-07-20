package com.sonianurag.fiber.vertx

import com.sonianurag.fiber.*
import com.sonianurag.fiber.VertxBuffer
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpServerRequest
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.coroutines.toReceiveChannel
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

class HttpRequestHandler(
  private val vertx: Vertx,
  private val service: suspend ServerContext.(Request) -> Response,
) : Handler<HttpServerRequest>, CoroutineScope {
  override val coroutineContext: CoroutineContext = vertx.dispatcher()

  private suspend fun dispatchRequest(request: Request, vertxRequest: HttpServerRequest) {
    val context = VertxServerContext(vertxRequest, vertx, request)
    val response = with(context) { service(request) }
    val vertxResponse = vertxRequest.response()
    vertxResponse.setStatusCode(response.statusCode.code)
    response.headers.forEach { entry -> vertxResponse.putHeader(entry.key, entry.value) }
    when (val body = response.body) {
      is EmptyBody -> vertxResponse.end()
      is FixedSizeBody -> vertxResponse.end(body.buf.toBuffer())
      is StreamingBody -> {
        when (val size = body.size) {
          null -> vertxResponse.setChunked(true)
          else -> {
            vertxResponse.putHeader(HttpHeaders.CONTENT_LENGTH, size.toString())
          }
        }

        body.asFlow().collect { chunk ->
          val future = vertxResponse.write(chunk.toBuffer())
          if (vertxResponse.writeQueueFull()) {
            future.coAwait()
          }
        }
        vertxResponse.end()
      }
    }
  }

  override fun handle(vertxRequest: HttpServerRequest) {
    if (vertxRequest.hasBody()) {
      val bodyStream = vertxRequest.toReceiveChannel(vertx)
      val request =
        vertxRequest.toRequest(
          body =
            streamingBody {
              for (chunk in bodyStream) {
                writeBuf(VertxBuffer(chunk))
              }
            }
        )
      launch {
        try {
          dispatchRequest(request, vertxRequest)
        } finally {
          // Drain the body in-case the user hasn't consumed it.
          bodyStream.consumeEach {}
        }
      }
    } else {
      launch { dispatchRequest(vertxRequest.toRequest(), vertxRequest) }
    }
  }
}
