package com.sonianurag.fiber.http.netty

import com.sonianurag.fiber.buffer.Buf
import com.sonianurag.fiber.buffer.FiberByteBufAllocator
import com.sonianurag.fiber.http.Body
import com.sonianurag.fiber.http.Request
import com.sonianurag.fiber.http.Response
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

    override fun channelActive(ctx: ChannelHandlerContext) {
        logger.trace("{}/active", ctx.name())
        ctx.read()
    }

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

    private fun createBodyStream(
        ctx: ChannelHandlerContext,
        bodyChannel: ReceiveChannel<Buf>
    ): Flow<Buf> = flow {
        ctx.read()
        try {
            val message = bodyChannel.receive()
            emit(message)
        } catch (e: Exception) {
            when (e) {
                is ClosedReceiveChannelException -> return@flow
                else -> throw e
            }
        }
    }

    private suspend fun sendResponse(ctx: ChannelHandlerContext, response: Response) {
        when (val responseBody = response.body) {
            is Body.Empty -> {
                response.headers.replace("Content-Length", "0")
                val nettyResponse = response.toNettyResponse()
                ctx.writeAndFlush(nettyResponse).coAwait()
            }
            is Body.Fixed -> {
                response.headers.replace("Content-Length", responseBody.buf.size.toString())
                val nettyResponse =
                    response.toFullNettyResponse(responseBody.buf.toNettyByteBuf(ctx))
                ctx.writeAndFlush(nettyResponse).coAwait()
            }
            is Body.Streaming -> {
                response.headers.replace("Transfer-Encoding", "chunked")
                val nettyResponse = response.toNettyResponse()
                ctx.write(nettyResponse)
                responseBody.reads.collect { buf ->
                    if (!buf.isEmpty()) {
                        ctx.writeAndFlush(DefaultHttpContent(buf.toNettyByteBuf(ctx))).coAwait()
                    }
                }
                ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
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
                val requestBody = createBodyStream(ctx, bodyChannel)
                val request = msg.toRequest(Body.Streaming(requestBody))
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
