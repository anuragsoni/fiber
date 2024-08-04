package netty.utilities

import com.sonianurag.fiber.netty.utilities.coAwait
import io.netty.util.concurrent.DefaultEventExecutor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertThrows

class CustomException : Exception()

class NettyFutureTest {
  @Test
  fun `can await on netty futures`() {
    runTest {
      val executor = DefaultEventExecutor()
      val promise1 = executor.newPromise<Int>()
      val promise2 = executor.newPromise<Int>()
      executor.submit { promise1.setSuccess(1) }
      executor.submit { promise2.setSuccess(2) }

      coroutineScope {
        assertEquals(expected = 3, actual = promise1.coAwait() + promise2.coAwait())
      }
      executor.shutdownGracefully().coAwait()
    }
  }

  @Test
  fun `await will propagate exceptions`() {
    runTest {
      val executor = DefaultEventExecutor()
      val promise1 = executor.newPromise<Int>()
      val promise2 = executor.newPromise<Int>()
      executor.submit { promise1.setSuccess(1) }
      executor.submit { promise2.setFailure(CustomException()) }

      assertThrows<CustomException> { promise1.coAwait() + promise2.coAwait() }
      executor.shutdownGracefully().coAwait()
    }
  }

  @Test
  fun `await will propagate cancellations`() {
    runTest {
      val executor = DefaultEventExecutor()
      val promise1 = executor.newPromise<Int>()
      val promise2 = executor.newPromise<Int>()
      executor.submit { promise1.cancel(false) }
      executor.submit { promise2.setSuccess(1) }

      assertThrows<CancellationException> { promise1.coAwait() + promise2.coAwait() }
      executor.shutdownGracefully().coAwait()
    }
  }
}
