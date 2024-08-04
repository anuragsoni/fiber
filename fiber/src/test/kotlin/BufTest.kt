package com.sonianurag.fiber

import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import net.jqwik.api.ForAll
import net.jqwik.api.Property
import org.junit.jupiter.api.assertThrows

class BufTest {
  @Test
  fun `can create a buf using builder`() {
    val temp = buildBuf { append(". ") }
    val buffer = buildBuf {
      append("Hello World")
      append(temp)
      append(Buf.empty)
    }
    assertEquals(expected = "Hello World. ", actual = buffer.decodeToString())
    assertFalse { buffer.isEmpty() }
    assertEquals(expected = 13, actual = buffer.size)
  }

  @Test
  fun `can iterate over buf`() {
    val buf = buildBuf { append("this is some text.") }
    val actual = buildString { buf.iterator().forEach { append(it.toInt().toChar()) } }
    assertEquals(expected = buf.decodeToString(), actual = actual)
  }

  @Test
  fun `buf accessor performs bounds checking`() {
    val buf = Buf.empty
    assertThrows<IndexOutOfBoundsException> { buf[0] }
  }

  @Test
  fun `copy verifies the destination bytearray size`() {
    val buf = buildBuf(32) { append("Hello world") }
    assertThrows<IllegalArgumentException> { buf.copyTo(byteArrayOf(), 0) }
    assertThrows<IllegalArgumentException> { buf.copyTo(byteArrayOf(), -1) }
  }

  @Test
  fun `copy verifies the destination bytebuffer size`() {
    val buf = buildBuf { append("Hello World") }
    val destination = ByteBuffer.allocate(2)
    assertThrows<IllegalArgumentException> { buf.copyTo(destination) }
  }

  @Property
  fun `can round-trip strings without any modifications`(@ForAll input: String) {
    val buf = buildBuf { append(input) }
    assertEquals(expected = input, actual = buf.decodeToString())
  }

  @Test
  fun `can concatenate buffers`() {
    val samplePayload = "Hello World"
    val buf = buildBuf { append(samplePayload) }
    val combinedBuffer = Buf.empty.append(buf).append(buf).append(buf).append(buf).append(Buf.empty)
    assertEquals(
      expected =
        listOf(samplePayload, samplePayload, samplePayload, samplePayload)
          .joinToString(separator = ""),
      actual = combinedBuffer.decodeToString(),
    )
  }
}
