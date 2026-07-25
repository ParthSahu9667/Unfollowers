package com.unfollowlens.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a point-in-time import of Instagram follower/following data.
 * Each import creates one Snapshot, which owns many [FollowerRecord]s.
 */
@Entity(tableName = "snapshots")
data class Snapshot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val importedAt: Long = System.currentTimeMillis(),
    val followerCount: Int = 0,
    val followingCount: Int = 0
)
