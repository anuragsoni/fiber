package com.sonianurag.fiber

import java.nio.charset.Charset
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow

/**
 * Opaque interface representing HTTP bodies. The underlying implementation could be a fixed length
 * body, or a stream of byte (useful when working with really large bodies).
 */
interface Body {
  /**
   * Size of the underlying body. This value is nullable as the underlying body could be a chunk
   * encoded stream.
   */
  val size: Long?

  /** Returns true if the bytes of the body are fully consumed. */
  fun isConsumed(): Boolean

  /**
   * Returns a [flow] that contains slices of [Buf]. This method is safer to use when working with
   * large bodies as it processes the body by lazily producing new chunks as requested by the user.
   */
  fun asFlow(): Flow<Buf>

  /**
   * Converts the entire body to a [String]. Some care is needed when working with large bodies as
   * the entire content is loaded into memory.
   */
  suspend fun asString(charset: Charset = Charsets.UTF_8): String {
    return buildBuf { asFlow().collect { append(it) } }.decodeToString(charset)
  }

  companion object {
    val empty: Body = EmptyBody()
  }
}

/** Create a fixed sized body from the user provided [String]. */
fun String.toBody(charset: Charset = Charsets.UTF_8): Body {
  return FixedSizeBody(buildBuf(initialSize = this.length) { append(this@toBody, charset) })
}

/** Create a fixed sized body from the user provided [ByteArray]. */
fun ByteArray.toBody(): Body {
  return FixedSizeBody(buildBuf(initialSize = this.size) { append(this@toBody) })
}

interface StreamingBodyScope {
  suspend fun writeString(payload: String, charset: Charset = Charsets.UTF_8)

  suspend fun writeBuf(buf: Buf)

  suspend fun writeByteArray(byteArray: ByteArray)
}

fun streamingBody(size: Long? = null, block: suspend StreamingBodyScope.() -> Unit): Body {
  val reader =
    channelFlow {
        val scope =
          object : StreamingBodyScope {
            override suspend fun writeBuf(buf: Buf) {
              send(buf)
            }

            override suspend fun writeByteArray(byteArray: ByteArray) {
              send(buildBuf(initialSize = byteArray.size) { append(byteArray) })
            }

            override suspend fun writeString(payload: String, charset: Charset) {
              send(buildBuf(initialSize = payload.length) { append(payload, charset) })
            }
          }
        with(scope) { block() }
      }
      .buffer(Channel.RENDEZVOUS)
  return StreamingBody(size, reader)
}

internal class EmptyBody : Body {
  override val size: Long = 0

  override fun asFlow(): Flow<Buf> {
    return flow {}
  }

  override fun isConsumed(): Boolean {
    return true
  }

  override suspend fun asString(charset: Charset): String {
    return ""
  }
}

internal class FixedSizeBody(internal val buf: Buf) : Body {
  override val size: Long = buf.size.toLong()

  override fun asFlow(): Flow<Buf> {
    return flow { emit(buf) }
  }

  override fun isConsumed(): Boolean {
    return true
  }

  override suspend fun asString(charset: Charset): String {
    return buf.decodeToString(charset)
  }
}

internal class StreamingBody(override val size: Long?, private val reads: Flow<Buf>) : Body {

  override fun isConsumed(): Boolean {
    return false
  }

  override fun asFlow(): Flow<Buf> {
    return reads
  }
}
