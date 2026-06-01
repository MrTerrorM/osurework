package com.example.osurework.data.repository

import com.example.osurework.data.local.AppDatabase
import com.example.osurework.data.local.entity.BeatmapAttributesEntity
import com.example.osurework.data.remote.RetrofitInstance
import com.example.osurework.data.remote.dto.BeatmapAttributesData
import com.example.osurework.data.remote.dto.BeatmapAttributesRequest
import com.example.osurework.domain.calculator.ReworkCalculator
import com.example.osurework.domain.model.Player
import com.example.osurework.domain.model.Score

class OsuRepository(private val database: AppDatabase) {

    private var cachedToken: String? = null

    private suspend fun getToken(): String {
        cachedToken?.let { return it }
        val response = RetrofitInstance.authService.getToken(
            clientId = RetrofitInstance.CLIENT_ID,
            clientSecret = RetrofitInstance.CLIENT_SECRET
        )
        cachedToken = "Bearer ${response.access_token}"
        return cachedToken!!
    }

    suspend fun getPlayer(username: String): Player {
        val token = getToken()
        val dto = RetrofitInstance.apiService.getUser(token, username)
        return Player(
            id = dto.id,
            username = dto.username,
            avatarUrl = dto.avatarUrl,
            globalRank = dto.statistics.globalRank,
            pp = dto.statistics.pp,
            accuracy = dto.statistics.accuracy,
            playCount = dto.statistics.playCount
        )
    }

    suspend fun getTopScores(
        player: Player,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ): List<Score> {
        val token = getToken()
        val scoreDtos = RetrofitInstance.apiService.getTopScores(token, player.id)
        val total = scoreDtos.size

        return scoreDtos.mapIndexed { index, dto ->
            onProgress(index + 1, total)

            val mods = dto.mods
            val clockRate = getClockRate(mods)
            val baseOD = applyODMods(dto.beatmap.accuracy, mods)
            val baseAR = applyARMods(dto.beatmap.ar, mods)

            val attrs = fetchAttributes(dto.beatmap.id, mods)?.copy(
                overallDifficulty = calculateRateAdjustedOD(baseOD, clockRate),
                approachRate      = calculateRateAdjustedAR(baseAR, clockRate),
                hitCircleCount    = dto.beatmap.countCircles,
                sliderCount       = dto.beatmap.countSliders,
                spinnerCount      = dto.beatmap.countSpinners
            )

            val baseScore = Score(
                id              = dto.id,
                beatmapId       = dto.beatmap.id,
                beatmapTitle    = dto.beatmapSet.title,
                beatmapArtist   = dto.beatmapSet.artist,
                beatmapVersion  = dto.beatmap.version,
                coverUrl        = dto.beatmapSet.covers.cover,
                mods            = mods,
                accuracy        = dto.accuracy,
                maxCombo        = dto.maxCombo,
                beatmapMaxCombo = attrs?.maxCombo ?: dto.beatmap.maxCombo,
                rank            = dto.rank,
                pp              = dto.pp,
                starRating      = dto.beatmap.difficultyRating,
                adjustedStarRating = attrs?.starRating ?: dto.beatmap.difficultyRating,
                cs              = dto.beatmap.cs,
                ar              = dto.beatmap.ar,
                od              = dto.beatmap.accuracy,
                misses          = dto.statistics.count_miss,
                count300        = dto.statistics.count_300,
                count100        = dto.statistics.count_100,
                count50         = dto.statistics.count_50,
                reworkPp        = null
            )

            val reworkPp = attrs?.let { ReworkCalculator.calculate(it, baseScore) }
            baseScore.copy(reworkPp = reworkPp)
        }
    }

    private fun getClockRate(mods: List<String>): Double = when {
        mods.any { it == "DT" || it == "NC" } -> 1.5
        mods.any { it == "HT" || it == "DC" } -> 0.75
        else -> 1.0
    }

