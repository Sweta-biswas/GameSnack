package com.swetabiswas.gamesnack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "badges")
data class BadgeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val iconEmoji: String,
    val earned: Boolean = false,
    val earnedDate: Long = 0L
)
