package com.swetabiswas.gamesnack.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.swetabiswas.gamesnack.data.local.dao.BadgeDao
import com.swetabiswas.gamesnack.data.local.dao.GameScoreDao
import com.swetabiswas.gamesnack.data.local.dao.PlayerProfileDao
import com.swetabiswas.gamesnack.data.local.entity.BadgeEntity
import com.swetabiswas.gamesnack.data.local.entity.GameScoreEntity
import com.swetabiswas.gamesnack.data.local.entity.PlayerProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PlayerProfileEntity::class,
        GameScoreEntity::class,
        BadgeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GameSnackDatabase : RoomDatabase() {

    abstract fun playerProfileDao(): PlayerProfileDao
    abstract fun gameScoreDao(): GameScoreDao
    abstract fun badgeDao(): BadgeDao

    companion object {
        @Volatile
        private var INSTANCE: GameSnackDatabase? = null

        fun getDatabase(context: Context): GameSnackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameSnackDatabase::class.java,
                    "gamesnack_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-populate default data on first launch
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    // Insert default player profile
                                    database.playerProfileDao().insertOrUpdateProfile(
                                        PlayerProfileEntity(id = 1)
                                    )
                                    // Insert default game score rows
                                    val games = listOf("ttt", "math", "reaction", "bottle")
                                    games.forEach { gameId ->
                                        database.gameScoreDao().insertOrUpdateScore(
                                            GameScoreEntity(gameId = gameId)
                                        )
                                    }
                                    // Insert all badges
                                    database.badgeDao().insertAllBadges(defaultBadges())
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private fun defaultBadges(): List<BadgeEntity> = listOf(
            BadgeEntity("first_game",  "Rookie",         "Play your first game",          "🎮"),
            BadgeEntity("math_master", "Math Master",    "Score 30+ in Speed Math",       "🧮"),
            BadgeEntity("reflex_king", "Reflex King",    "React in under 200ms",          "⚡"),
            BadgeEntity("ttt_pro",     "TicTacPro",      "Win 10 Tic Tac Toe games",      "🏆"),
            BadgeEntity("daily_5",     "Consistent",     "Maintain a 5-day streak",       "🔥"),
            BadgeEntity("xp_500",      "Rising Star",    "Earn 500 total XP",             "⭐"),
            BadgeEntity("spinner",     "Party Starter",  "Spin the bottle 20 times",      "🎡"),
            BadgeEntity("level_5",     "Level Up!",      "Reach Level 5",                 "🎯")
        )
    }
}
