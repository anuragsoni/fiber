package com.sonianurag.fiber

import kotlin.test.*

class DefaultHeadersTest {
  @Test
  fun `can create headers from list`() {
    val headers = listOf("foo" to "bar", "hello" to "world").toHeaders()
    assertFalse { headers.isEmpty() }
    assertContentEquals(
      expected = listOf("foo" to "bar", "hello" to "world"),
      actual = headers.map { it.key to it.value },
    )
  }

  @Test
  fun `header operations`() {
    val headers = Headers.create()

    assertTrue { headers.isEmpty() }

    headers.addUnlessExists("foo", "bar")
    headers.add("foo", "baz")
    headers.addUnlessExists("foo", "this won't be added")

    assertContentEquals(
      expected = listOf("foo" to "bar", "foo" to "baz"),
      actual = headers.map { it.key to it.value },
    )

    headers.add("hello", "world")
    headers.remove("foo")

    assertContentEquals(
      expected = listOf("hello" to "world"),
      actual = headers.map { it.key to it.value },
    )

    headers.remove("doesNotExist")
    assertContentEquals(
      expected = listOf("hello" to "world"),
      actual = headers.map { it.key to it.value },
    )

    assertEquals(expected = "world", actual = headers.get("HELLO"))
    headers.add("foo", "bar")
    headers.add("FOO", "baz")
    assertContentEquals(expected = listOf("bar", "baz"), actual = headers.getAll("FoO"))

    headers.replace("foo", "THIS IS A NEW KEY")

    assertContentEquals(expected = listOf("THIS IS A NEW KEY"), actual = headers.getAll("FoO"))

    assertFalse { headers.containsKey("MISSING") }
    assertContentEquals(expected = listOf(), headers.getAll("MISSING"))

    assertContentEquals(expected = listOf("foo", "hello"), actual = headers.names().toList())
  }
}
