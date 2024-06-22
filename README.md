# Fiber

Fiber is an asynchronous HTTP toolkit for Kotlin that is built on top of coroutines and Netty.

> [!CAUTION]
> The library is in a POC stage so far. This is good for browsing/experimenting but is not ready for real-world use.

## Server Example

```kotlin
import com.sonianurag.fiber.http.Body
import com.sonianurag.fiber.http.Http
import com.sonianurag.fiber.http.Response
import com.sonianurag.fiber.net.Address
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

fun main() {
    val logger = LoggerFactory.getLogger("example")
    runBlocking {
        val service = Service("hello") {
            Response.create(Body.fromString("Hello World"))
        }
        val server =
            Http.createServer(
                Address.HostAndPort(host = "localhost", port = 8080),
                service = service
            )
        logger.info("Listening on: {}", server.listeningOn)
        server.closed().await()
    }
}
```