package com.sonianurag.fiber.utilities

import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ExecutionException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal suspend fun ChannelFuture.coAwait(): ChannelFuture {
    if (isDone) {
        try {
            return this
        } catch (e: ExecutionException) {
            throw e.cause ?: e
        }
    }
    return suspendCancellableCoroutine { continuation ->
        addListener(
            ChannelFutureListener { future ->
                if (future.isSuccess) {
                    continuation.resume(this@coAwait)
                } else {
                    val e = cause()
                    continuation.resumeWithException(e?.cause ?: e)
                }
            }
        )
        if (isCancellable) {
            continuation.invokeOnCancellation { cancel(false) }
        }
    }
}
