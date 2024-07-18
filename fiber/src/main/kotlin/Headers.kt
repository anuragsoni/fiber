package com.sonianurag.fiber

import io.vertx.core.http.impl.headers.HeadersMultiMap

/** Key-value map where key comparisons are case-insensitive. */
interface Headers : Iterable<Map.Entry<String, String>> {
  /** Returns true if the header map is empty. */
  fun isEmpty(): Boolean

  /** Returns true if the header map contains a value for the user specified key. */
  fun containsKey(key: String): Boolean

  /** Adds a new key-value pair to the header map. */
  fun add(key: String, value: String)

  /**
   * Similar to [add] but only adds the new key-pair if the header map doesn't have an entry with
   * [key] in the map.
   */
  fun addUnlessExists(key: String, value: String) {
    if (!containsKey(key)) {
      add(key, value)
    }
  }

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

  /**
   * Returns the value associated with [key]. If the key has multiple values, use [getAll] to return
   * all values.
   */
  fun get(key: String): String?

  /** Returns all values associated with [key]. */
  fun getAll(key: String): List<String>

  /** Return a set with all key names in the header map. */
  fun names(): Set<String>

  companion object {
    fun create(): Headers {
      return DefaultHeaders()
    }
  }
}

/** Creates [Headers] from a list of [Pair]. */
fun Iterable<Pair<String, String>>.toHeaders(): Headers {
  return DefaultHeaders().also { this.forEach { item -> it.add(item.first, item.second) } }
}

private class DefaultHeaders : Headers {
  private val headers = HeadersMultiMap.httpHeaders()

  @Synchronized
  override fun isEmpty(): Boolean {
    return headers.isEmpty
  }

  @Synchronized
  override fun containsKey(key: String): Boolean {
    return headers.contains(key)
  }

  @Synchronized
  override fun add(key: String, value: String) {
    headers.add(key, value)
  }

  @Synchronized
  override fun remove(key: String) {
    headers.remove(key)
  }

  @Synchronized
  override fun get(key: String): String? {
    return headers.get(key)
  }

  @Synchronized
  override fun getAll(key: String): List<String> {
    return headers.getAll(key) ?: emptyList()
  }

  @Synchronized
  override fun names(): Set<String> {
    return headers.names() ?: emptySet()
  }

  override fun iterator(): Iterator<Map.Entry<String, String>> {
    return headers.iteratorAsString()
  }
}
