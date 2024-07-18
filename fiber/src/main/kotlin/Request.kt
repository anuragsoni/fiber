package com.sonianurag.fiber

/** Interface representing an HTTP request. */
interface Request {
  /** HTTP version associated with the request. */
  val version: Version

  /** HTTP method associated with the request. */
  val method: Method

  /** URI associated with the request. */
  val uri: String

  /** Header map associated with the request. */
  val headers: Headers

  /** Body associated with the request. */
  val body: Body
}
