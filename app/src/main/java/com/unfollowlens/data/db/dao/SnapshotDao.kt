package com.unfollowlens.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.unfollowlens.data.db.entities.Snapshot
import kotlinx.coroutines.flow.Flow

@Dao
interface SnapshotDao {

    @Insert
    suspend fun insert(snapshot: Snapshot): Long

    @Query("SELECT * FROM snapshots ORDER BY importedAt DESC")
    fun getAllSnapshots(): Flow<List<Snapshot>>

    @Query("SELECT * FROM snapshots ORDER BY importedAt DESC LIMIT 1")
    suspend fun getLatestSnapshot(): Snapshot?

    @Query("SELECT * FROM snapshots ORDER BY importedAt DESC LIMIT 2")
    suspend fun getLatestTwoSnapshots(): List<Snapshot>

    @Query("SELECT * FROM snapshots WHERE id = :id")
    suspend fun getById(id: Long): Snapshot?

    @Delete
    suspend fun delete(snapshot: Snapshot)

    @Query("SELECT COUNT(*) FROM snapshots")
    suspend fun getCount(): Int

    @Query("DELETE FROM snapshots WHERE importedAt < :cutoffTimestamp")
    suspend fun deleteOlderThan(cutoffTimestamp: Long)
}
