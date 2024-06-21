package com.sonianurag.fiber.http.netty

import com.sonianurag.fiber.buffer.Buf
import com.sonianurag.fiber.buffer.FiberByteBufAllocator
import com.sonianurag.fiber.http.*
import com.sonianurag.fiber.utilities.coAwait
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.*
import java.io.IOException
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class HttpRequestHandler(
    override val coroutineContext: CoroutineContext,
    private val handler: suspend (Request) -> Response
) : ChannelInboundHandlerAdapter(), CoroutineScope {
    private val logger: Logger = LoggerFactory.getLogger(HttpRequestHandler::class.java)

    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        when (cause) {
            is IOException -> {
                logger.trace("IO Exception from netty", cause)
                ctx?.close()
            }
            else -> {
                ctx?.close()
                logger.error("Unhandled exception in http handler", cause)
            }
        }
    }

    private suspend fun sendResponse(ctx: ChannelHandlerContext, response: Response) {
        when (val size = response.body.size) {
            null -> response.headers.replace("Transfer-Encoding", "chunked")
            else -> response.headers.replace("Content-Length", size.toString())
        }

        when (val body = response.body) {
            is Empty -> {
                val nettyResponse = response.toFullNettyResponse()
                ctx.writeAndFlush(nettyResponse).coAwait()
            }
            is FixedSizeBody -> {
                val nettyResponse = response.toFullNettyResponse(body.buf.toNettyByteBuf(ctx))
                ctx.writeAndFlush(nettyResponse).coAwait()
            }
            else -> {
                val nettyResponse = response.toNettyResponse()
                ctx.write(nettyResponse)

                response.body.asFlow().collect { buf ->
                    if (!buf.isEmpty()) {
                        ctx.writeAndFlush(DefaultHttpContent(buf.toNettyByteBuf(ctx))).coAwait()
                    }
                }

                ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).coAwait()
            }
        }
    }

    private fun createBodyStream(
        ctx: ChannelHandlerContext,
        bodyReader: ReceiveChannel<Buf>
    ): Flow<Buf> = flow {
        while (true) {
            ctx.read()
            try {
                val message = bodyReader.receive()
                emit(message)
            } catch (e: Exception) {
                when (e) {
                    is ClosedReceiveChannelException -> return@flow
                    else -> throw e
                }
            }
        }
    }

    private fun processRequest(ctx: ChannelHandlerContext, msg: HttpRequest) {
        when (msg.protocolVersion()) {
            HttpVersion.HTTP_1_1 -> {
                val bodyChannel = Channel<Buf>()
                val bodyHandler =
                    BodyHandler(coroutineContext, FiberByteBufAllocator.DEFAULT, bodyChannel)
                ctx.pipeline()
                    .addAfter(
                        PipelineStages.HTTP_REQUEST_DECODER,
                        PipelineStages.HTTP_BODY_HANDLER,
                        bodyHandler
                    )

                val size =
                    if (HttpUtil.isContentLengthSet(msg)) {
                        HttpUtil.getContentLength(msg)
                    } else {
                        null
                    }
                val request =
                    msg.toRequest(
                        StreamingBody(size = size, reads = createBodyStream(ctx, bodyChannel))
                    )
                launch {
                    val response = handler(request)
                    sendResponse(ctx, response)
                    bodyHandler.drain()
                }
            }
            else ->
                ctx.writeAndFlush(
                    DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST)
                        .also {
                            HttpUtil.setContentLength(it, 0)
                            HttpUtil.setKeepAlive(it, false)
                        }
                )
        }
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        when (msg) {
            is HttpRequest -> processRequest(ctx, msg)
            is HttpContent -> ctx.fireChannelRead(msg)
            else ->
                throw IllegalArgumentException(
                    "Expecting to read Http Request or Content but received: ${msg::class.java.name}"
                )
        }
    }
}
