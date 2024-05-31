# Fiber

Fiber is an asynchronous HTTP toolkit for Kotlin that is built on top of coroutines and Netty.

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
        val server =
            Http.createServer(Address.HostAndPort(host = "localhost", port = 8080)) {
                Response.create(Body.fromString("Hello World"))
            }
        logger.info("Listening on: {}", server.listeningOn)
        server.closed().await()
    }
}
```