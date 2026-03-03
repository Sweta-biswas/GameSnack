package com.swetabiswas.gamesnack.data.local.dao

import androidx.room.*
import com.swetabiswas.gamesnack.data.local.entity.BadgeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BadgeDao {

    @Query("SELECT * FROM badges")
    fun getAllBadges(): Flow<List<BadgeEntity>>

    @Query("SELECT * FROM badges WHERE id = :badgeId")
    suspend fun getBadgeById(badgeId: String): BadgeEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBadge(badge: BadgeEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllBadges(badges: List<BadgeEntity>)

    @Query("UPDATE badges SET earned = 1, earnedDate = :timestamp WHERE id = :badgeId AND earned = 0")
    suspend fun unlockBadge(badgeId: String, timestamp: Long)
}
