package com.unfollowlens.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.unfollowlens.data.db.entities.FollowerRecord
import com.unfollowlens.data.db.entities.ListType
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowerRecordDao {

    @Insert
    suspend fun insertAll(records: List<FollowerRecord>)

    @Query(
        "SELECT * FROM follower_records WHERE snapshotId = :snapshotId AND listType = :listType ORDER BY username ASC"
    )
    fun getBySnapshotAndType(snapshotId: Long, listType: ListType): Flow<List<FollowerRecord>>

    @Query(
        "SELECT username FROM follower_records WHERE snapshotId = :snapshotId AND listType = :listType"
    )
    suspend fun getUsernamesBySnapshotAndType(snapshotId: Long, listType: ListType): List<String>

    @Query(
        """SELECT * FROM follower_records 
           WHERE snapshotId = :snapshotId AND listType = :listType AND username IN (:usernames) 
           ORDER BY username ASC"""
    )
    fun getRecordsByUsernames(
        snapshotId: Long,
        listType: ListType,
        usernames: List<String>
    ): Flow<List<FollowerRecord>>

    @Query("DELETE FROM follower_records WHERE snapshotId = :snapshotId")
    suspend fun deleteBySnapshot(snapshotId: Long)

    @Query(
        """SELECT * FROM follower_records 
           WHERE snapshotId = :snapshotId AND listType = :listType 
           AND username LIKE '%' || :query || '%' 
           ORDER BY username ASC"""
    )
    fun searchByUsername(
        snapshotId: Long,
        query: String,
        listType: ListType
    ): Flow<List<FollowerRecord>>
}
