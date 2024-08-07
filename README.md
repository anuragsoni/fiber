# Fiber

Fiber is an asynchronous HTTP toolkit for Kotlin that is built on top of coroutines and [Vert.x](https://vertx.io).

> [!CAUTION]
> The library is in a POC stage so far. This is good for browsing/experimenting but is not ready for real-world use.

## Server Example

```kotlin
import com.sonianurag.fiber.Http
import com.sonianurag.fiber.toBody
import java.net.InetSocketAddress
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        val server =
            createHttpServer(InetSocketAddress("localhost", 8080)) { respond("Hello World".toBody()) }
        server.closed().await()
    }
}
```