package com.sonianurag.fiber.utilities

import io.netty.util.concurrent.DefaultEventExecutor
import io.netty.util.concurrent.DefaultPromise
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NettyFutureTest {
    @Test
    fun `can await on netty futures`() {
        runTest {
            val executor = DefaultEventExecutor()
            val promise1 = DefaultPromise<Int>(executor)
            val promise2 = DefaultPromise<Int>(executor)
            executor.submit {
                promise1.setSuccess(1)
            }
            executor.submit {
                promise2.setSuccess(2)
            }

            coroutineScope {
                assertEquals(expected = 3, actual = promise1.coAwait() + promise2.coAwait())
            }

            executor.shutdownGracefully().coAwait()
        }
    }
}