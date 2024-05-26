package com.sonianurag.fiber.http.netty

import com.sonianurag.fiber.buffer.Buf
import com.sonianurag.fiber.buffer.FiberByteBufAllocator
import com.sonianurag.fiber.http.Body
import com.sonianurag.fiber.http.Request
import com.sonianurag.fiber.http.Response
import com.sonianurag.fiber.utilities.coAwait
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.DefaultHttpContent
import io.netty.handler.codec.http.HttpContent
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.LastHttpContent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException

class HttpRequestHandler(private val handler: suspend (Request) -> Response) : ChannelInboundHandlerAdapter() {
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

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        when (msg) {
            is HttpRequest -> {
                val dispatcher = ctx.executor().asCoroutineDispatcher()
                val bodyChannel = Channel<Buf>()
                val bodyDrained = CompletableDeferred<Unit>()
                bodyChannel.invokeOnClose {
                    bodyDrained.complete(Unit)
                }
                val bodyHandler =
                    BodyHandler(dispatcher, FiberByteBufAllocator.DEFAULT, bodyChannel)
                ctx.pipeline()
                    .addAfter(PipelineStages.HTTP_REQUEST_DECODER, PipelineStages.HTTP_BODY_HANDLER, bodyHandler)

                val request = msg.toRequest(bodyChannel.consumeAsFlow())
                CoroutineScope(dispatcher).launch {
                    val response = handler(request)
                    when (val responseBody = response.body) {
                        is Body.Empty -> {
                            response.headers.replace("Content-Length", "0")
                            val nettyResponse = response.toNettyResponse()
                            ctx.writeAndFlush(nettyResponse).coAwait()
                        }

                        is Body.Fixed -> {
                            response.headers.replace("Content-Length", responseBody.buf.size.toString())
                            val nettyResponse = response.toFullNettyResponse(responseBody.buf.toNettyByteBuf(ctx))
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

                    bodyHandler.drain()
                    bodyDrained.await()
                    ctx.read()
                }
            }

            is HttpContent -> ctx.fireChannelRead(msg)
            else -> {
                "Expecting to read Http Request or Content but received: ${msg::class.java.name}"
            }
        }
    }
}