package com.example.osurework.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.osurework.domain.model.Score

@Composable
fun SearchScreen(navController: NavController, viewModel: SearchViewModel) {
    val state by viewModel.state.collectAsState()
    val progressMessage by viewModel.progressMessage.collectAsState()
    var username by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "osu! rework",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nick gracza") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor    = MaterialTheme.colorScheme.primary,
                cursorColor          = MaterialTheme.colorScheme.primary,
                focusedTextColor     = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor   = MaterialTheme.colorScheme.onBackground
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.searchPlayer(username) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Szukaj", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val s = state) {
            is SearchState.Idle -> {}

            is SearchState.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    if (progressMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = progressMessage,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            is SearchState.Error -> {
                Text(
                    "Błąd: ${s.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }

            is SearchState.Success -> {
                // Nagłówek gracza
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = s.player.avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = s.player.username,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = "#${s.player.globalRank ?: "?"} · ${"%.0f".format(s.player.pp)}pp",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Sort toggle
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (s.sortMode == SortMode.Official) "Oficjalne" else "Rework",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Switch(
                            checked = s.sortMode == SortMode.Rework,
                            onCheckedChange = {
                                viewModel.changeSortMode(
                                    if (it) SortMode.Rework else SortMode.Official
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor  = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor  = MaterialTheme.colorScheme.primary,
                                uncheckedTrackColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(s.scores) { index, score ->
                        ScoreCard(
                            score = score,
                            index = index,
                            onClick = {
                                viewModel.selectScore(score)
                                navController.navigate("detail")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreCard(score: Score, index: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        // Cover jako tło
        if (score.coverUrl != null) {
            AsyncImage(
                model = score.coverUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Gradient overlay dla czytelności
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xE0150F18),
                            Color(0xA0150F18),
                            Color(0x60150F18)
                        )
                    )
                )
        )

        // Treść
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Numer
            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color(0x99FFFFFF),
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.width(28.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${score.beatmapArtist} - ${score.beatmapTitle}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = "[${score.beatmapVersion}]  ·  ${score.mods.ifEmpty { listOf("NM") }.joinToString()}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xCCFFFFFF)
                    ),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                val official = score.pp ?: 0.0
                val rework   = score.reworkPp

                Text(
                    text = "${"%.2f".format(official)}pp",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xAAFFFFFF))
                )

                if (rework != null) {
                    val diff  = rework - official
                    val color = if (diff >= 0) Color(0xFF6EE87A) else Color(0xFFFF6B6B)
                    val arrow = if (diff >= 0) "↑" else "↓"
                    val diffText = if (diff >= 0) "+${"%.2f".format(diff)}" else "${"%.2f".format(diff)}"

                    Text(
                        text = "$arrow ${"%.2f".format(rework)}pp",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = diffText,
                        style = MaterialTheme.typography.labelSmall.copy(color = color)
                    )
                }
            }
        }
    }
}