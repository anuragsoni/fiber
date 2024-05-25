package com.sonianurag.fiber.http

import io.netty.handler.codec.http.DefaultHttpHeadersFactory
import io.netty.handler.codec.http.HttpHeaders

class Headers {
    private val headers: HttpHeaders = DefaultHttpHeadersFactory.headersFactory().newHeaders()

    fun isEmpty(): Boolean {
        return headers.isEmpty
    }

    fun size(): Int {
        return headers.size()
    }

    fun asSequence(): Sequence<Pair<String, String>> = sequence {
        headers.forEach { entry ->
            yield(entry.key to entry.value)
        }
    }

    fun add(key: String, value: String) {
        headers.add(key, value)
    }

    fun addUnlessExists(key: String, value: String) {
        if (!headers.contains(key)) {
            headers.add(key, value)
        }
    }

    fun contains(key: String): Boolean = headers.contains(key)

    fun replace(key: String, value: String) = headers.set(key, value)

    fun get(key: String): String? = headers.get(key)

    fun getAll(key: String): List<String> = headers.getAll(key) ?: listOf()

    fun remove(key: String) = headers.remove(key)

    fun names(): Set<String> = headers.names() ?: setOf()
}