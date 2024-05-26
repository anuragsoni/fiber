import com.sonianurag.fiber.http.*
import com.sonianurag.fiber.net.Address
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.math.max

const val payload = "Hello World"

fun main() {
    val logger = LoggerFactory.getLogger("example")
    runBlocking {
        val server =
            Http.createServer(
                Address.HostAndPort(host = "localhost", port = 8080),
                workerThreads = max(1, Runtime.getRuntime().availableProcessors() - 1)
            ) { request ->
                object : Response {
                    override val version: Version = request.version
                    override val headers: Headers = Headers()
                    override val statusCode: StatusCode = StatusCode.OK
                    override val body: Body = Body.fromString(payload)
                }
            }
        logger.info("Listening on: {}", server.listeningOn)
        server.closed().await()
    }
}