package com.example.osurework.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "beatmap_attributes")
data class BeatmapAttributesEntity(
    @PrimaryKey val cacheKey: String,
    val maxCombo: Int,
    val starRating: Double,
    val aimDifficulty: Double,
    val aimDifficultSliderCount: Double,
    val speedDifficulty: Double,
    val speedNoteCount: Double,
    val sliderFactor: Double,
    val aimDifficultStrainCount: Double,
    val speedDifficultStrainCount: Double
)