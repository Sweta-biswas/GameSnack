package com.swetabiswas.gamesnack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Player",
    val totalXp: Int = 0,
    val level: Int = 1,
    val dailyStreak: Int = 0,
    val lastPlayedDate: Long = 0L
)
