package com.sonianurag.fiber.http.netty

import com.sonianurag.fiber.buffer.Buf
import com.sonianurag.fiber.buffer.NettyBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.HttpContent
import io.netty.handler.codec.http.LastHttpContent
import io.netty.util.ReferenceCountUtil
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BodyHandler(
    override val coroutineContext: CoroutineContext,
    private val allocator: ByteBufAllocator,
    writer: SendChannel<Buf>
) : CoroutineScope, ChannelInboundHandlerAdapter() {
    private val logger: Logger = LoggerFactory.getLogger(BodyHandler::class.java)

    init {
        require(!allocator.isDirectBufferPooled) { "Unpooled byte buf allocator expected" }
    }

    sealed class State {
        data object Inactive : State()

        class Active(val writer: SendChannel<Buf>) : State()
    }

    private fun State.close() {
        when (this) {
            is State.Inactive -> {}
            is State.Active -> {
                writer.close()
                state = State.Inactive
            }
        }
    }

    fun drain() {
        state = State.Inactive
    }

    private suspend fun State.maybeSend(msg: HttpContent) {
        when (this) {
            is State.Inactive -> {
                ReferenceCountUtil.release(msg)
            }
            is State.Active -> {
                val buf = msg.content()
                val newBuffer =
                    try {
                        NettyBuf(allocator.buffer(buf.readableBytes()).writeBytes(buf))
                    } finally {
                        ReferenceCountUtil.release(msg)
                    }
                writer.send(newBuffer)
            }
        }
    }

    private var state: State = State.Active(writer)

    override fun handlerRemoved(ctx: ChannelHandlerContext) {
        ctx.read()
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        when (msg) {
            is LastHttpContent -> {
                launch {
                    if (msg == LastHttpContent.EMPTY_LAST_CONTENT) {
                        state.close()
                        ctx.pipeline().remove(this@BodyHandler)
                    } else {
                        try {
                            state.maybeSend(msg)
                        } catch (e: Exception) {
                            ctx.fireExceptionCaught(e)
                        } finally {
                            state.close()
                            ctx.pipeline().remove(this@BodyHandler)
                        }
                    }
                }
            }
            is HttpContent -> {
                launch {
                    try {
                        state.maybeSend(msg)
                    } catch (e: Exception) {
                        ctx.fireExceptionCaught(e)
                    }
                }
            }
            else ->
                throw IllegalArgumentException(
                    "Expecting to read Http Content but received: ${msg::class.java.name}"
                )
        }
    }
}
