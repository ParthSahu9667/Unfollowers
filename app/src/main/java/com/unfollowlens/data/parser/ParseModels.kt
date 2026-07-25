package com.unfollowlens.data.parser

/**
 * Represents a single parsed user from Instagram's export JSON.
 */
data class ParsedUser(
    val username: String,
    val profileUrl: String? = null,
    val timestamp: Long? = null
)

/**
 * Result of parsing an Instagram data export.
 */
data class ParseResult(
    val followers: List<ParsedUser>,
    val following: List<ParsedUser>
)
