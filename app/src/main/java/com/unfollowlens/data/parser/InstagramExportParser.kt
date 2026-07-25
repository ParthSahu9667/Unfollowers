package com.unfollowlens.data.parser

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parses Instagram's data export JSON files into [ParsedUser] lists.
 *
 * Instagram's export format has changed over time. This parser handles
 * multiple known structures via a defensive adapter approach so that
 * only this module needs updating when the format changes again.
 *
 * Known formats:
 * 1. Current (2023+): Array of objects with "string_list_data" containing
 *    [{value, href, timestamp}]
 * 2. Older: Object with "relationships_following" / "relationships_followers"
 *    wrapping the same structure
 * 3. Very old: Simple array of {username, timestamp} objects
 */
@Singleton
class InstagramExportParser @Inject constructor() {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    /**
     * Parse a followers JSON string. Handles numbered file splits
     * by accepting a list of JSON content strings.
     */
    fun parseFollowers(vararg jsonContents: String): List<ParsedUser> {
        return jsonContents.flatMap { content -> parseUserList(content) }
    }

    /**
     * Parse a following JSON string.
     */
    fun parseFollowing(jsonContent: String): List<ParsedUser> {
        return parseUserList(jsonContent)
    }

    /**
     * Core parsing logic — tries multiple format strategies in order.
     */
    private fun parseUserList(content: String): List<ParsedUser> {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return emptyList()

        val element = try {
            json.parseToJsonElement(trimmed)
        } catch (e: Exception) {
            return emptyList()
        }

        return tryParseCurrentFormat(element)
            ?: tryParseLegacyWrappedFormat(element)
            ?: tryParseSimpleFormat(element)
            ?: emptyList()
    }

    /**
     * Format 1 (Current, 2023+):
     * [
     *   {
     *     "title": "",
     *     "media_list_data": [],
     *     "string_list_data": [
     *       { "href": "https://www.instagram.com/username", "value": "username", "timestamp": 1234567890 }
     *     ]
     *   },
     *   ...
     * ]
     */
    private fun tryParseCurrentFormat(element: JsonElement): List<ParsedUser>? {
        if (element !is JsonArray) return null

        val users = mutableListOf<ParsedUser>()
        for (item in element) {
            val obj = item as? JsonObject ?: continue
            val stringListData = obj["string_list_data"] as? JsonArray ?: continue

            for (entry in stringListData) {
                val entryObj = entry as? JsonObject ?: continue
                val user = extractUserFromEntry(entryObj)
                if (user != null) users.add(user)
            }
        }

        return users.takeIf { it.isNotEmpty() }
    }

    /**
     * Format 2 (Legacy wrapped):
     * {
     *   "relationships_following": [ ... same as format 1 ... ]
     * }
     * or
     * {
     *   "relationships_followers": [ ... ]
     * }
     */
    private fun tryParseLegacyWrappedFormat(element: JsonElement): List<ParsedUser>? {
        if (element !is JsonObject) return null

        val possibleKeys = listOf(
            "relationships_following",
            "relationships_followers",
            "followers",
            "following"
        )

        for (key in possibleKeys) {
            val array = element[key] as? JsonArray ?: continue
            val result = tryParseCurrentFormat(array)
            if (result != null) return result
        }

        return null
    }

    /**
     * Format 3 (Very old / simple):
     * [ { "username": "user1", "timestamp": 1234567890 }, ... ]
     */
    private fun tryParseSimpleFormat(element: JsonElement): List<ParsedUser>? {
        if (element !is JsonArray) return null

        val users = mutableListOf<ParsedUser>()
        for (item in element) {
            val obj = item as? JsonObject ?: continue
            val username = obj["username"]?.jsonPrimitive?.content
                ?: obj["value"]?.jsonPrimitive?.content
                ?: continue

            users.add(
                ParsedUser(
                    username = username,
                    profileUrl = obj["href"]?.jsonPrimitive?.content,
                    timestamp = obj["timestamp"]?.jsonPrimitive?.longOrNull
                )
            )
        }

        return users.takeIf { it.isNotEmpty() }
    }

    private fun extractUserFromEntry(entry: JsonObject): ParsedUser? {
        val value = entry["value"]?.jsonPrimitive?.content ?: return null
        val href = entry["href"]?.jsonPrimitive?.content
        val timestamp = entry["timestamp"]?.jsonPrimitive?.longOrNull

        return ParsedUser(
            username = value,
            profileUrl = href,
            timestamp = timestamp
        )
    }
}