    private fun applyODMods(od: Double, mods: List<String>): Double = when {
        mods.contains("HR") -> (od * 1.4).coerceAtMost(10.0)
        mods.contains("EZ") -> od * 0.5
        else -> od
    }

    private fun applyARMods(ar: Double, mods: List<String>): Double = when {
        mods.contains("HR") -> (ar * 1.4).coerceAtMost(10.0)
        mods.contains("EZ") -> ar * 0.5
        else -> ar
    }

    private fun calculateRateAdjustedOD(baseOD: Double, clockRate: Double): Double {
        val hitWindow = (80.0 - 6.0 * baseOD) / clockRate
        return (80.0 - hitWindow) / 6.0
    }

    private fun calculateRateAdjustedAR(baseAR: Double, clockRate: Double): Double {
        val preempt = if (baseAR <= 5.0) 1800.0 - 120.0 * baseAR
        else 1950.0 - 150.0 * baseAR
        val adjusted = preempt / clockRate
        return if (adjusted >= 1200.0) (1800.0 - adjusted) / 120.0
        else (1950.0 - adjusted) / 150.0
    }

    private suspend fun fetchAttributes(beatmapId: Int, mods: List<String>): BeatmapAttributesData? {
        val cacheKey = "${beatmapId}_${mods.sorted().joinToString("")}"

        database.beatmapAttributesDao().getByKey(cacheKey)?.let { e ->
            return BeatmapAttributesData(
                maxCombo                     = e.maxCombo,
                aimDifficulty                = e.aimDifficulty,
                aimDifficultSliderCount      = e.aimDifficultSliderCount,
                speedDifficulty              = e.speedDifficulty,
                speedNoteCount               = e.speedNoteCount,
                sliderFactor                 = e.sliderFactor,
                aimDifficultStrainCount      = e.aimDifficultStrainCount,
                speedDifficultStrainCount    = e.speedDifficultStrainCount,
                flashlightDifficulty         = e.flashlightDifficulty,
                overallDifficulty            = e.overallDifficulty,
                approachRate                 = e.approachRate,
                drainRate                    = e.drainRate,
                hitCircleCount               = e.hitCircleCount,
                sliderCount                  = e.sliderCount,
                spinnerCount                 = e.spinnerCount,
                aimTopWeightedSliderFactor   = e.aimTopWeightedSliderFactor,
                speedTopWeightedSliderFactor = e.speedTopWeightedSliderFactor
            )
        }

        return try {
            val response = RetrofitInstance.apiService.getBeatmapAttributes(
                getToken(), beatmapId, BeatmapAttributesRequest(mods = mods)
            )
            val data = response.attributes
            database.beatmapAttributesDao().insert(
                BeatmapAttributesEntity(
                    cacheKey                     = cacheKey,
                    maxCombo                     = data.maxCombo,
                    aimDifficulty                = data.aimDifficulty,
                    aimDifficultSliderCount      = data.aimDifficultSliderCount,
                    speedDifficulty              = data.speedDifficulty,
                    speedNoteCount               = data.speedNoteCount,
                    sliderFactor                 = data.sliderFactor,
                    aimDifficultStrainCount      = data.aimDifficultStrainCount,
                    speedDifficultStrainCount    = data.speedDifficultStrainCount,
                    flashlightDifficulty         = data.flashlightDifficulty,
                    overallDifficulty            = data.overallDifficulty,
                    approachRate                 = data.approachRate,
                    drainRate                    = data.drainRate,
                    hitCircleCount               = data.hitCircleCount,
                    sliderCount                  = data.sliderCount,
                    spinnerCount                 = data.spinnerCount,
                    aimTopWeightedSliderFactor   = data.aimTopWeightedSliderFactor,
                    speedTopWeightedSliderFactor = data.speedTopWeightedSliderFactor
                )
            )
            data
        } catch (e: Exception) {
            null
        }
    }
}