package com.sonianurag.fiber

/** Interface representing an HTTP response. */
interface Response {
  /** HTTP version associated with the response. */
  val version: Version

  /** HTTP status code associate with the response. */
  val statusCode: StatusCode

  /** Header map associated with the response. */
  val headers: Headers

  /** Body associated with the response. */
  val body: Body
}

fun respond(
  body: Body = Body.empty,
  statusCode: StatusCode = StatusCode.OK,
  headers: Headers = emptyHeaders(),
  version: Version = Version.Http11,
): Response {
  return object : Response {
    override val version: Version = version
    override val statusCode: StatusCode = statusCode
    override val headers: Headers = headers
    override val body: Body = body
  }
}
