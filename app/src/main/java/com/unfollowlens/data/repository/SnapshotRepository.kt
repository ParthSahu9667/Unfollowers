package com.unfollowlens.data.repository

import android.net.Uri
import com.unfollowlens.data.db.dao.FollowerRecordDao
import com.unfollowlens.data.db.dao.SnapshotDao
import com.unfollowlens.data.db.entities.FollowerRecord
import com.unfollowlens.data.db.entities.ListType
import com.unfollowlens.data.db.entities.Snapshot
import com.unfollowlens.data.parser.ZipImportHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central repository for all snapshot and follower data operations.
 * Handles importing, set-diff computations, and history queries.
 */
@Singleton
class SnapshotRepository @Inject constructor(
    private val snapshotDao: SnapshotDao,
    private val followerRecordDao: FollowerRecordDao,
    private val zipImportHandler: ZipImportHandler
) {

    // ── Import ──────────────────────────────────────────────────────

    /**
     * Import from a ZIP archive URI. Creates a new Snapshot with all records.
     */
    suspend fun importFromZip(uri: Uri): Result<Snapshot> = withContext(Dispatchers.IO) {
        try {
            val parseResult = zipImportHandler.importFromZip(uri)

            val snapshot = Snapshot(
                followerCount = parseResult.followers.size,
                followingCount = parseResult.following.size
            )
            val snapshotId = snapshotDao.insert(snapshot)

            val followerRecords = parseResult.followers.map { user ->
                FollowerRecord(
                    snapshotId = snapshotId,
                    username = user.username,
                    profileUrl = user.profileUrl,
                    listType = ListType.FOLLOWER,
                    timestampFromExport = user.timestamp
                )
            }

            val followingRecords = parseResult.following.map { user ->
                FollowerRecord(
                    snapshotId = snapshotId,
                    username = user.username,
                    profileUrl = user.profileUrl,
                    listType = ListType.FOLLOWING,
                    timestampFromExport = user.timestamp
                )
            }

            followerRecordDao.insertAll(followerRecords + followingRecords)

            val savedSnapshot = snapshotDao.getById(snapshotId) ?: snapshot.copy(id = snapshotId)
            Result.success(savedSnapshot)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Set Operations ──────────────────────────────────────────────

    /** Users you follow that don't follow you back */
    suspend fun getNotFollowingBack(snapshotId: Long): Set<String> {
        val following = followerRecordDao
            .getUsernamesBySnapshotAndType(snapshotId, ListType.FOLLOWING).toHashSet()
        val followers = followerRecordDao
            .getUsernamesBySnapshotAndType(snapshotId, ListType.FOLLOWER).toHashSet()
        return following - followers
    }

    /** Users who follow you but you don't follow back (fans) */
    suspend fun getFans(snapshotId: Long): Set<String> {
        val followers = followerRecordDao
            .getUsernamesBySnapshotAndType(snapshotId, ListType.FOLLOWER).toHashSet()
        val following = followerRecordDao
            .getUsernamesBySnapshotAndType(snapshotId, ListType.FOLLOWING).toHashSet()
        return followers - following
    }

    /** Users you mutually follow */
    suspend fun getMutuals(snapshotId: Long): Set<String> {
        val followers = followerRecordDao
            .getUsernamesBySnapshotAndType(snapshotId, ListType.FOLLOWER).toHashSet()
        val following = followerRecordDao
            .getUsernamesBySnapshotAndType(snapshotId, ListType.FOLLOWING).toHashSet()
        return followers.intersect(following)
    }

    // ── Records by category ─────────────────────────────────────────

    fun getFollowerRecords(snapshotId: Long): Flow<List<FollowerRecord>> =
        followerRecordDao.getBySnapshotAndType(snapshotId, ListType.FOLLOWER)

    fun getFollowingRecords(snapshotId: Long): Flow<List<FollowerRecord>> =
        followerRecordDao.getBySnapshotAndType(snapshotId, ListType.FOLLOWING)

    fun getRecordsByUsernames(
        snapshotId: Long,
        listType: ListType,
        usernames: List<String>
    ): Flow<List<FollowerRecord>> =
        followerRecordDao.getRecordsByUsernames(snapshotId, listType, usernames)

    // ── History & Diffing ───────────────────────────────────────────

    data class SnapshotDiff(
        val newFollowers: Set<String>,
        val lostFollowers: Set<String>,
        val newFollowing: Set<String>,
        val stoppedFollowing: Set<String>
    )

    /** Compute the diff between two snapshots */
    suspend fun computeDiff(oldSnapshotId: Long, newSnapshotId: Long): SnapshotDiff {
        val oldFollowers = followerRecordDao
            .getUsernamesBySnapshotAndType(oldSnapshotId, ListType.FOLLOWER).toHashSet()
        val newFollowers = followerRecordDao
            .getUsernamesBySnapshotAndType(newSnapshotId, ListType.FOLLOWER).toHashSet()
        val oldFollowing = followerRecordDao
            .getUsernamesBySnapshotAndType(oldSnapshotId, ListType.FOLLOWING).toHashSet()
        val newFollowing = followerRecordDao
            .getUsernamesBySnapshotAndType(newSnapshotId, ListType.FOLLOWING).toHashSet()

        return SnapshotDiff(
            newFollowers = newFollowers - oldFollowers,
            lostFollowers = oldFollowers - newFollowers,
            newFollowing = newFollowing - oldFollowing,
            stoppedFollowing = oldFollowing - newFollowing
        )
    }

    // ── Snapshot Management ─────────────────────────────────────────

    fun getAllSnapshots(): Flow<List<Snapshot>> = snapshotDao.getAllSnapshots()

    suspend fun getLatestSnapshot(): Snapshot? = snapshotDao.getLatestSnapshot()

    suspend fun getLatestTwoSnapshots(): List<Snapshot> = snapshotDao.getLatestTwoSnapshots()

    suspend fun deleteSnapshot(snapshot: Snapshot) = snapshotDao.delete(snapshot)

    suspend fun getSnapshotCount(): Int = snapshotDao.getCount()

    /** Auto-prune snapshots older than 12 months */
    suspend fun pruneOldSnapshots() {
        val twelveMonthsMs = 365L * 24 * 60 * 60 * 1000
        val cutoff = System.currentTimeMillis() - twelveMonthsMs
        snapshotDao.deleteOlderThan(cutoff)
    }

    /** Delete all data */
    suspend fun clearAllData() {
        val snapshots = snapshotDao.getAllSnapshots().first()
        // Cascade delete handles follower records
        snapshots.forEach { snapshotDao.delete(it) }
    }
}
