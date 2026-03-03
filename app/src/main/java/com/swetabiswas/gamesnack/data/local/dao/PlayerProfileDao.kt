package com.swetabiswas.gamesnack.data.local.dao

import androidx.room.*
import com.swetabiswas.gamesnack.data.local.entity.PlayerProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerProfileDao {

    @Query("SELECT * FROM player_profile WHERE id = 1")
    fun getProfile(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = 1")
    suspend fun getProfileOnce(): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: PlayerProfileEntity)

    @Query("UPDATE player_profile SET totalXp = totalXp + :xp WHERE id = 1")
    suspend fun addXp(xp: Int)

    @Query("UPDATE player_profile SET level = :level WHERE id = 1")
    suspend fun updateLevel(level: Int)

    @Query("UPDATE player_profile SET dailyStreak = :streak, lastPlayedDate = :date WHERE id = 1")
    suspend fun updateStreak(streak: Int, date: Long)
}
