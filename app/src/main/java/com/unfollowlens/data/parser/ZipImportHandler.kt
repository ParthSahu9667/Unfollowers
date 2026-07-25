package com.unfollowlens.data.parser

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles importing Instagram data from ZIP archives or individual JSON files.
 * Uses Storage Access Framework URIs — no broad storage permission needed.
 */
@Singleton
class ZipImportHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val parser: InstagramExportParser
) {

    /**
     * Import from a ZIP file URI. Scans entries for follower/following JSON files.
     */
    fun importFromZip(uri: Uri): ParseResult {
        val followerJsons = mutableListOf<String>()
        var followingJson: String? = null

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            ZipInputStream(inputStream).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val name = entry.name.lowercase()

                    when {
                        // Match followers_1.json, followers_2.json, etc.
                        name.contains("followers") && name.endsWith(".json") &&
                                !name.contains("following") -> {
                            followerJsons.add(readZipEntryContent(zip))
                        }
                        // Match following.json
                        name.contains("following") && name.endsWith(".json") -> {
                            followingJson = readZipEntryContent(zip)
                        }
                    }

                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        } ?: throw IllegalArgumentException("Could not open ZIP file")

        if (followerJsons.isEmpty() && followingJson == null) {
            throw IllegalArgumentException(
                "No follower/following data found in ZIP. " +
                "Make sure you exported 'Followers and following' from Instagram."
            )
        }

        val followers = parser.parseFollowers(*followerJsons.toTypedArray())
        val following = followingJson?.let { parser.parseFollowing(it) } ?: emptyList()

        return ParseResult(followers = followers, following = following)
    }

    /**
     * Import from individual JSON file URIs.
     */
    fun importFromJsonFiles(followerUris: List<Uri>, followingUri: Uri?): ParseResult {
        val followerJsons = followerUris.map { uri -> readUriContent(uri) }
        val followingJson = followingUri?.let { readUriContent(it) }

        val followers = parser.parseFollowers(*followerJsons.toTypedArray())
        val following = followingJson?.let { parser.parseFollowing(it) } ?: emptyList()

        return ParseResult(followers = followers, following = following)
    }

    private fun readZipEntryContent(zip: ZipInputStream): String {
        return BufferedReader(InputStreamReader(zip, Charsets.UTF_8)).readText()
    }

    private fun readUriContent(uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).readText()
        } ?: throw IllegalArgumentException("Could not read file: $uri")
    }
}
