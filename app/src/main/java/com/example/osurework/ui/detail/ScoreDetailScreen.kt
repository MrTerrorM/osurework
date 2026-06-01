package com.example.osurework.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.osurework.domain.model.Score

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreDetailScreen(
    score: Score,
    navController: NavController
) {
    val viewModel: ScoreDetailViewModel = viewModel()
    val maxCombo by viewModel.maxCombo.collectAsState()

    LaunchedEffect(score.beatmapId) {
        viewModel.loadBeatmapDetails(score.beatmapId)
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
            DetailRow("Accuracy", "${String.format("%.2f", score.accuracy * 100)}%")
            DetailRow("Combo", "${score.maxCombo}x / ${maxCombo?.let { "${it}x" } ?: "..."}")
            DetailRow("Misses", score.misses.toString())
            DetailRow("Star Rating", "${String.format("%.2f", score.starRating)}★")
            DetailRow("AR", score.ar.toString())
            DetailRow("CS", score.cs.toString())

            HorizontalDivider()

            DetailRow("Oficjalne pp", "${String.format("%.2f", score.pp ?: 0.0)}pp")
            DetailRow("Rework pp", score.reworkPp?.let { "${String.format("%.2f", it)}pp" } ?: "wkrótce")
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