package com.sonianurag.fiber

/**
 * Request method is used to indicate the purpose of an HTTP request. See
 * [https://datatracker.ietf.org/doc/html/rfc7231#section-4.3](https://datatracker.ietf.org/doc/html/rfc7231#section-4.3)
 * for more details.
 */
enum class Method {
  /**
   * The CONNECT method requests that the recipient establish a tunnel to the destination origin
   * server identified by the request-target and, if successful, thereafter restrict its behavior to
   * blind forwarding of packets, in both directions, until the tunnel is closed.
   *
   * [RFC7231, Section 4.3.6](https://datatracker.ietf.org/doc/html/rfc7231#section-4.3.6)
   */
  CONNECT,

  /**
   * The DELETE method requests that the origin server remove the association between the target
   * resource and its current functionality.
   *
   * [RFC7231, Section 4.3.5](https://datatracker.ietf.org/doc/html/rfc7231#section-4.3.5)
   */
  DELETE,

  /**
   * The GET method requests transfer of a current selected representation for the target resource.
   *
   * [RFC7231, Section 4.3.1](https://datatracker.ietf.org/doc/html/rfc7231#section-4.3.1)
   */
  GET,

  /**
   * The HEAD method is identical to GET except that the server MUST NOT send a message body in the
   * response (i.e., the response terminates at the end of the header section).
   *
   * [RFC7231, Section 4.3.2](https://datatracker.ietf.org/doc/html/rfc7231#section-4.3.2)
   */
  HEAD,

  /**
   * The OPTIONS method requests information about the communication options available for the
   * target resource, at either the origin server or an intervening intermediary.
   *
   * [RFC7231, Section 4.3.7](https://datatracker.ietf.org/doc/html/rfc7231#section-4.3.7)
   */
  OPTIONS,

  /**
   * The PATCH method requests that a set of changes described in the request entity be applied to
   * the resource identified by the Request-URI.
   *
   * [RFC5789, Section 4.3.7](https://datatracker.ietf.org/doc/html/rfc5789#section-2)
   */
  PATCH,

  /**
   * The POST method requests that the target resource process the representation enclosed in the
   * request according to the resource's own specific semantics.
   *
   * [RFC7231, Section 4.3.3](https://datatracker.ietf.org/doc/html/rfc7231#section-4.3.3)
   */
  POST,

  /**
   * The PUT method requests that the state of the target resource be created or replaced with the
   * state defined by the representation enclosed in the request message payload.
   *
   * [RFC7231, Section 4.3.4](https://datatracker.ietf.org/doc/html/rfc7231#section-4.3.4)
   */
  PUT,

  /**
   * The TRACE method requests a remote, application-level loop-back of the request message.
   *
   * [RFC7231, Section 4.3.8](https://datatracker.ietf.org/doc/html/rfc7231#section-4.3.8)
   */
  TRACE,
}

fun String.toHttpMethod(): Method {
  return when (this.uppercase()) {
    "GET" -> Method.GET
    "POST" -> Method.POST
    "OPTIONS" -> Method.OPTIONS
    "HEAD" -> Method.HEAD
    "PUT" -> Method.PUT
    "PATCH" -> Method.PATCH
    "DELETE" -> Method.DELETE
    "TRACE" -> Method.TRACE
    "CONNECT" -> Method.CONNECT
    else -> throw IllegalArgumentException("Unknown method $this")
  }
}

/**
 * [isSafe] returns true if the semantics for an HTTP method are essentially read-only, and the
 * client does not expect any state change on the server as a result of the request.
 *
 * See
 * [https://datatracker.ietf.org/doc/html/rfc7231#section-4.2.1](https://datatracker.ietf.org/doc/html/rfc7231#section-4.2.1)
 * for more details.
 */
fun Method.isSafe(): Boolean =
  when (this) {
    Method.GET,
    Method.HEAD,
    Method.OPTIONS,
    Method.TRACE -> true
    else -> false
  }

/**
 * [isIdempotent] returns true if multiple requests with an HTTP method are intended to have the
 * same effect on the server as a single such request. This function returns true for PUT, DELETE
 * and all safe methods.
 *
 * See
 * [https://datatracker.ietf.org/doc/html/rfc7231#section-4.2.2](https://datatracker.ietf.org/doc/html/rfc7231#section-4.2.2)
 * for more details.
 */
fun Method.isIdempotent(): Boolean =
  when (this) {
    Method.PUT,
    Method.DELETE -> true
    else -> this.isSafe()
  }

/**
 * [isCacheable] indicates that responses to requests with an HTTP method are allowed to be stored
 * for future reuse. This function returns true for GET, HEAD and POST.
 *
 * See
 * [https://datatracker.ietf.org/doc/html/rfc7231#section-4.2.3](https://datatracker.ietf.org/doc/html/rfc7231#section-4.2.3)
 * for more details.
 */
fun Method.isCacheable(): Boolean =
  when (this) {
    Method.GET,
    Method.HEAD,
    Method.POST -> true
    else -> false
  }
