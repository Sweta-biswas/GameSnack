package com.swetabiswas.gamesnack.data.repository

import com.swetabiswas.gamesnack.data.local.dao.BadgeDao
import com.swetabiswas.gamesnack.data.local.entity.BadgeEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BadgeRepository @Inject constructor(
    private val badgeDao: BadgeDao
) {
    fun getAllBadges(): Flow<List<BadgeEntity>> = badgeDao.getAllBadges()
}
