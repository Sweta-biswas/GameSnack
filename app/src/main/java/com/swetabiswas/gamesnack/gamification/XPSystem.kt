package com.swetabiswas.gamesnack.gamification

object XPSystem {

    /** XP threshold to reach given level. Level 1 = 0 XP base. */
    fun xpForLevel(level: Int): Int {
        if (level <= 1) return 0
        var total = 0
        for (i in 1 until level) {
            total += i * 100
        }
        return total
    }

    /** Compute level from total XP */
    fun levelFromXp(totalXp: Int): Int {
        var level = 1
        while (totalXp >= xpForLevel(level + 1)) {
            level++
            if (level >= 100) break  // safety cap
        }
        return level
    }

    /** How much XP within current level (0..xpForNextLevel-1) */
    fun xpWithinLevel(totalXp: Int): Int {
        val level = levelFromXp(totalXp)
        return totalXp - xpForLevel(level)
    }

    /** XP needed to go from current level to next */
    fun xpToNextLevel(totalXp: Int): Int {
        val level = levelFromXp(totalXp)
        return xpForLevel(level + 1) - xpForLevel(level)
    }

    /** Progress fraction [0f..1f] within current level */
    fun levelProgress(totalXp: Int): Float {
        val within = xpWithinLevel(totalXp).toFloat()
        val needed = xpToNextLevel(totalXp).toFloat()
        return if (needed == 0f) 1f else (within / needed).coerceIn(0f, 1f)
    }

    // ── XP rewards ────────────────────────────────────────────────────────────
    const val XP_TTT_WIN       = 15
    const val XP_TTT_DRAW      = 5
    const val XP_MATH_CORRECT  = 2   // per correct answer
    const val XP_REACTION_BEST = 20  // beating personal best
    const val XP_REACTION_PLAY = 5
    const val XP_BOTTLE_SPIN   = 3
}
