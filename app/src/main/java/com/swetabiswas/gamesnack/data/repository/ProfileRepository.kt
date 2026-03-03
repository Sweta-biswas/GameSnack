package com.swetabiswas.gamesnack.data.repository

import com.swetabiswas.gamesnack.data.local.dao.GameScoreDao
import com.swetabiswas.gamesnack.data.local.dao.PlayerProfileDao
import com.swetabiswas.gamesnack.data.local.entity.PlayerProfileEntity
import com.swetabiswas.gamesnack.gamification.BadgeSystem
import com.swetabiswas.gamesnack.gamification.XPSystem
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val profileDao: PlayerProfileDao,
    private val scoreDao: GameScoreDao,
    private val badgeSystem: BadgeSystem
) {
    fun getProfile(): Flow<PlayerProfileEntity?> = profileDao.getProfile()

    suspend fun ensureProfileExists() {
        val existing = profileDao.getProfileOnce()
        if (existing == null) {
            profileDao.insertOrUpdateProfile(PlayerProfileEntity(id = 1))
        }
    }

    suspend fun awardXp(xp: Int) {
        ensureProfileExists()
        profileDao.addXp(xp)

        // Recalculate level
        val profile = profileDao.getProfileOnce() ?: return
        val newLevel = XPSystem.levelFromXp(profile.totalXp)
        if (newLevel != profile.level) {
            profileDao.updateLevel(newLevel)
        }

        // Check daily streak
        updateStreak()

        // Check badge unlocks
        badgeSystem.checkAndAwardBadges()
    }

    private suspend fun updateStreak() {
        val profile = profileDao.getProfileOnce() ?: return
        val today   = todayStart()
        val lastDay = dayStart(profile.lastPlayedDate)

        when {
            // Played today already – no change
            lastDay == today -> return
            // Played yesterday – increment streak
            lastDay == today - DAY_MS -> {
                profileDao.updateStreak(profile.dailyStreak + 1, today)
            }
            // Missed a day – reset streak to 1
            else -> {
                profileDao.updateStreak(1, today)
            }
        }
    }

    private fun todayStart(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun dayStart(timestamp: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    companion object {
        private const val DAY_MS = 86_400_000L
    }
}
