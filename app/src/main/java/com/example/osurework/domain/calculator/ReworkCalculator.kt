package com.example.osurework.domain.calculator

import com.example.osurework.data.remote.dto.BeatmapAttributesData
import com.example.osurework.domain.model.Score
import kotlin.math.*

class ReworkCalculator private constructor(
    private val attrs: BeatmapAttributesData,
    private val score: Score
) {
    private val usingClassicSliderAccuracy = true

    private val countGreat = score.count300
    private val countOk    = score.count100
    private val countMeh   = score.count50
    private val countMiss  = score.misses
    private val scoreMaxCombo = score.maxCombo
    private val accuracy   = score.accuracy

    private val countSliderTickMiss    = 0
    private val countSliderEndsDropped = 0

    private val totalHits           get() = countGreat + countOk + countMeh + countMiss
    private val totalSuccessfulHits get() = countGreat + countOk + countMeh
    private val totalImperfectHits  get() = countOk + countMeh + countMiss

    private val clockRate: Double = when {
        score.mods.any { it.equals("DT", true) || it.equals("NC", true) } -> 1.5
        score.mods.any { it.equals("HT", true) } -> 0.75
        else -> 1.0
    }

    private val overallDifficulty = attrs.overallDifficulty
    private val approachRate      = attrs.approachRate

    private val baseOD = when {
        score.mods.any { it.equals("HR", true) } -> minOf(score.od * 1.4, 10.0)
        score.mods.any { it.equals("EZ", true) } -> score.od * 0.5
        else -> score.od
    }
    private val greatHitWindow = (80.0 - 6.0  * baseOD) / clockRate
    private val okHitWindow    = (140.0 - 8.0  * baseOD) / clockRate
    private val mehHitWindow   = (200.0 - 10.0 * baseOD) / clockRate

    private val isRelax     = score.mods.any { it.equals("RX", true) }
    private val isAutopilot = score.mods.any { it.equals("AP", true) }
    private val isNoFail    = score.mods.any { it.equals("NF", true) }
    private val isSpunOut   = score.mods.any { it.equals("SO", true) }
    private val isFL        = score.mods.any { it.equals("FL", true) }
    private val isHD        = score.mods.any { it.equals("HD", true) }
    private val isBlinds    = score.mods.any { it.equals("BL", true) }

    private var effectiveMissCount          = 0.0
    private var speedDeviation: Double?     = null
    private var aimEstimatedSliderBreaks    = 0.0
    private var speedEstimatedSliderBreaks  = 0.0

    fun calculate(): Double {
        if (totalHits == 0) return 0.0

        android.util.Log.d("CALC_WINDOWS", """
        overallDifficulty=$overallDifficulty
        clockRate=$clockRate
        greatHitWindow=$greatHitWindow
        odBase=$baseOD
        okHitWindow=$okHitWindow
        mehHitWindow=$mehHitWindow
        hitCircleCount=${attrs.hitCircleCount}
        totalHits=$totalHits
        count300=$countGreat  count100=$countOk  count50=$countMeh
        """.trimIndent())

        effectiveMissCount = calculateComboBasedEstimatedMissCount()
        effectiveMissCount = max(countMiss.toDouble(), effectiveMissCount)
        effectiveMissCount = min(totalHits.toDouble(), effectiveMissCount)

        var multiplier = PERFORMANCE_BASE_MULTIPLIER

        if (isNoFail)
            multiplier *= max(0.90, 1.0 - 0.02 * effectiveMissCount)

        if (isSpunOut && totalHits > 0)
            multiplier *= 1.0 - (attrs.spinnerCount.toDouble() / totalHits).pow(0.85)

        if (isRelax) {
            val okMult  = 0.75 * max(0.0, if (overallDifficulty > 0.0) 1.0 - overallDifficulty / 13.33 else 1.0)
            val mehMult = max(0.0, if (overallDifficulty > 0.0) 1.0 - (overallDifficulty / 13.33).pow(5.0) else 1.0)
            effectiveMissCount = min(
                effectiveMissCount + countOk * okMult + countMeh * mehMult,
                totalHits.toDouble()
            )
        }

        speedDeviation = calculateSpeedDeviation()

        val aimValue        = if (!isAutopilot) computeAimValue() else 0.0
        val speedValue      = if (!isRelax && speedDeviation != null) computeSpeedValue() else 0.0
        val accuracyValue   = if (!isRelax) computeAccuracyValue() else 0.0
        val flashlightValue = if (isFL) computeFlashlightValue() else 0.0

        // ==================== DEBUG DEBUG DEBUG ====================
        val finalPp = (aimValue.pow(1.1) + speedValue.pow(1.1) +
                accuracyValue.pow(1.1) + flashlightValue.pow(1.1))
            .pow(1.0 / 1.1) * multiplier

        println("=".repeat(85))
        println("REWORK CALCULATOR DEBUG")
        println("Mods       : ${score.mods.joinToString()}")
        println("Combo      : $scoreMaxCombo / ${attrs.maxCombo}")
        println("Accuracy   : ${"%.2f".format(accuracy * 100)}%")
        println("Hits       : 300x$countGreat  100x$countOk  50x$countMeh  Missx$countMiss")
        println("effMiss    : ${"%.3f".format(effectiveMissCount)}")
        println("speedDev   : ${speedDeviation?.let { "%.2f ms".format(it) } ?: "null"}")
        println("─".repeat(85))
        println("AIM        : ${"%.2f".format(aimValue)}")
        println("SPEED      : ${"%.2f".format(speedValue)}")
        println("ACCURACY   : ${"%.2f".format(accuracyValue)}")
        println("FLASHLIGHT : ${"%.2f".format(flashlightValue)}")
        println("Multiplier : ${"%.4f".format(multiplier)}")
        println("→ FINAL PP : ${"%.2f".format(finalPp)}")
        println("=".repeat(85))
        // =======================================================

        return (
                aimValue.pow(1.1) + speedValue.pow(1.1) +
                        accuracyValue.pow(1.1) + flashlightValue.pow(1.1)
                ).pow(1.0 / 1.1) * multiplier
    }

    private fun computeAimValue(): Double {
        var aimDifficulty = attrs.aimDifficulty

        if (attrs.sliderCount > 0 && attrs.aimDifficultSliderCount > 0.0) {
            val maxDropped = totalImperfectHits
            val estimatedImproper = min(
                maxDropped.toDouble(),
                (attrs.maxCombo - scoreMaxCombo).toDouble()
            ).coerceIn(0.0, attrs.aimDifficultSliderCount)

            val sliderNerfFactor = (1.0 - attrs.sliderFactor) *
                    (1.0 - estimatedImproper / attrs.aimDifficultSliderCount).pow(3.0) +
                    attrs.sliderFactor
            aimDifficulty *= sliderNerfFactor
        }

        var aimValue = difficultyToPerformance(aimDifficulty)
        aimValue *= lengthBonus(totalHits)

        if (effectiveMissCount > 0.0) {
            aimEstimatedSliderBreaks = calculateEstimatedSliderBreaks(attrs.aimTopWeightedSliderFactor)
            val relevantMissCount = min(
                effectiveMissCount + aimEstimatedSliderBreaks,
                (totalImperfectHits + countSliderTickMiss).toDouble()
            )
            aimValue *= calculateMissPenalty(relevantMissCount, attrs.aimDifficultStrainCount)
        }

        aimValue *= getComboScalingFactor()

        if (isBlinds)
            aimValue *= 1.3 + (totalHits * (0.0016 / (1 + 2 * effectiveMissCount)) *
                    accuracy.pow(16.0)) * (1.0 - 0.003 * attrs.drainRate * attrs.drainRate)

        aimValue *= accuracy
        return aimValue
    }

    private fun computeSpeedValue(): Double {
        var speedValue = difficultyToPerformance(attrs.speedDifficulty)
        speedValue *= lengthBonus(totalHits)

        if (effectiveMissCount > 0.0) {
            speedEstimatedSliderBreaks = calculateEstimatedSliderBreaks(attrs.speedTopWeightedSliderFactor)
            val relevantMissCount = min(
                effectiveMissCount + speedEstimatedSliderBreaks,
                (totalImperfectHits + countSliderTickMiss).toDouble()
            )
            speedValue *= calculateMissPenalty(relevantMissCount, attrs.speedDifficultStrainCount)
        }

        speedValue *= getComboScalingFactor()

        if (isBlinds) speedValue *= 1.12

        speedValue *= calculateSpeedHighDeviationNerf()

        // POPRAWKA: Usunięto rzutowanie doInt() aby zapobiec dramatycznej utracie precyzji w obliczeniach
        val relDiff  = max(0.0, totalHits.toDouble() - attrs.speedNoteCount)
        val relGreat = max(0.0, countGreat - relDiff)
        val relOk    = max(0.0, countOk  - max(0.0, relDiff - countGreat))
        val relMeh   = max(0.0, countMeh - max(0.0, relDiff - countGreat - countOk))
        val relAcc   = if (attrs.speedNoteCount <= 0.0) 0.0
        else (relGreat * 6.0 + relOk * 2.0 + relMeh) / (attrs.speedNoteCount * 6.0)

        speedValue *= ((accuracy + relAcc) / 2.0).pow((14.5 - overallDifficulty) / 2.0)
        return speedValue
    }

    private fun computeAccuracyValue(): Double {
        val circles = attrs.hitCircleCount

        val betterAcc = if (circles > 0) {
            val maxPossible = max(totalHits - circles, 0)
            val numerator = (countGreat - maxPossible) * 6.0 + countOk * 2.0 + countMeh
            max(0.0, numerator / (circles * 6.0))
        } else 0.0

        var accValue = 1.52163.pow(overallDifficulty) * betterAcc.pow(24.0) * 2.83
        accValue *= min(1.15, (circles / 1000.0).pow(0.3))

        if (isBlinds)
            accValue *= 1.14
        else if (isHD)
            accValue *= 1.0 + 0.08 * reverseLerp(approachRate, 11.5, 10.0)

        if (isFL) accValue *= 1.02

        return accValue
    }

    private fun computeFlashlightValue(): Double {
        // POPRAWKA: Flashlight używa zupełnie innej formuły (diff^2 * 25.0) niż standardowe skille!
        var flValue = attrs.flashlightDifficulty.pow(2.0) * 25.0

        if (effectiveMissCount > 0.0)
            flValue *= 0.97 * (1.0 - (effectiveMissCount / totalHits).pow(0.775))
                .pow(effectiveMissCount.pow(0.875))

        flValue *= getComboScalingFactor()
        flValue *= 0.5 + accuracy / 2.0
        return flValue
    }

    private fun calculateComboBasedEstimatedMissCount(): Double {
        if (attrs.sliderCount <= 0) return countMiss.toDouble()

        var missCount = countMiss.toDouble()

        val fullComboThreshold = attrs.maxCombo - 0.1 * attrs.sliderCount

        if (scoreMaxCombo < fullComboThreshold)
            missCount = fullComboThreshold / max(1.0, scoreMaxCombo.toDouble())

        missCount = min(missCount, totalImperfectHits.toDouble())

        // POPRAWKA: Zabezpieczenie na wypadek, gdyby scoreMaxCombo było o dziwo wyższe od maxCombo z atrybutów
        val maxPossibleSliderBreaks = min(attrs.sliderCount, max(0, (attrs.maxCombo - scoreMaxCombo) / 2))
        val sliderBreaks = missCount - countMiss

        if (sliderBreaks > maxPossibleSliderBreaks)
            missCount = (countMiss + maxPossibleSliderBreaks).toDouble()

        return missCount
    }

    private fun calculateEstimatedSliderBreaks(topWeightedSliderFactor: Double): Double {
        if (!usingClassicSliderAccuracy || countOk == 0) return 0.0

        val missedComboPercent = 1.0 - scoreMaxCombo.toDouble() / attrs.maxCombo
        var estimated = min(countOk.toDouble(), effectiveMissCount * topWeightedSliderFactor)

        val okAdjustment = ((countOk - estimated) + 0.5) / countOk
        estimated *= smoothstep(effectiveMissCount, 1.0, 2.0)

        return estimated * okAdjustment * logistic(missedComboPercent, 0.33, 15.0)
    }

    private fun calculateSpeedDeviation(): Double? {
        if (totalSuccessfulHits == 0) return null

        var snc = attrs.speedNoteCount
        snc += (totalHits - attrs.speedNoteCount) * 0.1

        val miss  = min(countMiss.toDouble(), snc)
        val meh   = min(countMeh.toDouble(),  snc - miss)
        val ok    = min(countOk.toDouble(),   snc - miss - meh)
        val great = max(0.0, snc - miss - meh - ok)

        return calculateDeviation(great, ok, meh)
    }

    private fun calculateDeviation(great: Double, ok: Double, meh: Double): Double? {
        if (great + ok + meh <= 0.0) return null

        val n = max(1.0, great + ok)
        val p = great / n
        val z = 2.32634787404

        var pLow = (n * p + z * z / 2.0) / (n + z * z) -
                z / (n + z * z) * sqrt(n * p * (1.0 - p) + z * z / 4.0)
        pLow = min(p, pLow)

        val dev = if (pLow > 0.01) {
            var d = greatHitWindow / (sqrt(2.0) * erfInv(pLow))
            val tail = sqrt(2.0 / PI) * okHitWindow *
                    exp(-0.5 * (okHitWindow / d).pow(2.0)) /
                    (d * erf(okHitWindow / (sqrt(2.0) * d)))
            d * sqrt(1.0 - tail)
        } else {
            okHitWindow / sqrt(3.0)
        }

        val mehVar = (mehHitWindow * mehHitWindow + okHitWindow * mehHitWindow + okHitWindow * okHitWindow) / 3.0

        return sqrt(((great + ok) * dev.pow(2.0) + meh * mehVar) / (great + ok + meh))
    }

    private fun calculateSpeedHighDeviationNerf(): Double {
        val dev = speedDeviation ?: return 0.0

        val sv = difficultyToPerformance(attrs.speedDifficulty)
        val cutoff = 100.0 + 220.0 * (22.0 / dev).pow(6.5)

        if (sv <= cutoff) return 1.0

        val scale = 50.0
        val adjusted = scale * (ln((sv - cutoff) / scale + 1.0) + cutoff / scale)

        val lerp = 1.0 - reverseLerp(dev, 22.0, 27.0)
        val lerpedValue = adjusted + (sv - adjusted) * lerp

        return lerpedValue / sv
    }

    private fun getComboScalingFactor(): Double {
        if (attrs.maxCombo <= 0) return 1.0
        val fullComboScaling = min(
            scoreMaxCombo.toDouble().pow(0.8) / attrs.maxCombo.toDouble().pow(0.8),
            1.0
        )
        val comboWeight = 0.17
        return (1.0 - comboWeight) + fullComboScaling * comboWeight
    }

    companion object {
        private const val PERFORMANCE_BASE_MULTIPLIER = 1.14

        fun calculate(attrs: BeatmapAttributesData, score: Score): Double =
            ReworkCalculator(attrs, score).calculate()

        fun difficultyToPerformance(diff: Double): Double =
            (5.0 * max(1.0, diff / 0.0675) - 4.0).pow(3.0) / 100000.0

        fun lengthBonus(totalHits: Int): Double =
            0.95 + 0.4 * min(1.0, totalHits / 2000.0) +
                    if (totalHits > 2000) log10(totalHits / 2000.0) * 0.5 else 0.0

        fun calculateMissPenalty(missCount: Double, difficultStrainCount: Double): Double {
            // POPRAWKA: Zabezpieczenie strainCount przed logarytmem dającym liczby ujemne, 0, lub NaN.
            val safeStrainCount = max(1.1, difficultStrainCount)
            return 0.96 / (missCount / (4.0 * ln(safeStrainCount).pow(0.94)) + 1.0)
        }

        fun reverseLerp(value: Double, start: Double, end: Double): Double =
            ((value - start) / (end - start)).coerceIn(0.0, 1.0)

        fun smoothstep(x: Double, edge0: Double, edge1: Double): Double {
            val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0.0, 1.0)
            return t * t * (3.0 - 2.0 * t)
        }

        fun logistic(x: Double, midpoint: Double, multiplier: Double): Double =
            1.0 / (1.0 + exp(multiplier * (midpoint - x)))

        fun erf(x: Double): Double {
            val t = 1.0 / (1.0 + 0.3275911 * abs(x))
            val p = t * (0.254829592 + t * (-0.284496736 + t * (1.421413741 +
                    t * (-1.453152027 + t * 1.061405429))))
            return x.sign * (1.0 - p * exp(-x * x))
        }

        fun erfInv(x: Double): Double {
            val a = 0.147
            val ln1 = ln(1.0 - x * x)
            val b = 2.0 / (PI * a) + ln1 / 2.0
            return x.sign * sqrt(sqrt(b * b - ln1 / a) - b)
        }
    }
}