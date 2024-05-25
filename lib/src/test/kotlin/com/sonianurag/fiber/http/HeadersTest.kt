package com.sonianurag.fiber.http

import kotlin.test.*

class HeadersTest {
    @Test
    fun `header operations`() {
        val headers = Headers()
        assertTrue {
            headers.isEmpty()
        }

        headers.addUnlessExists("foo", "bar")
        headers.add("foo", "baz")
        headers.addUnlessExists("foo", "this won't be added")

        assertEquals(expected = 2, headers.size())

        assertContentEquals(
            expected = listOf("foo" to "bar", "foo" to "baz"),
            actual = headers.asSequence().toList()
        )

        headers.add("hello", "world")
        headers.remove("foo")

        assertContentEquals(
            expected = listOf("hello" to "world"),
            actual = headers.asSequence().toList()
        )

        headers.remove("doesNotExist")
        assertContentEquals(
            expected = listOf("hello" to "world"),
            actual = headers.asSequence().toList()
        )

        assertEquals(expected = "world", actual = headers.get("HELLO"))

        headers.add("foo", "bar")
        headers.add("FOO", "baz")
        assertContentEquals(expected = listOf("bar", "baz"), actual = headers.getAll("FoO"))

        headers.replace("foo", "THIS IS A NEW KEY")
        assertContentEquals(expected = listOf("THIS IS A NEW KEY"), actual = headers.getAll("FoO"))

        assertFalse { headers.contains("MISSING") }
        assertContentEquals(expected = listOf(), headers.getAll("MISSING"))

        assertContentEquals(expected = listOf("hello", "foo"), actual = headers.names().toList())
    }
}