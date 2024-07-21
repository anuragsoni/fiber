package com.sonianurag.fiber

import io.vertx.core.MultiMap
import io.vertx.core.http.impl.headers.HeadersMultiMap
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/** Mutable header builder that can be used to construct an instance of [Headers]. */
interface HeaderBuilder {
  /** Adds a new key-value pair to the header map. */
  fun add(key: String, value: String)

  /**
   * Similar to [add] but only adds the new key-pair if the header map doesn't have an entry with
   * [key] in the map.
   */
  fun addUnlessExists(key: String, value: String)

  /** Removes [key] from the header map. */
  fun remove(key: String)

  /**
   * Adds a new key-value entry to the header map. If the header map contains existing entries for
   * [key] they are removed.
   */
  fun replace(key: String, value: String) {
    remove(key)
    add(key, value)
  }

  /** Adds all headers in [headers] to the header map. */
  fun add(headers: Headers) {
    headers.forEach { entry -> add(entry.key, entry.value) }
  }
}

internal object EmptyHeaders : Headers {
  override val isEmpty: Boolean = true

  override val size: Int = 0

  override fun containsKey(key: String): Boolean {
    return false
  }

  override fun get(key: String): String? {
    return null
  }

  override fun getAll(key: String): List<String> {
    return emptyList()
  }

  override fun names(): Set<String> {
    return emptySet()
  }

  override fun iterator(): Iterator<Map.Entry<String, String>> {
    return emptyList<Map.Entry<String, String>>().iterator()
  }
}

fun emptyHeaders(): Headers = EmptyHeaders

/** Build an instance of [Headers] incrementally. */
@OptIn(ExperimentalContracts::class)
inline fun buildHeaders(handler: HeaderBuilder.() -> Unit): Headers {
  contract { callsInPlace(handler, InvocationKind.EXACTLY_ONCE) }

  val headers = HeadersMultiMap.httpHeaders()

  val builder =
    object : HeaderBuilder {
      override fun add(key: String, value: String) {
        headers.add(key, value)
      }

      override fun addUnlessExists(key: String, value: String) {
        if (!headers.contains(key)) {
          headers.add(key, value)
        }
      }

      override fun remove(key: String) {
        headers.remove(key)
      }
    }

  with(builder) { handler() }

  return object : Headers {

    override val isEmpty: Boolean = headers.isEmpty

    override val size: Int = headers.size()

    override fun containsKey(key: String): Boolean {
      return headers.contains(key)
    }

    override fun get(key: String): String? {
      return headers.get(key)
    }

    override fun getAll(key: String): List<String> {
      return headers.getAll(key) ?: emptyList()
    }

    override fun names(): Set<String> {
      return headers.names() ?: emptySet()
    }

    override fun iterator(): Iterator<Map.Entry<String, String>> {
      return headers.iteratorAsString()
    }
  }
}

/** Immutable Key-value map where key comparisons are case-insensitive. */
interface Headers : Iterable<Map.Entry<String, String>> {
  /** Returns true if the header map is empty. */
  val isEmpty: Boolean

  /** Returns the number of headers. */
  val size: Int

  /** Returns true if the header map contains a value for the user specified key. */
  fun containsKey(key: String): Boolean

  /**
   * Returns the value associated with [key]. If the key has multiple values, use [getAll] to return
   * all values.
   */
  fun get(key: String): String?

  /** Returns all values associated with [key]. */
  fun getAll(key: String): List<String>

  /** Return a set with all key names in the header map. */
  fun names(): Set<String>
}

internal class VertxHeader(private val vertxHeaders: MultiMap) : Headers {
  override val isEmpty: Boolean = vertxHeaders.isEmpty
  override val size: Int = vertxHeaders.size()

  override fun containsKey(key: String): Boolean {
    return vertxHeaders.contains(key)
  }

  override fun get(key: String): String? {
    return vertxHeaders.get(key)
  }

  override fun getAll(key: String): List<String> {
    return vertxHeaders.getAll(key) ?: emptyList()
  }

  override fun names(): Set<String> {
    return vertxHeaders.names() ?: emptySet()
  }

  override fun iterator(): Iterator<Map.Entry<String, String>> {
    return vertxHeaders.iterator()
  }
}
