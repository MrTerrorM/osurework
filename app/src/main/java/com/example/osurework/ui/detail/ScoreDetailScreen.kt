package com.example.osurework.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
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

    Box(modifier = Modifier.fillMaxSize()) {
        if (score.listCoverUrl != null) {
            AsyncImage(
                model = score.listCoverUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Ciemny gradient od góry do dołu
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0xCC1B171C),
                            0.35f to Color(0xEE1B171C),
                            1.0f to Color(0xFF1B171C)
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            score.beatmapTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Wróć",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = "${score.beatmapArtist} - ${score.beatmapTitle}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "[${score.beatmapVersion}]",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color(0xCCFFFFFF)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                DetailRow("Mody",     score.mods.ifEmpty { listOf("NM") }.joinToString())
                DetailRow("Accuracy", "${"%.2f".format(score.accuracy * 100)}%")
                DetailRow("Combo",    "${score.maxCombo}x / ${score.beatmapMaxCombo?.let { "${it}x" } ?: "?"}")
                DetailRow("Misses",   score.misses.toString())
                DetailRow("★ Stars",  "${"%.2f".format(score.adjustedStarRating)}")
                DetailRow("AR",       "${"%.1f".format(displayAR)}")
                DetailRow("CS",       "${"%.1f".format(displayCS)}")
                DetailRow("OD",       "${"%.1f".format(displayOD)}")

                Spacer(modifier = Modifier.height(20.dp))

                HorizontalDivider(color = Color(0x44FFFFFF))

                Spacer(modifier = Modifier.height(16.dp))

                // PP porównanie
                val official = score.pp ?: 0.0
                val rework   = score.reworkPp
                val diff     = rework?.let { it - official }
                val diffColor = if (diff == null || diff >= 0) Color(0xFF6EE87A) else Color(0xFFFF6B6B)
                val arrow     = if (diff == null || diff >= 0) "↑" else "↓"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Oficjalne",
                            style = MaterialTheme.typography.labelMedium.copy(color = Color(0x99FFFFFF))
                        )
                        Text(
                            text = "${"%.2f".format(official)}pp",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    if (rework != null && diff != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Rework",
                                style = MaterialTheme.typography.labelMedium.copy(color = Color(0xCCB57FD4))
                            )
                            Text(
                                text = "$arrow ${"%.2f".format(rework)}pp",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color = diffColor,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            val diffText = if (diff >= 0) "+${"%.2f".format(diff)}" else "${"%.2f".format(diff)}"
                            Text(
                                text = diffText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = diffColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xAAFFFFFF))
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        )
    }
}