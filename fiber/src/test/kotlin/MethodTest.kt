package com.sonianurag.fiber

import kotlin.test.Test
import kotlin.test.assertContentEquals

class MethodTest {
  @Test
  fun `Http Methods can be coverted to strings and back`() {

    val methods = enumValues<Method>().toList()
    val result = methods.map { it.toString() }.map { it.toHttpMethod() }

    assertContentEquals(expected = methods, actual = result)
  }
}
