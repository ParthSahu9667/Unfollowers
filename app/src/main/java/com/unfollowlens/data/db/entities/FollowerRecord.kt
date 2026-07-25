package com.unfollowlens.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single username entry from an Instagram export, tied to a [Snapshot].
 * Can be either a FOLLOWER or FOLLOWING record.
 */
@Entity(
    tableName = "follower_records",
    foreignKeys = [
        ForeignKey(
            entity = Snapshot::class,
            parentColumns = ["id"],
            childColumns = ["snapshotId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("snapshotId"),
        Index("username")
    ]
)
data class FollowerRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val snapshotId: Long,
    val username: String,
    val profileUrl: String? = null,
    val listType: ListType,
    val timestampFromExport: Long? = null
)
