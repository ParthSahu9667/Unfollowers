package com.unfollowlens.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.unfollowlens.data.db.dao.FollowerRecordDao
import com.unfollowlens.data.db.dao.SnapshotDao
import com.unfollowlens.data.db.entities.FollowerRecord
import com.unfollowlens.data.db.entities.ListType
import com.unfollowlens.data.db.entities.Snapshot

@Database(
    entities = [Snapshot::class, FollowerRecord::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun snapshotDao(): SnapshotDao
    abstract fun followerRecordDao(): FollowerRecordDao
}

class Converters {
    @TypeConverter
    fun fromListType(value: ListType): String = value.name

    @TypeConverter
    fun toListType(value: String): ListType = ListType.valueOf(value)
}
