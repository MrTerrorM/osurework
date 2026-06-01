package com.example.osurework.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.osurework.data.local.entity.BeatmapAttributesEntity

@Database(
    entities = [BeatmapAttributesEntity::class],
    version = 15
)
abstract class AppDatabase : RoomDatabase() {

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