module com.sonianurag.fiber {
    requires transitive kotlin.stdlib;
    requires transitive kotlinx.coroutines.core;
    requires jdk.unsupported;

    requires io.netty.buffer;
    requires io.netty.codec.http2;
    requires io.netty.codec.http;
    requires io.netty.handler;
    requires io.netty.transport.epoll.linux.aarch_64;
    requires io.netty.transport.epoll.linux.x86_64;
    requires io.netty.transport.kqueue.osx.aarch_64;
    requires io.netty.transport.kqueue.osx.x86_64;
    requires org.slf4j;

    exports com.sonianurag.fiber.buffer;
    exports com.sonianurag.fiber.http;
    exports com.sonianurag.fiber.net;
    exports com.sonianurag.fiber.ssl;
}