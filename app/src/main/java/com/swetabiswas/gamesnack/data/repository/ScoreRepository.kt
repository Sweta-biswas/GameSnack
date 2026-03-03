package com.swetabiswas.gamesnack.data.repository

import com.swetabiswas.gamesnack.data.local.dao.GameScoreDao
import com.swetabiswas.gamesnack.data.local.entity.GameScoreEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScoreRepository @Inject constructor(
    private val scoreDao: GameScoreDao
) {
    fun getScoreForGame(gameId: String): Flow<GameScoreEntity?> =
        scoreDao.getScoreForGame(gameId)

    fun getAllScores(): Flow<List<GameScoreEntity>> =
        scoreDao.getAllScores()

    suspend fun recordScore(gameId: String, score: Int) {
        // Ensure row exists first
        val existing = scoreDao.getScoreForGameOnce(gameId)
        if (existing == null) {
            scoreDao.insertOrUpdateScore(GameScoreEntity(gameId = gameId, lastScore = score, bestScore = score, gamesPlayed = 1))
        } else {
            scoreDao.updateScore(gameId, score, System.currentTimeMillis())
        }
    }
}
