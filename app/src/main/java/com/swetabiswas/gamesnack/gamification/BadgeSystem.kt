package com.swetabiswas.gamesnack.gamification

import com.swetabiswas.gamesnack.data.local.dao.BadgeDao
import com.swetabiswas.gamesnack.data.local.dao.GameScoreDao
import com.swetabiswas.gamesnack.data.local.dao.PlayerProfileDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BadgeSystem @Inject constructor(
    private val badgeDao: BadgeDao,
    private val profileDao: PlayerProfileDao,
    private val scoreDao: GameScoreDao
) {
    suspend fun checkAndAwardBadges() {
        val now = System.currentTimeMillis()
        val profile = profileDao.getProfileOnce() ?: return

        // 🎮 Rookie – first game played
        val ttt      = scoreDao.getScoreForGameOnce("ttt")
        val math     = scoreDao.getScoreForGameOnce("math")
        val reaction = scoreDao.getScoreForGameOnce("reaction")
        val bottle   = scoreDao.getScoreForGameOnce("bottle")

        val totalGamesPlayed = listOf(ttt, math, reaction, bottle)
            .sumOf { it?.gamesPlayed ?: 0 }

        if (totalGamesPlayed >= 1)
            badgeDao.unlockBadge("first_game", now)

        // 🧮 Math Master – 30+ best score in Speed Math
        if ((math?.bestScore ?: 0) >= 30)
            badgeDao.unlockBadge("math_master", now)

        // ⚡ Reflex King – reaction time < 200ms (stored inverted: best score = 10000 - ms)
        // Reaction best score is stored as raw milliseconds; lower = better
        if ((reaction?.bestScore ?: Int.MAX_VALUE) in 1..199)
            badgeDao.unlockBadge("reflex_king", now)

        // 🏆 TicTacPro – win 10 games (TTT wins stored as bestScore tracks wins)
        if ((ttt?.gamesPlayed ?: 0) >= 10 && (ttt?.bestScore ?: 0) >= 10)
            badgeDao.unlockBadge("ttt_pro", now)

        // 🔥 Consistent – 5-day streak
        if (profile.dailyStreak >= 5)
            badgeDao.unlockBadge("daily_5", now)

        // ⭐ Rising Star – 500 total XP
        if (profile.totalXp >= 500)
            badgeDao.unlockBadge("xp_500", now)

        // 🎡 Party Starter – 20+ bottle spins
        if ((bottle?.gamesPlayed ?: 0) >= 20)
            badgeDao.unlockBadge("spinner", now)

        // 🎯 Level Up – reach level 5
        if (profile.level >= 5)
            badgeDao.unlockBadge("level_5", now)
    }
}
