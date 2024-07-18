package com.sonianurag.fiber

import com.sonianurag.fiber.vertx.*
import com.sonianurag.fiber.vertx.toVertxAddress
import io.vertx.core.Vertx
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.kotlin.core.http.httpServerOptionsOf
import io.vertx.kotlin.core.vertxOptionsOf
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.coAwait
import java.net.SocketAddress
import java.util.concurrent.atomic.LongAdder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

object Http {
  suspend fun createServer(
    whereToListen: SocketAddress,
    config: HttpServerOptions = HttpServerOptions(),
    service: suspend (Request) -> Response,
  ): Server {
    val server = CompletableDeferred<Server>()
    val counter = LongAdder()
    val vertx =
      Vertx.vertx(
        vertxOptionsOf(
          preferNativeTransport = config.preferNativeTransport,
          useDaemonThread = true,
          eventLoopPoolSize = Runtime.getRuntime().availableProcessors(),
        )
      )
    val httpServer = {
      object : CoroutineVerticle() {
        override suspend fun start() {
          val http =
            vertx.createHttpServer(
              httpServerOptionsOf(
                tcpNoDelay = config.tcpNoDelay,
                tcpKeepAlive = config.keepAlive,
                acceptBacklog = config.backlog,
                receiveBufferSize = config.receiveBufferSize,
                sendBufferSize = config.sendBufferSize,
                idleTimeout = config.connectTimeout?.inWholeSeconds?.toInt(),
              )
            )

          http.connectionHandler {
            counter.increment()
            it.closeHandler { counter.decrement() }
          }

          http.requestHandler(HttpRequestHandler(vertx, service))

          try {
            http.listen(whereToListen.toVertxAddress()).coAwait().let {
              server.complete(
                object : Server {
                  val closedDeferred = CompletableDeferred<Unit>()

                  override fun closed(): Deferred<Unit> {
                    return closedDeferred
                  }

                  override val actualPort: Int = it.actualPort()

                  override fun numberOfConnections(): Long {
                    return counter.sum()
                  }

                  override fun close() {
                    it.close().onComplete { result ->
                      if (result.succeeded()) {
                        closedDeferred.complete(Unit)
                      } else {
                        closedDeferred.completeExceptionally(result.cause())
                      }
                    }
                  }
                }
              )
            }
          } catch (e: Exception) {
            server.completeExceptionally(e)
            throw e
          }
        }
      }
    }

    vertx
      .deployVerticle(httpServer, deploymentOptionsOf(instances = config.workerThreads))
      .coAwait()
    return server.await()
  }
}
