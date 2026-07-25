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
     * Core parsing logic — uses a robust recursive search to find user nodes.
     * This makes it immune to wrapper/hierarchy changes by Instagram.
     */
    private fun parseUserList(content: String): List<ParsedUser> {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return emptyList()

        val element = try {
            json.parseToJsonElement(trimmed)
        } catch (e: Exception) {
            return emptyList()
        }

        val users = mutableListOf<ParsedUser>()
        extractUsersRecursively(element, users)
        
        // Remove duplicates just in case the JSON has duplicate entries
        return users.distinctBy { it.username }
    }

    private fun extractUsersRecursively(element: JsonElement, users: MutableList<ParsedUser>) {
        when (element) {
            is JsonArray -> {
                for (item in element) {
                    extractUsersRecursively(item, users)
                }
            }
            is JsonObject -> {
                // Check if this object looks like a user node
                val valueContent = element["value"]?.jsonPrimitive?.content
                val usernameContent = element["username"]?.jsonPrimitive?.content
                val href = element["href"]?.jsonPrimitive?.content
                val timestamp = element["timestamp"]?.jsonPrimitive?.longOrNull

                val username = valueContent ?: usernameContent

                // It is considered a user node if it has a username AND (an instagram href OR a timestamp)
                if (username != null && username.isNotBlank() && (href?.contains("instagram.com") == true || element.containsKey("timestamp"))) {
                    users.add(ParsedUser(username, href, timestamp))
                } else {
                    // Not a user node, recurse into its values
                    for (child in element.values) {
                        extractUsersRecursively(child, users)
                    }
                }
            }
            else -> {}
        }
    }
}
