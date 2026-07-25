package com.unfollowlens.di

import androidx.hilt.work.HiltWorker
import androidx.work.ListenableWorker
import com.unfollowlens.data.repository.SnapshotRepository
import com.unfollowlens.work.ReminderWorker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

// Note: In newer Hilt versions, we don't strictly need this binding for WorkManager
// if we use HiltWorkerFactory in the Application class.
// But to ensure it's provided correctly without extra setup, we inject via custom factory.
