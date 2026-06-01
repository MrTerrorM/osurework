package com.example.osurework.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "beatmaps")
data class BeatmapEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val artist: String,
    val version: String,
    val coverUrl: String?,
    val maxCombo: Int?,
    val starRating: Double,
    val cs: Double,
    val ar: Double
)