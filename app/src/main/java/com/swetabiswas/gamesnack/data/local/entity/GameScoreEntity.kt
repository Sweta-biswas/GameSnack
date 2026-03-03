package com.swetabiswas.gamesnack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_scores")
data class GameScoreEntity(
    @PrimaryKey val gameId: String,   // "ttt", "math", "reaction", "bottle"
    val bestScore: Int = 0,
    val gamesPlayed: Int = 0,
    val lastScore: Int = 0,
    val lastPlayedTimestamp: Long = 0L
)
