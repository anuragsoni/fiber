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

class BodyHandler(
    override val coroutineContext: CoroutineContext,
    private val allocator: ByteBufAllocator,
    private val sender: SendChannel<Buf>
) : CoroutineScope, ChannelInboundHandlerAdapter() {

    init {
        require(!allocator.isDirectBufferPooled) { "Unpooled byte buf allocator expected" }
    }

    private suspend fun writeChunk(msg: HttpContent) {
        val buf = msg.content()
        val newBuffer =
            try {
                NettyBuf(allocator.buffer(buf.readableBytes()).writeBytes(buf))
            } finally {
                ReferenceCountUtil.release(msg)
            }
        sender.send(newBuffer)
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext?) {
        sender.close()
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        when (msg) {
            is LastHttpContent -> {
                launch {
                    if (msg != LastHttpContent.EMPTY_LAST_CONTENT) {
                        try {
                            writeChunk(msg)
                        } catch (e: Exception) {
                            ctx.fireExceptionCaught(e)
                        }
                    }
                    sender.close()
                    ctx.pipeline().remove(this@BodyHandler)
                }
            }
            is HttpContent -> {
                launch {
                    try {
                        writeChunk(msg)
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
