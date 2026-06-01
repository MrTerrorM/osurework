package com.example.osurework.domain.model

data class Score(
    val id: Long,
    val beatmapId: Int,
    val beatmapTitle: String,
    val beatmapArtist: String,
    val beatmapVersion: String,
    val coverUrl: String?,
    val mods: List<String>,
    val accuracy: Double,
    val maxCombo: Int,
    val beatmapMaxCombo: Int?,
    val rank: String,
    val pp: Double?,
    val starRating: Double,
    val cs: Double,
    val ar: Double,
    val od: Double = 0.0,
    val misses: Int,
    val count300: Int = 0,
    val count100: Int = 0,
    val count50: Int = 0,
    val reworkPp: Double? = null
)