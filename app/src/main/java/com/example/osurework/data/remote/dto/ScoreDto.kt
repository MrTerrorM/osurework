package com.example.osurework.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ScoreDto(
    val id: Long,
    val pp: Double?,
    val accuracy: Double,
    @SerializedName("max_combo") val maxCombo: Int,
    val rank: String,
    val mods: List<String>,
    val beatmap: BeatmapDto,
    @SerializedName("beatmapset") val beatmapSet: BeatmapSetDto,
    val statistics: ScoreStatisticsDto
)

data class BeatmapDto(
    val id: Int,
    val version: String,
    @SerializedName("total_length") val totalLength: Int,
    @SerializedName("max_combo") val maxCombo: Int?,
    val cs: Double,
    val ar: Double,
    val accuracy: Double,
    val drain: Double,
    @SerializedName("difficulty_rating") val difficultyRating: Double,
    @SerializedName("count_circles") val countCircles: Int = 0,
    @SerializedName("count_sliders") val countSliders: Int = 0,
    @SerializedName("count_spinners") val countSpinners: Int = 0
)

data class BeatmapSetDto(
    val id: Int,
    val title: String,
    val artist: String,
    @SerializedName("covers") val covers: CoversDto
)

data class CoversDto(
    @SerializedName("cover@2x") val cover: String?
)

data class ScoreStatisticsDto(
    val count_300: Int,
    val count_100: Int,
    val count_50: Int,
    val count_miss: Int
)