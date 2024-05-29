package com.sonianurag.fiber.http

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import net.jqwik.api.*

class MethodTest {
    @Provide
    fun methodGenerator(): Arbitrary<Method> {
        return Arbitraries.of(
            listOf(
                Method.GET,
                Method.POST,
                Method.OPTIONS,
                Method.HEAD,
                Method.PUT,
                Method.PATCH,
                Method.DELETE,
                Method.TRACE,
                Method.CONNECT
            )
        )
    }

    @Test
    fun `Http Methods can be coverted to strings and back`() {
        val methods =
            listOf(
                Method.GET,
                Method.POST,
                Method.OPTIONS,
                Method.HEAD,
                Method.PUT,
                Method.PATCH,
                Method.DELETE,
                Method.TRACE,
                Method.CONNECT
            )

        val result = methods.map { it.toString() }.map { it.toHttpMethod() }

        assertContentEquals(expected = methods, actual = result)
    }

    @Property
    fun `Converting a method to string and back always succeeds`(
        @ForAll("methodGenerator") meth: Method
    ) {
        assertEquals(expected = meth, actual = meth.toString().toHttpMethod())
    }
}
