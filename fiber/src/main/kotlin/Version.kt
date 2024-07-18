package com.sonianurag.fiber

/** HTTP Version */
enum class Version {
  /** HTTP/1.1 */
  Http11,

  /** HTTP/2.0 */
  H2;

  override fun toString(): String {
    return when (this) {
      Http11 -> "HTTP/1.1"
      H2 -> "HTTP/2.0"
    }
  }
}
