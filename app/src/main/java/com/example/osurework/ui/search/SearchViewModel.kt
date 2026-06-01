package com.example.osurework.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.osurework.data.local.AppDatabase
import com.example.osurework.data.repository.OsuRepository
import com.example.osurework.domain.model.Player
import com.example.osurework.domain.model.Score
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SortMode { Official, Rework }

sealed class SearchState {
    object Idle : SearchState()
    object Loading : SearchState()
    data class Success(
        val player: Player,
        val scores: List<Score>,
        val sortMode: SortMode
    ) : SearchState()
    data class Error(val message: String) : SearchState()
}

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = OsuRepository(AppDatabase.getInstance(application))

    private val _state = MutableStateFlow<SearchState>(SearchState.Idle)
    val state: StateFlow<SearchState> = _state

    private val _selectedScore = MutableStateFlow<Score?>(null)
    val selectedScore: StateFlow<Score?> = _selectedScore

    private val _progressMessage = MutableStateFlow("")
    val progressMessage: StateFlow<String> = _progressMessage

    private var allScores: List<Score> = emptyList()

    fun searchPlayer(username: String) {
        viewModelScope.launch {
            _state.value = SearchState.Loading
            _progressMessage.value = "Pobieranie danych gracza..."
            try {
                val player = repository.getPlayer(username)
                _progressMessage.value = "Pobieranie top plays..."

                val scores = repository.getTopScores(player) { current, total ->
                    _progressMessage.value = "Przetwarzanie beatmap: $current / $total"
                }

                allScores = scores
                _progressMessage.value = ""
                _state.value = SearchState.Success(
                    player = player,
                    scores = sortScores(scores, SortMode.Official),
                    sortMode = SortMode.Official
                )
            } catch (e: Exception) {
                _progressMessage.value = ""
                _state.value = SearchState.Error(e.message ?: "Nieznany błąd")
            }
        }
    }

    fun changeSortMode(newMode: SortMode) {
        val current = _state.value
        if (current is SearchState.Success) {
            _state.update {
                SearchState.Success(
                    player = current.player,
                    scores = sortScores(allScores, newMode),
                    sortMode = newMode
                )
            }
        }
    }

    private fun sortScores(scores: List<Score>, mode: SortMode): List<Score> =
        when (mode) {
            SortMode.Official -> scores.sortedByDescending { it.pp ?: 0.0 }
            SortMode.Rework   -> scores.sortedByDescending { it.reworkPp ?: it.pp ?: 0.0 }
        }

    fun selectScore(score: Score) {
        _selectedScore.value = score
    }
}