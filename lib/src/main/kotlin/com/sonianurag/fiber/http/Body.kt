package com.sonianurag.fiber.http

import com.sonianurag.fiber.buffer.Buf
import java.nio.charset.Charset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

sealed class Body {
    data object Empty : Body()

    class Fixed(internal val buf: Buf) : Body()

    class Streaming(internal val reads: Flow<Buf>) : Body()

    companion object {
        val empty: Body = Empty

        fun fromString(payload: String, encoding: Charset = Charsets.UTF_8): Body {
            return Fixed(Buf.string(payload, encoding))
        }

        fun fromByteArray(payload: ByteArray): Body {
            return Fixed(Buf.byteArray(payload))
        }

        fun stream(block: suspend FlowCollector<Buf>.() -> Unit): Body {
            return Streaming(flow(block))
        }
    }
}

fun Body.isEmpty(): Boolean = this is Body.Empty

suspend fun Body.asString(charset: Charset = Charsets.UTF_8): String {
    return when (this) {
        is Body.Empty -> ""
        is Body.Fixed -> this.buf.decodeToString(charset)
        is Body.Streaming ->
            buildString { this@asString.reads.collect { append(it.decodeToString(charset)) } }
    }
}
