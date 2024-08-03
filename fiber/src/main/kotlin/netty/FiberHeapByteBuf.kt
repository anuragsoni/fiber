package com.sonianurag.fiber.netty

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.UnpooledHeapByteBuf
import io.netty.util.internal.PlatformDependent

internal class FiberHeapByteBuf(
  allocator: ByteBufAllocator,
  initialCapacity: Int,
  maxCapacity: Int,
) : UnpooledHeapByteBuf(allocator, initialCapacity, maxCapacity) {
  override fun retain(): ByteBuf {
    return this
  }

  override fun retain(increment: Int): ByteBuf {
    return this
  }

  override fun touch(): ByteBuf {
    return this
  }

  override fun touch(hint: Any?): ByteBuf {
    return this
  }

  override fun release(): Boolean {
    return false
  }

  override fun release(decrement: Int): Boolean {
    return false
  }
}

internal class FiberUnsafeHeapByteBuf(
  allocator: ByteBufAllocator,
  initialCapacity: Int,
  maxCapacity: Int,
) : UnpooledHeapByteBuf(allocator, initialCapacity, maxCapacity) {
  override fun retain(): ByteBuf {
    return this
  }

  override fun retain(increment: Int): ByteBuf {
    return this
  }

  override fun touch(): ByteBuf {
    return this
  }

  override fun touch(hint: Any?): ByteBuf {
    return this
  }

  override fun release(): Boolean {
    return false
  }

  override fun release(decrement: Int): Boolean {
    return false
  }

  override fun allocateArray(initialCapacity: Int): ByteArray {
    return PlatformDependent.allocateUninitializedArray(initialCapacity)
  }
}
