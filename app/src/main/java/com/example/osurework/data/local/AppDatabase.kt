package com.example.osurework.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.osurework.data.local.entity.BeatmapAttributesEntity
import com.example.osurework.data.local.entity.BeatmapEntity

@Database(
    entities = [BeatmapEntity::class, BeatmapAttributesEntity::class],
    version = 9
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun beatmapDao(): BeatmapDao
    abstract fun beatmapAttributesDao(): BeatmapAttributesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "osu_rework_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}