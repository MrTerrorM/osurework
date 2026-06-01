package com.example.osurework.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.osurework.data.local.AppDatabase
import com.example.osurework.data.repository.OsuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ScoreDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = OsuRepository(AppDatabase.getInstance(application))

    private val _maxCombo = MutableStateFlow<Int?>(null)
    val maxCombo: StateFlow<Int?> = _maxCombo

    fun loadBeatmapDetails(beatmapId: Int) {
        viewModelScope.launch {
            try {
                _maxCombo.value = repository.getBeatmapMaxCombo(beatmapId)
            } catch (e: Exception) {
                android.util.Log.e("ScoreDetail", "Błąd ładowania beatmapy: ${e.message}", e)
            }
        }
    }
}