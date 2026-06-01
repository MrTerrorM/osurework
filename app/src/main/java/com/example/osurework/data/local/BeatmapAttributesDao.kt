package com.example.osurework.data.local

import androidx.room.*
import com.example.osurework.data.local.entity.BeatmapAttributesEntity

@Dao
interface BeatmapAttributesDao {
    @Query("SELECT * FROM beatmap_attributes WHERE cacheKey = :key")
    suspend fun getByKey(key: String): BeatmapAttributesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BeatmapAttributesEntity)
}