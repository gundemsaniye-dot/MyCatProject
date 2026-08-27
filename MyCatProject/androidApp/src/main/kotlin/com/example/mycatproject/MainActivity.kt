package com.example.mycatproject

import android.content.Context
import android.os.Bundle
import android.media.MediaPlayer
import android.os.Build
import android.os.SystemClock
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    private var backgroundMusic: MediaPlayer? = null
    private var soundEffect: MediaPlayer? = null
    private lateinit var vibrator: Vibrator
    private var lastBrushHapticAt = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        backgroundMusic = MediaPlayer.create(this, R.raw.scruffy_to_fluffy)?.apply {
            isLooping = true
            setVolume(BACKGROUND_MUSIC_VOLUME, BACKGROUND_MUSIC_VOLUME)
            start()
        }

        setContent {
            App(
                onBrushHaptic = ::playBrushHaptic,
                onCatTurnSound = { playRandomSoundEffect(SIDE_EYE_SOUNDS) },
                onCaughtSound = { playRandomSoundEffect(ANGRY_SOUNDS) },
            )
        }
    }

    private fun playRandomSoundEffect(resources: IntArray) {
        soundEffect?.release()
        soundEffect = MediaPlayer.create(this, resources.random())?.apply {
            setVolume(SOUND_EFFECT_VOLUME, SOUND_EFFECT_VOLUME)
            setOnCompletionListener { completedPlayer ->
                if (soundEffect === completedPlayer) soundEffect = null
                completedPlayer.release()
            }
            setOnErrorListener { failedPlayer, _, _ ->
                if (soundEffect === failedPlayer) soundEffect = null
                failedPlayer.release()
                true
            }
            start()
        }
    }

    private fun playBrushHaptic() {
        if (!vibrator.hasVibrator()) return

        val now = SystemClock.elapsedRealtime()
        if (now - lastBrushHapticAt < BRUSH_HAPTIC_INTERVAL_MS) return
        lastBrushHapticAt = now

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                vibrator.areEffectsSupported(VibrationEffect.EFFECT_TICK).firstOrNull() !=
                Vibrator.VIBRATION_EFFECT_SUPPORT_NO
            ) {
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
            } else {
                VibrationEffect.createOneShot(
                    BRUSH_HAPTIC_DURATION_MS,
                    BRUSH_HAPTIC_AMPLITUDE,
                )
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                vibrator.vibrate(
                    effect,
                    VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH),
                )
            } else {
                vibrator.vibrate(effect)
            }
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(BRUSH_HAPTIC_DURATION_MS)
        }
    }

    override fun onStart() {
        super.onStart()
        backgroundMusic?.takeIf { !it.isPlaying }?.start()
    }

    override fun onStop() {
        backgroundMusic?.pause()
        soundEffect?.release()
        soundEffect = null
        super.onStop()
    }

    override fun onDestroy() {
        backgroundMusic?.release()
        backgroundMusic = null
        soundEffect?.release()
        soundEffect = null
        super.onDestroy()
    }
}

private const val BRUSH_HAPTIC_INTERVAL_MS = 60L
private const val BRUSH_HAPTIC_DURATION_MS = 16L
private const val BRUSH_HAPTIC_AMPLITUDE = 150
private const val BACKGROUND_MUSIC_VOLUME = 0.40f
private const val SOUND_EFFECT_VOLUME = 1.0f

private val SIDE_EYE_SOUNDS = intArrayOf(
    R.raw.sideeyes_1,
    R.raw.sideeyes_2,
    R.raw.sideeyes_3,
    R.raw.sideeyes_4,
    R.raw.sideeyes_5,
)

private val ANGRY_SOUNDS = intArrayOf(
    R.raw.angry_1,
    R.raw.angry_2,
)

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
