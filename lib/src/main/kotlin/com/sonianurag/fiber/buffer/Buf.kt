package com.sonianurag.fiber.buffer

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufConvertible
import java.nio.ByteBuffer
import java.nio.charset.Charset

/** [Buf] is an immutable byte buffer with efficient index based access. */
interface Buf {
    val size: Int

    /** [isEmpty] returns true if the buffer is empty. */
    fun isEmpty(): Boolean = size == 0

    operator fun get(index: Int): Byte

    /** Decodes a string from the [Buf] in [charset]. */
    fun decodeToString(charset: Charset = Charsets.UTF_8): String

    /**
     * Write the content of [Buf] to the destination bytearray.
     *
     * @throws [IllegalArgumentException] if the destination buffer is too small.
     */
    fun copyTo(destination: ByteArray, offset: Int)

    fun writeString(string: String, charset: Charset = Charsets.UTF_8)

    fun writeBuf(buf: Buf)

    /**
     * Write the content of [Buf] to the destination bytearray.
     *
     * @throws [IllegalArgumentException] if the destination buffer is too small.
     */
    fun copyTo(destination: ByteBuffer)

    operator fun iterator(): ByteIterator =
        object : ByteIterator() {
            private var index = 0

            override fun hasNext(): Boolean = index != size

            override fun nextByte(): Byte {
                return if (index != size) {
                    get(index++)
                } else {
                    throw NoSuchElementException("$index")
                }
            }
        }

    companion object {
        fun create(): Buf {
            return NettyBuf(FiberByteBufAllocator.DEFAULT.buffer())
        }

        fun byteArray(byteArray: ByteArray): Buf {
            val buffer =
                FiberByteBufAllocator.DEFAULT.buffer(byteArray.size, byteArray.size)
                    .writeBytes(byteArray)
            return NettyBuf(buffer)
        }

        fun string(string: String, charset: Charset = Charsets.UTF_8): Buf {
            return byteArray(string.toByteArray(charset))
        }
    }
}

internal class NettyBuf(private val byteBuf: ByteBuf) : ByteBufConvertible by byteBuf, Buf {
    override val size: Int = byteBuf.readableBytes()

    override fun get(index: Int): Byte {
        return byteBuf.getByte(index)
    }

    override fun decodeToString(charset: Charset): String {
        return byteBuf.toString(charset)
    }

    private fun checkDestinationForWrite(destinationLength: Int, destinationOffset: Int) {
        require(destinationOffset >= 0) {
            "Destination offset must be a non-negative integer: $destinationOffset"
        }
        require(size <= (destinationLength - destinationOffset)) {
            "Destination is too small. Capacity = ${destinationLength - destinationOffset}, needed: $size"
        }
    }

    override fun writeString(string: String, charset: Charset) {
        byteBuf.writeBytes(string.toByteArray(charset))
    }

    override fun writeBuf(buf: Buf) {
        when (buf) {
            is NettyBuf -> {
                byteBuf.writeBytes(buf.byteBuf)
            }
            else -> {
                if (!buf.isEmpty()) {
                    val bytearray = ByteArray(buf.size)
                    buf.copyTo(bytearray, 0)
                    byteBuf.writeBytes(bytearray)
                }
            }
        }
    }

    override fun copyTo(destination: ByteArray, offset: Int) {
        checkDestinationForWrite(destination.size, offset)
        byteBuf.getBytes(0, destination, offset, size)
    }

    override fun copyTo(destination: ByteBuffer) {
        checkDestinationForWrite(destination.remaining(), 0)
        byteBuf.getBytes(0, destination)
    }
}
