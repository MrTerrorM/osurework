package com.example.osurework.domain.model

data class Score(
    val id: Long,
    val beatmapId: Int,
    val beatmapTitle: String,
    val beatmapArtist: String,
    val beatmapVersion: String,
    val coverUrl: String?,
    val listCoverUrl: String?,
    val mods: List<String>,
    val accuracy: Double,
    val maxCombo: Int,
    val beatmapMaxCombo: Int?,
    val rank: String,
    val pp: Double?,
    val starRating: Double,
    val adjustedStarRating: Double,
    val cs: Double,
    val ar: Double,
    val od: Double,
    val misses: Int,
    val count300: Int,
    val count100: Int,
    val count50: Int,
    val reworkPp: Double? = null
)