package com.sonianurag.fiber

@JvmInline
value class StatusCode(val code: Int) {
  companion object {
    /**
     * 100 Continue
     *
     * [RFC7231, Section 6.2.1](https://tools.ietf.org/html/rfc7231#section-6.2.1)
     */
    val CONTINUE: StatusCode = StatusCode(100)

    /**
     * 101 Switching Protocols
     *
     * [RFC7231, Section 6.2.2](https://tools.ietf.org/html/rfc7231#section-6.2.2)
     */
    val SWITCHING_PROTOCOLS: StatusCode = StatusCode(101)

    /**
     * 102 Processing
     *
     * [RFC2518](https://tools.ietf.org/html/rfc2518)
     */
    val PROCESSING: StatusCode = StatusCode(102)

    /**
     * 103 Early Hints
     *
     * [RFC8297, Section 2](https://datatracker.ietf.org/doc/html/rfc8297#section-2)
     */
    val EARLY_HINTS: StatusCode = StatusCode(103)

    /**
     * 200 OK
     *
     * [RFC7231, Section 6.3.1](https://tools.ietf.org/html/rfc7231#section-6.3.1)
     */
    val OK: StatusCode = StatusCode(200)

    /**
     * 201 Created
     *
     * [RFC7231, Section 6.3.2](https://tools.ietf.org/html/rfc7231#section-6.3.2)
     */
    val CREATED: StatusCode = StatusCode(201)

    /**
     * 202 Accepted
     *
     * [RFC7231, Section 6.3.3](https://tools.ietf.org/html/rfc7231#section-6.3.3)
     */
    val ACCEPTED: StatusCode = StatusCode(202)

    /**
     * 203 Non-Authoritative Information
     *
     * [RFC7231, Section 6.3.4](https://tools.ietf.org/html/rfc7231#section-6.3.4)
     */
    val NON_AUTHORITATIVE_INFORMATION = StatusCode(203)

    /**
     * 204 No Content
     *
     * [RFC7231, Section 6.3.5](https://tools.ietf.org/html/rfc7231#section-6.3.5)
     */
    val NO_CONTENT = StatusCode(204)

    /**
     * 205 Reset Content
     *
     * [RFC7231, Section 6.3.6](https://tools.ietf.org/html/rfc7231#section-6.3.6)
     */
    val RESET_CONTENT = StatusCode(205)

    /**
     * 206 Partial Content
     *
     * [RFC7233, Section 4.1](https://tools.ietf.org/html/rfc7233#section-4.1)
     */
    val PARTIAL_CONTENT = StatusCode(206)

    /**
     * 207 Multi-Status
     *
     * [RFC4918](https://tools.ietf.org/html/rfc4918)
     */
    val MULTI_STATUS = StatusCode(207)

    /**
     * 208 Already Reported
     *
     * [RFC5842](https://tools.ietf.org/html/rfc5842)
     */
    val ALREADY_REPORTED = StatusCode(208)

    /**
     * 226 IM Used
     *
     * [RFC3229](https://tools.ietf.org/html/rfc3229)
     */
    val IM_USED = StatusCode(226)

    /**
     * 300 Multiple Choices
     *
     * [RFC7231, Section 6.4.1](https://tools.ietf.org/html/rfc7231#section-6.4.1)
     */
    val MULTIPLE_CHOICES = StatusCode(300)

    /**
     * 301 Moved Permanently
     *
     * [RFC7231, Section 6.4.2](https://tools.ietf.org/html/rfc7231#section-6.4.2)
     */
    val MOVED_PERMANENTLY = StatusCode(301)

    /**
     * 302 Found
     *
     * [RFC7231, Section 6.4.3](https://tools.ietf.org/html/rfc7231#section-6.4.3)
     */
    val FOUND = StatusCode(302)

    /**
     * 303 See Other
     *
     * [RFC7231, Section 6.4.4](https://tools.ietf.org/html/rfc7231#section-6.4.4)
     */
    val SEE_OTHER = StatusCode(303)

    /**
     * 304 Not Modified
     *
     * [RFC7232, Section 4.1](https://tools.ietf.org/html/rfc7232#section-4.1)
     */
    val NOT_MODIFIED = StatusCode(304)

    /**
     * 305 Use Proxy
     *
     * [RFC7231, Section 6.4.5](https://tools.ietf.org/html/rfc7231#section-6.4.5)
     */
    val USE_PROXY = StatusCode(305)

    /**
     * 307 Temporary Redirect
     *
     * [RFC7231, Section 6.4.7](https://tools.ietf.org/html/rfc7231#section-6.4.7)
     */
    val TEMPORARY_REDIRECT = StatusCode(307)

    /**
     * 308 Permanent Redirect
     *
     * [RFC7238](https://tools.ietf.org/html/rfc7238)
     */
    val PERMANENT_REDIRECT = StatusCode(308)

    /**
     * 400 Bad Request
     *
     * [RFC7231, Section 6.5.1](https://tools.ietf.org/html/rfc7231#section-6.5.1)
     */
    val BAD_REQUEST = StatusCode(400)

    /**
     * 401 Unauthorized
     *
     * [RFC7235, Section 3.1](https://tools.ietf.org/html/rfc7235#section-3.1)
     */
    val UNAUTHORIZED = StatusCode(401)

    /**
     * 402 Payment Required
     *
     * [RFC7231, Section 6.5.2](https://tools.ietf.org/html/rfc7231#section-6.5.2)
     */
    val PAYMENT_REQUIRED = StatusCode(402)

    /**
     * 403 Forbidden
     *
     * [RFC7231, Section 6.5.3](https://tools.ietf.org/html/rfc7231#section-6.5.3)
     */
    val FORBIDDEN = StatusCode(403)

    /**
     * 404 Not Found
     *
     * [RFC7231, Section 6.5.4](https://tools.ietf.org/html/rfc7231#section-6.5.4)
     */
    val NOT_FOUND = StatusCode(404)

    /**
     * 405 Method Not Allowed
     *
     * [RFC7231, Section 6.5.5](https://tools.ietf.org/html/rfc7231#section-6.5.5)
     */
    val METHOD_NOT_ALLOWED = StatusCode(405)

    /**
     * 406 Not Acceptable
     *
     * [RFC7231, Section 6.5.6](https://tools.ietf.org/html/rfc7231#section-6.5.6)
     */
    val NOT_ACCEPTABLE = StatusCode(406)

    /**
     * 407 Proxy Authentication Required
     *
     * [RFC7235, Section 3.2](https://tools.ietf.org/html/rfc7235#section-3.2)
     */
    val PROXY_AUTHENTICATION_REQUIRED = StatusCode(407)

    /**
     * 408 Request Timeout
     *
     * [RFC7231, Section 6.5.7](https://tools.ietf.org/html/rfc7231#section-6.5.7)
     */
    val REQUEST_TIMEOUT = StatusCode(408)

    /**
     * 409 Conflict
     *
     * [RFC7231, Section 6.5.8](https://tools.ietf.org/html/rfc7231#section-6.5.8)
     */
    val CONFLICT = StatusCode(409)

    /**
     * 410 Gone
     *
     * [RFC7231, Section 6.5.9](https://tools.ietf.org/html/rfc7231#section-6.5.9)
     */
    val GONE = StatusCode(410)

    /**
     * 411 Length Required
     *
     * [RFC7231, Section 6.5.10](https://tools.ietf.org/html/rfc7231#section-6.5.10)
     */
    val LENGTH_REQUIRED = StatusCode(411)

    /**
     * 412 Precondition Failed
     *
     * [RFC7232, Section 4.2](https://tools.ietf.org/html/rfc7232#section-4.2)
     */
    val PRECONDITION_FAILED = StatusCode(412)

    /**
     * 413 Payload Too Large
     *
     * [RFC7231, Section 6.5.11](https://tools.ietf.org/html/rfc7231#section-6.5.11)
     */
    val PAYLOAD_TOO_LARGE = StatusCode(413)

    /**
     * 414 URI Too Long
     *
     * [RFC7231, Section 6.5.12](https://tools.ietf.org/html/rfc7231#section-6.5.12)
     */
    val URI_TOO_LONG = StatusCode(414)

    /**
     * 415 Unsupported Media Type
     *
     * [RFC7231, Section 6.5.13](https://tools.ietf.org/html/rfc7231#section-6.5.13)
     */
    val UNSUPPORTED_MEDIA_TYPE = StatusCode(415)

    /**
     * 416 Range Not Satisfiable
     *
     * [RFC7233, Section 4.4](https://tools.ietf.org/html/rfc7233#section-4.4)
     */
    val RANGE_NOT_SATISFIABLE = StatusCode(416)

    /**
     * 417 Expectation Failed
     *
     * [RFC7231, Section 6.5.14](https://tools.ietf.org/html/rfc7231#section-6.5.14)
     */
    val EXPECTATION_FAILED = StatusCode(417)

    /**
     * 418 I'm a teapot
     *
     * [RFC2324](https://tools.ietf.org/html/rfc2324)
     */
    val IM_A_TEAPOT = StatusCode(418)

    /**
     * 421 Misdirected Request
     *
     * [RFC7540, Section 9.1.2](http://tools.ietf.org/html/rfc7540#section-9.1.2)
     */
    val MISDIRECTED_REQUEST = StatusCode(421)

    /**
     * 422 Unprocessable Entity
     *
     * [RFC4918](https://tools.ietf.org/html/rfc4918)
     */
    val UNPROCESSABLE_ENTITY = StatusCode(422)

    /**
     * 423 Locked
     *
     * [RFC4918](https://tools.ietf.org/html/rfc4918)
     */
    val LOCKED = StatusCode(423)

    /**
     * 424 Failed Dependency
     *
     * [RFC4918](https://tools.ietf.org/html/rfc4918)
     */
    val FAILED_DEPENDENCY = StatusCode(424)

    /**
     * 425 Too Early
     *
     * [RFC8470, Section 5.2](https://datatracker.ietf.org/doc/html/rfc8470#section-5.2)
     */
    val TOO_EARLY = StatusCode(425)

    /**
     * 426 Upgrade Required
     *
     * [RFC7231, Section 6.5.15](https://tools.ietf.org/html/rfc7231#section-6.5.15)
     */
    val UPGRADE_REQUIRED = StatusCode(426)

    /**
     * 428 Precondition Required
     *
     * [RFC6585](https://tools.ietf.org/html/rfc6585)
     */
    val PRECONDITION_REQUIRED = StatusCode(428)

    /**
     * 429 Too Many Requests
     *
     * [RFC6585](https://tools.ietf.org/html/rfc6585)
     */
    val TOO_MANY_REQUESTS = StatusCode(429)

    /**
     * 431 Request Header Fields Too Large
     *
     * [RFC6585](https://tools.ietf.org/html/rfc6585)
     */
    val REQUEST_HEADER_FIELDS_TOO_LARGE = StatusCode(431)

    /**
     * 451 Unavailable For Legal Reasons
     *
     * [RFC7725](http://tools.ietf.org/html/rfc7725)
     */
    val UNAVAILABLE_FOR_LEGAL_REASONS = StatusCode(451)

    /**
     * 500 Internal Server Error
     *
     * [RFC7231, Section 6.6.1](https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    val INTERNAL_SERVER_ERROR = StatusCode(500)

    /**
     * 501 Not Implemented
     *
     * [RFC7231, Section 6.6.2](https://tools.ietf.org/html/rfc7231#section-6.6.2)
     */
    val NOT_IMPLEMENTED = StatusCode(501)

    /**
     * 502 Bad Gateway
     *
     * [RFC7231, Section 6.6.3](https://tools.ietf.org/html/rfc7231#section-6.6.3)
     */
    val BAD_GATEWAY = StatusCode(502)

    /**
     * 503 Service Unavailable
     *
     * [RFC7231, Section 6.6.4](https://tools.ietf.org/html/rfc7231#section-6.6.4)
     */
    val SERVICE_UNAVAILABLE = StatusCode(503)

    /**
     * 504 Gateway Timeout
     *
     * [RFC7231, Section 6.6.5](https://tools.ietf.org/html/rfc7231#section-6.6.5)
     */
    val GATEWAY_TIMEOUT = StatusCode(504)

    /**
     * 505 HTTP Version Not Supported
     *
     * [RFC7231, Section 6.6.6](https://tools.ietf.org/html/rfc7231#section-6.6.6)
     */
    val HTTP_VERSION_NOT_SUPPORTED = StatusCode(505)

    /**
     * 506 Variant Also Negotiates
     *
     * [RFC2295](https://tools.ietf.org/html/rfc2295)
     */
    val VARIANT_ALSO_NEGOTIATES = StatusCode(506)

    /**
     * 507 Insufficient Storage
     *
     * [RFC4918](https://tools.ietf.org/html/rfc4918)
     */
    val INSUFFICIENT_STORAGE = StatusCode(507)

    /**
     * 508 Loop Detected
     *
     * [RFC5842](https://tools.ietf.org/html/rfc5842)
     */
    val LOOP_DETECTED = StatusCode(508)

    /**
     * 510 Not Extended
     *
     * [RFC2774](https://tools.ietf.org/html/rfc2774)
     */
    val NOT_EXTENDED = StatusCode(510)

    /**
     * 511 Network Authentication Required
     *
     * [RFC6585](https://tools.ietf.org/html/rfc6585)
     */
    val NETWORK_AUTHENTICATION_REQUIRED = StatusCode(511)
  }

  override fun toString(): String {
    return code.toString()
  }
}

/** Returns true if the status is in the range 100-199. */
fun StatusCode.isInformational(): Boolean = code in 100..<200

/** Returns true if the status is in the range 200-299. */
fun StatusCode.isSuccess(): Boolean = code in 200..<300

/** Returns true if the status is in the range 300-399. */
fun StatusCode.isRedirection(): Boolean = code in 300..<400

/** Returns true if the status is in the range 400-499. */
fun StatusCode.isClientError(): Boolean = code in 400..<500

/** Returns true if the status is in the range 500-599. */
fun StatusCode.isServerError(): Boolean = code in 500..<600
