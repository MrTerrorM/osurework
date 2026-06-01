package com.example.osurework.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.osurework.domain.model.Score

@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var username by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nick gracza") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.searchPlayer(username) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Szukaj")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val s = state) {
            is SearchState.Idle -> {}

            is SearchState.Loading -> {
                val progress by viewModel.progressMessage.collectAsState()
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = progress,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            is SearchState.Error -> {
                Text(text = "Błąd: ${s.message}", color = MaterialTheme.colorScheme.error)
            }

            is SearchState.Success -> {
                Text(
                    text = "${s.player.username} | #${s.player.globalRank} | ${String.format("%.2f", s.player.pp)}pp",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        val newMode = if (s.sortMode == SortMode.Official) SortMode.Rework else SortMode.Official
                        viewModel.changeSortMode(newMode)
                    }) {
                        Text(
                            text = when (s.sortMode) {
                                SortMode.Official -> "Sortuj: Oficjalne pp ▼"
                                SortMode.Rework -> "Sortuj: Rework pp ▼"
                            }
                        )
                    }
                }

                LazyColumn {
                    items(s.scores.withIndex().toList()) { (index, score) ->
                        ScoreCard(score, index + 1, viewModel, navController)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreCard(
    score: Score,
    position: Int,
    viewModel: SearchViewModel,
    navController: NavController
) {
    Card(
        onClick = {
            viewModel.selectScore(score)
            navController.navigate("detail")
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "#$position",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("${score.beatmapArtist} - ${score.beatmapTitle}")
                    Text("[${score.beatmapVersion}]", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            PpComparisonRow(score)
        }
    }
}

@Composable
private fun PpComparisonRow(score: Score) {
    val officialPp = score.pp ?: 0.0
    val reworkPp = score.reworkPp ?: officialPp
    val diff = reworkPp - officialPp
    val color = if (diff >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${String.format("%.2f", officialPp)}pp",
            style = MaterialTheme.typography.titleMedium
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.padding(horizontal = 8.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${String.format("%.2f", reworkPp)}pp",
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
            Text(
                text = if (diff >= 0) "(+${String.format("%.2f", diff)})"
                else "(${String.format("%.2f", diff)})",
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
        }

        Text(
            text = score.mods.ifEmpty { listOf("NM") }.joinToString(" + "),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}