package com.sonianurag.fiber.buffer

import io.netty.buffer.*
import io.netty.util.internal.PlatformDependent

class FiberByteBufAllocator : AbstractByteBufAllocator() {
    companion object {
        val unpooledAllocator: ByteBufAllocator = UnpooledByteBufAllocator(false)
        val pooledAllocator: ByteBufAllocator = PooledByteBufAllocator(true)
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