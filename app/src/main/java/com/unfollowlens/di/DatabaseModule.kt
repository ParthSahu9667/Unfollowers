package com.unfollowlens.di

import android.content.Context
import androidx.room.Room
import com.unfollowlens.data.db.AppDatabase
import com.unfollowlens.data.db.dao.FollowerRecordDao
import com.unfollowlens.data.db.dao.SnapshotDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "unfollowlens_db"
        ).build()
    }

    @Provides
    fun provideSnapshotDao(database: AppDatabase): SnapshotDao = database.snapshotDao()

    @Provides
    fun provideFollowerRecordDao(database: AppDatabase): FollowerRecordDao =
        database.followerRecordDao()
}
