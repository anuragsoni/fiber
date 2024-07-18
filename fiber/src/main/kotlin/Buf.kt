package com.sonianurag.fiber

import io.vertx.core.buffer.Buffer
import io.vertx.core.buffer.impl.BufferImpl
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
    val empty: Buf = EmptyBuffer()
  }
}

private class EmptyBuffer : Buf {
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

internal class VertxBuffer(internal val vertxBuf: Buffer) : Buf {
  override val size: Int = vertxBuf.length()

  override fun get(index: Int): Byte {
    return vertxBuf.getByte(index)
  }

  override fun decodeToString(charset: Charset): String {
    return vertxBuf.toString(charset)
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
    vertxBuf.getBytes(destination, offset)
  }

  override fun copyTo(destination: ByteBuffer) {
    checkDestinationForWrite(destination.remaining(), 0)
    val internal = vertxBuf as BufferImpl
    internal.byteBuf().getBytes(0, destination)
  }
}

@PublishedApi
internal class MutableBufBuilder(initialSize: Int = 256) : BufBuilder {
  private val vertxBuffer = Buffer.buffer(initialSize)

  fun toBuf(): Buf {
    return if (vertxBuffer.length() == 0) {
      Buf.empty
    } else {
      VertxBuffer(vertxBuffer)
    }
  }

  override fun append(buf: Buf) {
    when (buf) {
      is VertxBuffer -> {
        vertxBuffer.appendBuffer(buf.vertxBuf)
      }
      else -> {
        if (!buf.isEmpty()) {
          val byteArray = ByteArray(buf.size)
          buf.copyTo(byteArray, 0)
          vertxBuffer.appendBytes(byteArray)
        }
      }
    }
  }

  override fun append(byteArray: ByteArray) {
    vertxBuffer.appendBytes(byteArray)
  }

  override fun append(string: String, charset: Charset) {
    vertxBuffer.appendString(string, charset.name())
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
