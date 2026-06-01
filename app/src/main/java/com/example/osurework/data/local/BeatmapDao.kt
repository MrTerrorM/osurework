package com.example.osurework.data.local

import androidx.room.*
import com.example.osurework.data.local.entity.BeatmapEntity

@Dao
interface BeatmapDao {

    @Query("SELECT * FROM beatmaps WHERE id = :id")
    suspend fun getById(id: Int): BeatmapEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(beatmap: BeatmapEntity)
}