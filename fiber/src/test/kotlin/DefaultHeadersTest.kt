package com.sonianurag.fiber

import kotlin.test.*

class DefaultHeadersTest {
  @Test
  fun `can create headers incrementally`() {
    val headers = buildHeaders {
      add("foo", "bar")
      add("hello", "world")
    }
    assertFalse { headers.isEmpty }
    assertContentEquals(
      expected = listOf("foo" to "bar", "hello" to "world"),
      actual = headers.map { it.key to it.value },
    )
  }

  @Test
  fun `header operations`() {

    assertTrue { emptyHeaders().isEmpty }

    val headers = buildHeaders {
      addUnlessExists("foo", "bar")
      add("foo", "baz")
      addUnlessExists("foo", "this won't be added")
      add("hello", "world")
    }

    assertContentEquals(
      expected = listOf("foo" to "bar", "foo" to "baz", "hello" to "world"),
      actual = headers.map { it.key to it.value },
    )

    assertEquals(expected = "world", actual = headers.get("HELLO"))

    val headers2 = buildHeaders {
      add("foo", "bar")
      add("FOO", "baz")
    }

    assertContentEquals(expected = listOf("bar", "baz"), actual = headers2.getAll("FoO"))
    assertFalse { headers2.containsKey("MISSING") }
    assertContentEquals(expected = listOf(), headers2.getAll("MISSING"))

    assertContentEquals(expected = listOf("foo", "hello"), actual = headers.names().toList())
  }
}
