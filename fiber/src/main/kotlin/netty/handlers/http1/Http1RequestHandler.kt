package com.sonianurag.fiber.netty.handlers.http1

import com.sonianurag.fiber.*
import com.sonianurag.fiber.netty.*
import com.sonianurag.fiber.netty.hasBody
import com.sonianurag.fiber.netty.toFullNettyResponse
import com.sonianurag.fiber.netty.toNettyByteBuf
import com.sonianurag.fiber.netty.toNettyResponse
import com.sonianurag.fiber.netty.utilities.coAwait
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

class Http1RequestHandler(
  override val coroutineContext: CoroutineContext,
  private val service: suspend ServerContext.(Request) -> Response,
) : ChannelInboundHandlerAdapter(), CoroutineScope {
  private val logger: Logger = LoggerFactory.getLogger(Http1RequestHandler::class.java)

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
    when (val body = response.body) {
      is EmptyBody -> {
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
    bodyReader: ReceiveChannel<Buf>,
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
    if (msg.hasBody()) {
      val bodyChannel = Channel<Buf>()
      val bodyHandler =
        Http1BodyHandler(coroutineContext, FiberByteBufAllocator.DEFAULT, bodyChannel)
      ctx
        .pipeline()
        .addAfter(
          PipelineStages.HTTP_REQUEST_DECODER,
          PipelineStages.HTTP_BODY_HANDLER,
          bodyHandler,
        )

      val size =
        if (HttpUtil.isContentLengthSet(msg)) {
          HttpUtil.getContentLength(msg)
        } else {
          null
        }
      val request =
        msg.toRequest(StreamingBody(size = size, reads = createBodyStream(ctx, bodyChannel)))
      launch {
        val response = with(NettyHttp1ServerContext(request, ctx.channel())) { service(request) }
        sendResponse(ctx, response)
        bodyHandler.drain()
      }
    } else {
      val request = msg.toRequest(Body.empty)
      launch {
        val response = with(NettyHttp1ServerContext(request, ctx.channel())) { service(request) }
        sendResponse(ctx, response)
      }
    }
  }

  override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
    when (msg) {
      is HttpRequest -> {
        when (msg.protocolVersion()) {
          HttpVersion.HTTP_1_1 -> processRequest(ctx, msg)
          else ->
            ctx.writeAndFlush(
              DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST).also {
                HttpUtil.setContentLength(it, 0)
                HttpUtil.setKeepAlive(it, false)
              }
            )
        }
      }
      is HttpContent -> ctx.fireChannelRead(msg)
      else ->
        throw IllegalArgumentException(
          "Expecting to read Http Request or Content but received: ${msg::class.java.name}"
        )
    }
  }
}
