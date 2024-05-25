package com.sonianurag.fiber.buffer

import net.jqwik.api.ForAll
import net.jqwik.api.Property
import org.junit.jupiter.api.assertThrows
import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertEquals

class BufTest {
    @Property
    fun `can round-trip strings without any modifications`(@ForAll input: String) {
        assertEquals(expected = input, actual = Buf.string(input).decodeToString())
    }

    @Test
    fun `copy verifies the destination bytearray size`() {
        val buf = Buf.string("hello world")
        assertThrows<IllegalArgumentException> {
            buf.copyTo(byteArrayOf(), 0)
        }
    }

    @Test
    fun `copy verifies the destination bytebuffer size`() {
        val buf = Buf.string("hello world")
        val destination = ByteBuffer.allocate(2)
        assertThrows<IllegalArgumentException> {
            buf.copyTo(destination)
        }
    }
}