package com.swetabiswas.gamesnack.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swetabiswas.gamesnack.data.local.entity.BadgeEntity
import com.swetabiswas.gamesnack.data.local.entity.GameScoreEntity
import com.swetabiswas.gamesnack.data.local.entity.PlayerProfileEntity
import com.swetabiswas.gamesnack.data.repository.BadgeRepository
import com.swetabiswas.gamesnack.data.repository.ProfileRepository
import com.swetabiswas.gamesnack.data.repository.ScoreRepository
import com.swetabiswas.gamesnack.gamification.XPSystem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val profile: PlayerProfileEntity = PlayerProfileEntity(),
    val gameScores: List<GameScoreEntity> = emptyList(),
    val badges: List<BadgeEntity> = emptyList(),
    val levelProgress: Float = 0f,
    val xpToNext: Int = 100,
    val xpWithinLevel: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val scoreRepository: ScoreRepository,
    private val badgeRepository: BadgeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            profileRepository.ensureProfileExists()
        }
        collectData()
    }

    private fun collectData() {
        viewModelScope.launch {
            combine(
                profileRepository.getProfile().filterNotNull(),
                scoreRepository.getAllScores(),
                badgeRepository.getAllBadges()
            ) { profile, scores, badges ->
                HomeUiState(
                    profile       = profile,
                    gameScores    = scores,
                    badges        = badges,
                    levelProgress = XPSystem.levelProgress(profile.totalXp),
                    xpToNext      = XPSystem.xpToNextLevel(profile.totalXp),
                    xpWithinLevel = XPSystem.xpWithinLevel(profile.totalXp)
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
