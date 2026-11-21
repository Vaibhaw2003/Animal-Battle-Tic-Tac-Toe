package com.example.neontictactoe.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

interface SoundFeedbackController {
  fun setSoundEnabled(enabled: Boolean)

  fun setVibrationEnabled(enabled: Boolean)

  fun playButtonClick()

  fun playMovePlayer1()

  fun playMovePlayer2()

  fun playWin()

  fun playDraw()

  fun playGameOver()

  fun vibrateMove()

  fun vibrateWin()

  fun release()
}

class SoundFeedbackManager(
  context: Context,
) : SoundFeedbackController {

  private val appContext = context.applicationContext
  @Volatile private var soundEnabled = true
  @Volatile private var vibrationEnabled = true

  private val audioExecutor: ExecutorService = Executors.newSingleThreadExecutor()
  private val pendingCount = AtomicInteger(0)
  private val playbackGeneration = AtomicInteger(0)

  private val vibrator: Vibrator? by lazy {
    runCatching {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager =
          appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
      } else {
        @Suppress("DEPRECATION")
        appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
      }
    }.getOrNull()
  }

  override fun setSoundEnabled(enabled: Boolean) {
    soundEnabled = enabled
    if (!enabled) {
      playbackGeneration.incrementAndGet()
    }
  }

  override fun setVibrationEnabled(enabled: Boolean) {
    vibrationEnabled = enabled
  }

  override fun playButtonClick() {
    if (!soundEnabled) return
    playSequence(notes = listOf(640.0 to 36), interruptPrevious = false)
  }

  override fun playMovePlayer1() {
    if (!soundEnabled) return
    playSequence(notes = listOf(523.25 to 45, 783.99 to 60), interruptPrevious = false)
  }

  override fun playMovePlayer2() {
    if (!soundEnabled) return
    playSequence(notes = listOf(440.0 to 45, 587.33 to 60), interruptPrevious = false)
  }

  override fun playWin() {
    if (!soundEnabled) return
    playSequence(
      notes =
        listOf(
          523.25 to 75,
          659.25 to 75,
          783.99 to 85,
          1046.50 to 165,
        ),
      interruptPrevious = true,
    )
  }

  override fun playDraw() {
    if (!soundEnabled) return
    playSequence(notes = listOf(392.0 to 80, 349.23 to 120), interruptPrevious = true)
  }

  override fun playGameOver() {
    if (!soundEnabled) return
    playSequence(notes = listOf(440.0 to 80, 349.23 to 80, 261.63 to 150), interruptPrevious = true)
  }

  override fun vibrateMove() {
    triggerVibration(durationMs = 22L, amplitude = 90)
  }

  override fun vibrateWin() {
    triggerVibration(durationMs = 70L, amplitude = 180)
  }

  private fun triggerVibration(durationMs: Long, amplitude: Int) {
    if (!vibrationEnabled) return
    runCatching {
      val vib = vibrator ?: return
      if (!vib.hasVibrator()) return
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vib.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
      } else {
        @Suppress("DEPRECATION")
        vib.vibrate(durationMs)
      }
    }
  }

  private fun playSequence(
    notes: List<Pair<Double, Int>>,
    interruptPrevious: Boolean,
  ) {
    if (!interruptPrevious && pendingCount.get() >= 2) {
      return
    }
    val token =
      if (interruptPrevious) {
        playbackGeneration.incrementAndGet()
      } else {
        playbackGeneration.get()
      }
    pendingCount.incrementAndGet()

    runCatching {
      audioExecutor.execute {
        var track: AudioTrack? = null
        try {
          if (!soundEnabled || playbackGeneration.get() != token) return@execute

          val sampleRate = 22050
          val totalSamples = notes.sumOf { (sampleRate * it.second) / 1000 }
          if (totalSamples <= 0) return@execute
          val pcm = ShortArray(totalSamples)
          var offset = 0

          for ((freqHz, durationMs) in notes) {
            val count = (sampleRate * durationMs) / 1000
            for (i in 0 until count) {
              val t = i.toDouble() / sampleRate
              val env = exp(-4.0 * i.toDouble() / count.coerceAtLeast(1))
              val wave = sin(2.0 * PI * freqHz * t) * 0.45 + sin(4.0 * PI * freqHz * t) * 0.12
              pcm[offset + i] =
                (wave * env * Short.MAX_VALUE)
                  .toInt()
                  .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                  .toShort()
            }
            offset += count
          }

          val bufferBytes = pcm.size * 2
          track =
            AudioTrack.Builder()
              .setAudioAttributes(
                AudioAttributes.Builder()
                  .setUsage(AudioAttributes.USAGE_GAME)
                  .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                  .build()
              )
              .setAudioFormat(
                AudioFormat.Builder()
                  .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                  .setSampleRate(sampleRate)
                  .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                  .build()
              )
              .setBufferSizeInBytes(bufferBytes)
              .setTransferMode(AudioTrack.MODE_STATIC)
              .build()

          if (track.state != AudioTrack.STATE_INITIALIZED) return@execute
          track.write(pcm, 0, pcm.size)
          track.play()

          val totalMs = notes.sumOf { it.second }.toLong() + 25L
          var elapsed = 0L
          while (elapsed < totalMs && soundEnabled && playbackGeneration.get() == token) {
            val step = minOf(20L, totalMs - elapsed)
            Thread.sleep(step)
            elapsed += step
          }
          if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
            track.stop()
          }
        } catch (_: Throwable) {
          // Ignore transient audio device errors during rapid transitions or shutdown
        } finally {
          runCatching { track?.release() }
          pendingCount.decrementAndGet()
        }
      }
    }.onFailure {
      pendingCount.decrementAndGet()
    }
  }

  override fun release() {
    playbackGeneration.incrementAndGet()
    runCatching { audioExecutor.shutdownNow() }
  }
}
