package com.sonianurag.fiber

import com.sonianurag.fiber.netty.FiberByteBufAllocator
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufConvertible
import java.nio.ByteBuffer
import java.nio.charset.Charset
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/** Mutable builder that can be used to construct an instance of [Buf]. */
interface BufBuilder {
  /** Appends the user provided string to [BufBuilder]. */
  fun append(string: String, charset: Charset = Charsets.UTF_8)

  /** Appends the user provided bytearray to [BufBuilder] */
  fun append(byteArray: ByteArray)

  /** Appends the user provided [Buf] to [BufBuilder]. */
  fun append(buf: Buf)
}

/** [Buf] is an immutable byte buffer with efficient index based access. */
sealed interface Buf {
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
    val empty: Buf = Empty()
  }
}

@PublishedApi
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

  override fun copyTo(destination: ByteArray, offset: Int) {
    checkDestinationForWrite(destination.size, offset)
    byteBuf.getBytes(0, destination, offset, size)
  }

  override fun copyTo(destination: ByteBuffer) {
    checkDestinationForWrite(destination.remaining(), 0)
    byteBuf.getBytes(0, destination)
  }
}

private class Empty : Buf {
  override val size: Int = 0

  override fun get(index: Int): Byte {
    throw IndexOutOfBoundsException()
  }

  private val contentAsString = ""

  override fun decodeToString(charset: Charset): String {
    return contentAsString
  }

  override fun copyTo(destination: ByteArray, offset: Int) {}

  override fun copyTo(destination: ByteBuffer) {}
}

@PublishedApi
internal class MutableBufBuilder(initialSize: Int = 256) : BufBuilder {
  private val nettyBuf = FiberByteBufAllocator.DEFAULT.buffer(initialSize)

  fun toBuf(): Buf {
    return if (nettyBuf.readableBytes() == 0) {
      Buf.empty
    } else {
      NettyBuf(nettyBuf)
    }
  }

  override fun append(buf: Buf) {
    when (buf) {
      is NettyBuf -> {
        nettyBuf.writeBytes(buf.asByteBuf())
      }
      else -> {
        if (!buf.isEmpty()) {
          val byteArray = ByteArray(buf.size)
          buf.copyTo(byteArray, 0)
          nettyBuf.writeBytes(byteArray)
        }
      }
    }
  }

  override fun append(byteArray: ByteArray) {
    nettyBuf.writeBytes(byteArray)
  }

  override fun append(string: String, charset: Charset) {
    nettyBuf.writeBytes(string.toByteArray(charset))
  }
}

/** Build a [Buf] incrementally. */
@OptIn(ExperimentalContracts::class)
inline fun buildBuf(handler: BufBuilder.() -> Unit): Buf {
  contract { callsInPlace(handler, InvocationKind.EXACTLY_ONCE) }
  return MutableBufBuilder().apply(handler).toBuf()
}

/** Build a [Buf] incrementally. */
@OptIn(ExperimentalContracts::class)
inline fun buildBuf(initialSize: Int, handler: BufBuilder.() -> Unit): Buf {
  contract { callsInPlace(handler, InvocationKind.EXACTLY_ONCE) }
  return MutableBufBuilder(initialSize).apply(handler).toBuf()
}

fun String.toBuf(charset: Charset = Charsets.UTF_8): Buf {
  val buffer =
    FiberByteBufAllocator.DEFAULT.buffer(this.length).also {
      it.writeBytes(this.toByteArray(charset))
    }
  return NettyBuf(buffer)
}

fun ByteArray.toBuf(): Buf {
  val buffer =
    FiberByteBufAllocator.DEFAULT.buffer(this.size, this.size).also { it.writeBytes(this) }
  return NettyBuf(buffer)
}

/** Creates a new immutable buffer with the content of [other] appended at the end of [Buf]. */
fun Buf.append(other: Buf): Buf {
  return when (this) {
    is Empty -> other
    is NettyBuf -> {
      when (other) {
        is Empty -> this
        is NettyBuf -> {
          FiberByteBufAllocator.unpooledAllocator.compositeBuffer().let {
            it.addComponents(true, this.asByteBuf(), other.asByteBuf())
            NettyBuf(it)
          }
        }
      }
    }
  }
}
