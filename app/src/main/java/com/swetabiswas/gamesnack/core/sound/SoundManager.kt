package com.swetabiswas.gamesnack.core.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.media.AudioManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SoundManager – plays short UI sound effects via SoundPool.
 * Uses system sound URIs so no raw audio files are needed.
 * Respects the isSoundEnabled preference from callers.
 */
@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    // Load system effect sounds from the Android framework resource path
    private val clickSoundId  = loadSystemSound("Effect_Tick")
    private val winSoundId    = loadSystemSound("Effect_Tick")   // overridden below
    private val loseSoundId   = loadSystemSound("Effect_Tick")
    private val tapSoundId    = loadSystemSound("Effect_Tick")

    // Instead of external files, use AudioManager beeps or SoundPool with generated tones.
    // We use a helper that creates sounds purely from the SoundPool.
    private val sounds = mutableMapOf<SoundType, Int>()

    init {
        loadSounds()
    }

    private fun loadSounds() {
        // Load sounds from Android's built-in sound effects directory
        val effectsDir = "/system/media/audio/ui/"
        val files = mapOf(
            SoundType.CLICK  to "${effectsDir}KeypressStandard.ogg",
            SoundType.WIN    to "${effectsDir}VideoRecord.ogg",
            SoundType.LOSE   to "${effectsDir}KeypressDelete.ogg",
            SoundType.TAP    to "${effectsDir}KeypressReturn.ogg",
            SoundType.SPIN   to "${effectsDir}ScrollBarminLandscape.ogg",
            SoundType.CORRECT to "${effectsDir}VideoRecord.ogg",
            SoundType.WRONG  to "${effectsDir}KeypressDelete.ogg",
            SoundType.START  to "${effectsDir}KeypressSpacebar.ogg"
        )

        files.forEach { (type, path) ->
            try {
                val id = soundPool.load(path, 1)
                if (id > 0) sounds[type] = id
            } catch (_: Exception) { /* silently skip if file not available on device */ }
        }
    }

    private fun loadSystemSound(name: String): Int {
        return try {
            soundPool.load("/system/media/audio/ui/$name.ogg", 1)
        } catch (_: Exception) { 0 }
    }

    /** Play a sound effect if sound is enabled. */
    fun play(type: SoundType, isSoundEnabled: Boolean = true) {
        if (!isSoundEnabled) return
        val id = sounds[type] ?: return
        soundPool.play(id, 1f, 1f, 1, 0, when (type) {
            SoundType.WIN    -> 1.2f
            SoundType.LOSE   -> 0.8f
            SoundType.SPIN   -> 0.9f
            else             -> 1.0f
        })
    }

    fun release() {
        soundPool.release()
    }
}

enum class SoundType {
    CLICK,   // Generic button press
    TAP,     // Tap on game element
    WIN,     // Win / correct answer
    LOSE,    // Game over / wrong
    CORRECT, // Right answer in speed math
    WRONG,   // Wrong answer in speed math
    SPIN,    // Bottle spinning
    START    // Game start
}
