package com.swetabiswas.gamesnack.data.local.dao

import androidx.room.*
import com.swetabiswas.gamesnack.data.local.entity.GameScoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameScoreDao {

    @Query("SELECT * FROM game_scores WHERE gameId = :gameId")
    fun getScoreForGame(gameId: String): Flow<GameScoreEntity?>

    @Query("SELECT * FROM game_scores WHERE gameId = :gameId")
    suspend fun getScoreForGameOnce(gameId: String): GameScoreEntity?

    @Query("SELECT * FROM game_scores")
    fun getAllScores(): Flow<List<GameScoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateScore(score: GameScoreEntity)

    @Query("""
        UPDATE game_scores 
        SET lastScore = :score,
            bestScore = CASE WHEN :score > bestScore THEN :score ELSE bestScore END,
            gamesPlayed = gamesPlayed + 1,
            lastPlayedTimestamp = :timestamp
        WHERE gameId = :gameId
    """)
    suspend fun updateScore(gameId: String, score: Int, timestamp: Long)
}
