package com.dlight.database.di

import android.content.Context
import androidx.room.Room
import com.dlight.database.DatabaseConstants.DATABASE_NAME
import com.dlight.database.TaskDatabase
import com.dlight.database.dao.TaskDao
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
    fun provideTaskDatabase(
        @ApplicationContext context: Context
    ): TaskDatabase {
        return Room.databaseBuilder(
            context,
            TaskDatabase::class.java,
            DATABASE_NAME
        )
            .fallbackToDestructiveMigration(false)
        .build()
    }
    @Provides
    fun provideTaskDao(
        database: TaskDatabase
    ): TaskDao {
        return database.taskDao()
    }
}
