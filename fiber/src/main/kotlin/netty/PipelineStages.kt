package com.sonianurag.fiber.netty

internal object PipelineStages {
  const val HTTP_REQUEST_DECODER = "fiber/http_request_decoder"
  const val HTTP_REQUEST_HANDLER = "fiber/http_request_handler"
  const val HTTP_BODY_HANDLER = "fiber/http_body_handler"
  const val HTTP_RESPONSE_ENCODER = "fiber/http_response_encoder"
  const val HTTP_SERVER_EXPECT_CONTINUE = "fiber/http_server_expect_continue"
}
