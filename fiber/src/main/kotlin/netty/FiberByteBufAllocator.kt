package com.sonianurag.fiber.netty

import io.netty.buffer.AbstractByteBufAllocator
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.UnpooledByteBufAllocator
import io.netty.util.internal.PlatformDependent

class FiberByteBufAllocator : AbstractByteBufAllocator() {
  companion object {
    val unpooledAllocator: ByteBufAllocator = UnpooledByteBufAllocator(false)
    val DEFAULT = FiberByteBufAllocator()
  }

  override fun isDirectBufferPooled(): Boolean {
    return false
  }

  override fun newHeapBuffer(initialCapacity: Int, maxCapacity: Int): ByteBuf {
    return when (PlatformDependent.hasUnsafe()) {
      true -> FiberUnsafeHeapByteBuf(unpooledAllocator, initialCapacity, maxCapacity)
      false -> FiberHeapByteBuf(unpooledAllocator, initialCapacity, maxCapacity)
    }
  }

  override fun newDirectBuffer(initialCapacity: Int, maxCapacity: Int): ByteBuf {
    return unpooledAllocator.directBuffer(initialCapacity, maxCapacity)
  }
}
