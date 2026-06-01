package com.example.osurework.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BeatmapAttributesRequest(
    @SerializedName("ruleset_id") val rulesetId: Int = 0,
    val mods: List<String> = emptyList()
)

data class BeatmapAttributesResponse(
    val attributes: BeatmapAttributesData
)

data class BeatmapAttributesData(
    @SerializedName("star_rating")                  val starRating: Double = 0.0,
    @SerializedName("max_combo")                    val maxCombo: Int = 0,
    @SerializedName("aim_difficulty")               val aimDifficulty: Double = 0.0,
    @SerializedName("aim_difficult_slider_count")   val aimDifficultSliderCount: Double = 0.0,
    @SerializedName("speed_difficulty")             val speedDifficulty: Double = 0.0,
    @SerializedName("speed_note_count")             val speedNoteCount: Double = 0.0,
    @SerializedName("slider_factor")                val sliderFactor: Double = 1.0,
    @SerializedName("aim_difficult_strain_count")   val aimDifficultStrainCount: Double = 10.0,
    @SerializedName("speed_difficult_strain_count") val speedDifficultStrainCount: Double = 10.0,
    @SerializedName("flashlight_difficulty")        val flashlightDifficulty: Double = 0.0,
    @SerializedName("overall_difficulty")           val overallDifficulty: Double = 0.0,
    @SerializedName("approach_rate")                val approachRate: Double = 0.0,
    @SerializedName("drain_rate")                   val drainRate: Double = 0.0,
    @SerializedName("hit_circle_count")             val hitCircleCount: Int = 0,
    @SerializedName("slider_count")                 val sliderCount: Int = 0,
    @SerializedName("spinner_count")                val spinnerCount: Int = 0,
    @SerializedName("aim_top_weighted_slider_factor")   val aimTopWeightedSliderFactor: Double = 0.0,
    @SerializedName("speed_top_weighted_slider_factor") val speedTopWeightedSliderFactor: Double = 0.0
)