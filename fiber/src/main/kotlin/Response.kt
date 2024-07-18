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
