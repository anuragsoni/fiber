package com.sonianurag.fiber.http

import io.netty.handler.codec.http.DefaultHttpHeadersFactory
import io.netty.handler.codec.http.HttpHeaders

/** Key-value map where key comparisons are case-insensitive. */
class Headers : Iterable<Map.Entry<String, String>> {
    private val headers: HttpHeaders = DefaultHttpHeadersFactory.headersFactory().newHeaders()

    /** Returns true if the header map is empty. */
    fun isEmpty(): Boolean {
        return headers.isEmpty
    }

    /** Iterator that return all entries within the header map. */
    override fun iterator(): Iterator<Map.Entry<String, String>> {
        return headers.iteratorAsString()
    }

    /**
     * Returns the number of headers in the map.
     *
     * This number is the total number of header values. Since one header key can be associated with
     * multiple values.
     */
    fun size(): Int {
        return headers.size()
    }

    /** Adds a new key-value pair to the header map. */
    fun add(key: String, value: String) {
        headers.add(key, value)
    }

    /**
     * Similar to [add] but only adds the new key-pair if the header map doesn't have an entry with
     * [key] in the map.
     */
    fun addUnlessExists(key: String, value: String) {
        if (!headers.contains(key)) {
            headers.add(key, value)
        }
    }

    /** Returns true if the header map contains a value for the user specified key. */
    fun containsKey(key: String): Boolean = headers.contains(key)

    /**
     * Adds a new key-value entry to the header map. If the header map contains existing entries for
     * [key] they are removed.
     */
    fun replace(key: String, value: String) = headers.set(key, value)

    /**
     * Returns the value associated with [key]. If the key has multiple values, use [getAll] to
     * return all values.
     */
    fun get(key: String): String? = headers.get(key)

    /** Returns all values associated with [key]. */
    fun getAll(key: String): List<String> = headers.getAll(key) ?: listOf()

    /** Removes [key] from the header map. */
    fun remove(key: String) = headers.remove(key)

    /** Return a set with all key names in the header map. */
    fun names(): Set<String> = headers.names() ?: setOf()
}
