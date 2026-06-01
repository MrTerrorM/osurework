package com.example.osurework.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.osurework.domain.model.Score

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreDetailScreen(score: Score, navController: NavController) {

    val clockRate = when {
        score.mods.any { it == "DT" || it == "NC" } -> 1.5
        score.mods.any { it == "HT" || it == "DC" } -> 0.75
        else -> 1.0
    }

    val displayAR = run {
        var ar = score.ar
        if (score.mods.contains("HR")) ar = (ar * 1.4).coerceAtMost(10.0)
        if (score.mods.contains("EZ")) ar *= 0.5
        val preempt = if (ar <= 5.0) 1800.0 - 120.0 * ar else 1950.0 - 150.0 * ar
        val adjusted = preempt / clockRate
        if (adjusted >= 1200.0) (1800.0 - adjusted) / 120.0 else (1950.0 - adjusted) / 150.0
    }

    val displayCS = run {
        var cs = score.cs
        if (score.mods.contains("HR")) cs = (cs * 1.3).coerceAtMost(10.0)
        if (score.mods.contains("EZ")) cs *= 0.5
        cs
    }

    val displayOD = run {
        var od = score.od
        if (score.mods.contains("HR")) od = (od * 1.4).coerceAtMost(10.0)
        if (score.mods.contains("EZ")) od *= 0.5
        val hitWindow = (80.0 - 6.0 * od) / clockRate
        (80.0 - hitWindow) / 6.0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(score.beatmapTitle) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "${score.beatmapArtist} - ${score.beatmapTitle}",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "[${score.beatmapVersion}]",
                style = MaterialTheme.typography.titleMedium
            )

            HorizontalDivider()

            DetailRow("Mody", score.mods.ifEmpty { listOf("NM") }.joinToString())
            DetailRow("Accuracy", "${"%.2f".format(score.accuracy * 100)}%")
            DetailRow("Combo", "${score.maxCombo}x / ${score.beatmapMaxCombo?.let { "${it}x" } ?: "?"}")
            DetailRow("Misses", score.misses.toString())
            DetailRow("Star Rating", "${"%.2f".format(score.adjustedStarRating)}★")
            DetailRow("AR", "${"%.1f".format(displayAR)}")
            DetailRow("CS", "${"%.1f".format(displayCS)}")
            DetailRow("OD", "${"%.1f".format(displayOD)}")

            HorizontalDivider()

            DetailRow("Oficjalne pp", "${"%.2f".format(score.pp ?: 0.0)}pp")
            DetailRow("Rework pp", score.reworkPp?.let { "${"%.2f".format(it)}pp" } ?: "brak danych")
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}