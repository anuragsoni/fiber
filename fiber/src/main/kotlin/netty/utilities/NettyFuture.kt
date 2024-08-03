package com.sonianurag.fiber.netty.utilities

import io.netty.util.concurrent.Future
import io.netty.util.concurrent.GenericFutureListener
import java.util.concurrent.ExecutionException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

internal suspend fun <T> Future<T>.coAwait(): T {
  if (isDone) {
    try {
      @Suppress("BlockingMethodInNonBlockingContext") return get()
    } catch (e: ExecutionException) {
      throw e.cause ?: e
    }
  }
  return suspendCancellableCoroutine { cont ->
    addListener(
      object : GenericFutureListener<Future<T>> {
        override fun operationComplete(future: Future<T>) {
          if (future.isSuccess) {
            cont.resume(future.now)
          } else {
            val e = future.cause()
            cont.resumeWithException(e.cause ?: e)
          }
        }
      }
    )
    if (isCancellable) {
      cont.invokeOnCancellation { cancel(false) }
    }
  }
}
