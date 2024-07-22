package com.sonianurag.fiber

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import net.jqwik.api.ForAll
import net.jqwik.api.Property

class BodyTest {
  @Property
  fun `can round-trip bodies from string`(@ForAll input: String) {
    runTest { assertEquals(expected = input, actual = input.toBody().asString()) }
  }

  @Property
  fun `fixed size bodies have length specified`(@ForAll input: String) {
    assertEquals(expected = input.encodeToByteArray().size.toLong(), input.toBody().size)
  }

  @Test
  fun `can create streaming bodies`() {
    runTest {
      val body = streamingBody {
        writeString("hello")
        writeByteArray(" world".encodeToByteArray())
        writeBuf(buildBuf { append(". Foo bar.") })
      }
      assertEquals(expected = "hello world. Foo bar.", actual = body.asString())
    }
  }

  @Test
  fun `can consume bodies as a flow`() {
    runTest {
      val fixedSizeBody = "Hello World".encodeToByteArray().toBody()
      val result = buildString { fixedSizeBody.asFlow().collect { append(it.decodeToString()) } }
      assertEquals(expected = "Hello World", actual = result)
    }
  }
}
