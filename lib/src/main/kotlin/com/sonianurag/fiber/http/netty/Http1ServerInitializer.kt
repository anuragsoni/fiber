package com.sonianurag.fiber.http.netty

import com.sonianurag.fiber.http.Request
import com.sonianurag.fiber.http.Response
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.handler.codec.http.HttpRequestDecoder
import io.netty.handler.codec.http.HttpResponseEncoder
import io.netty.handler.codec.http.HttpServerExpectContinueHandler
import kotlinx.coroutines.asCoroutineDispatcher

class Http1ServerInitializer(private val service: suspend (Request) -> Response) :
    ChannelInitializer<Channel>() {
    override fun initChannel(ch: Channel) {
        ch.config().setAutoRead(true)
        val pipeline = ch.pipeline()
        pipeline.addLast(PipelineStages.HTTP_REQUEST_DECODER, HttpRequestDecoder())
        pipeline.addLast(PipelineStages.HTTP_RESPONSE_ENCODER, HttpResponseEncoder())
        pipeline.addLast(
            PipelineStages.HTTP_SERVER_EXPECT_CONTINUE,
            HttpServerExpectContinueHandler()
        )
        pipeline.addLast(
            PipelineStages.HTTP_REQUEST_HANDLER,
            HttpRequestHandler(
                coroutineContext = ch.eventLoop().asCoroutineDispatcher(),
                handler = service
            )
        )
    }
}
