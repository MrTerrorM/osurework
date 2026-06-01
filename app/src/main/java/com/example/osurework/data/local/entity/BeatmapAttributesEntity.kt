package com.example.osurework.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "beatmap_attributes")
data class BeatmapAttributesEntity(
    @PrimaryKey val cacheKey: String,
    val maxCombo: Int,
    val aimDifficulty: Double,
    val aimDifficultSliderCount: Double,
    val speedDifficulty: Double,
    val speedNoteCount: Double,
    val sliderFactor: Double,
    val aimDifficultStrainCount: Double,
    val speedDifficultStrainCount: Double,
    val flashlightDifficulty: Double,
    val overallDifficulty: Double,
    val approachRate: Double,
    val drainRate: Double,
    val hitCircleCount: Int,
    val sliderCount: Int,
    val spinnerCount: Int,
    val aimTopWeightedSliderFactor: Double,
    val speedTopWeightedSliderFactor: Double
)