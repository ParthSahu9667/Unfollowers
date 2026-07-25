package com.unfollowlens.data.parser

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.longOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parses Instagram's data export JSON files into [ParsedUser] lists.
 *
 * Two distinct real-world formats confirmed from an actual July 2026 export:
 *
 * FOLLOWERS (followers_1.json) — Root is a JsonArray:
 * [
 *   {
 *     "title": "",
 *     "media_list_data": [],
 *     "string_list_data": [
 *       { "href": "https://www.instagram.com/username", "value": "username", "timestamp": 123 }
 *     ]
 *   }, ...
 * ]
 * --> username comes from string_list_data[0].value
 *
 * FOLLOWING (following.json) — Root is a JsonObject with a key wrapper:
 * {
 *   "relationships_following": [
 *     {
 *       "title": "username",          <-- username is in the OUTER title, NOT in string_list_data
 *       "string_list_data": [
 *         { "href": "https://www.instagram.com/_u/username", "timestamp": 123 }
 *         // NOTE: NO "value" field in this format!
 *       ]
 *     }, ...
 *   ]
 * }
 * --> username comes from the outer object's "title" field
 */
@Singleton
class InstagramExportParser @Inject constructor() {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    fun parseFollowers(vararg jsonContents: String): List<ParsedUser> =
        jsonContents.flatMap { parseUserList(it) }

    fun parseFollowing(jsonContent: String): List<ParsedUser> =
        parseUserList(jsonContent)

    private fun parseUserList(content: String): List<ParsedUser> {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return emptyList()

        val element = try {
            json.parseToJsonElement(trimmed)
        } catch (e: Exception) {
            return emptyList()
        }

        val users = mutableListOf<ParsedUser>()

        // Unwrap top-level object if needed (e.g. {"relationships_following": [...]})
        val arrays: List<JsonArray> = when (element) {
            is JsonArray -> listOf(element)
            is JsonObject -> element.values.filterIsInstance<JsonArray>()
            else -> emptyList()
        }

        for (array in arrays) {
            for (item in array) {
                if (item !is JsonObject) continue
                parseEntryObject(item)?.let { users.add(it) }
            }
        }

        return users.distinctBy { it.username }
    }

    /**
     * Parses a single entry object which may be in one of two formats:
     *
     * Format A (followers_1.json):
     *   { "title": "", "string_list_data": [{ "value": "username", "href": "...", "timestamp": 123 }] }
     *   Username is in string_list_data[0].value
     *
     * Format B (following.json):
     *   { "title": "username", "string_list_data": [{ "href": ".../_u/username", "timestamp": 123 }] }
     *   Username is in the outer object's "title" field
     */
    private fun parseEntryObject(obj: JsonObject): ParsedUser? {
        val stringListData = obj["string_list_data"] as? JsonArray ?: return null
        val firstEntry = stringListData.firstOrNull() as? JsonObject ?: return null

        val value = (firstEntry["value"] as? JsonPrimitive)?.content?.trim()
        val href = (firstEntry["href"] as? JsonPrimitive)?.content
        val timestamp = (firstEntry["timestamp"] as? JsonPrimitive)?.longOrNull
        val title = (obj["title"] as? JsonPrimitive)?.content?.trim()

        // Username resolution:
        // Format A has a non-empty "value" in string_list_data → use that
        // Format B has no "value" at all → fall back to outer "title"
        val username = when {
            !value.isNullOrBlank() -> value
            !title.isNullOrBlank() -> title
            else -> return null
        }

        // Normalize profile URL: following.json uses /instagram.com/_u/username
        val cleanHref = href?.replace("instagram.com/_u/", "instagram.com/")

        return ParsedUser(
            username = username,
            profileUrl = cleanHref,
            timestamp = timestamp
        )
    }
}
