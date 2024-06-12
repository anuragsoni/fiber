module com.sonianurag.fiber.example {
    requires kotlin.stdlib;
    requires kotlinx.coroutines.core;
    requires transitive com.sonianurag.fiber;
    requires ch.qos.logback.classic;
    requires org.slf4j;

    exports com.sonianurag.fiber.example;
}