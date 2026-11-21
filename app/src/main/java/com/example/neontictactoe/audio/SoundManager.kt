package com.example.neontictactoe.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.R
import java.util.EnumMap
import java.util.concurrent.ConcurrentHashMap

enum class GameSoundEffect(
  val resId: Int,
  val cooldownMs: Long,
  val priority: Int,
  val isOutcomeSound: Boolean = false,
) {
  BUTTON_CLICK(R.raw.sfx_button_click, cooldownMs = 65L, priority = 1),
  GAME_START(R.raw.sfx_game_start, cooldownMs = 180L, priority = 2, isOutcomeSound = true),
  ANIMAL_SELECT(R.raw.sfx_animal_select, cooldownMs = 85L, priority = 2),
  LION_MOVE(R.raw.sfx_lion_move, cooldownMs = 70L, priority = 2),
  WOLF_MOVE(R.raw.sfx_wolf_move, cooldownMs = 70L, priority = 2),
  AI_MOVE(R.raw.sfx_ai_move, cooldownMs = 70L, priority = 2),
  WIN(R.raw.sfx_win, cooldownMs = 350L, priority = 3, isOutcomeSound = true),
  DRAW(R.raw.sfx_draw, cooldownMs = 350L, priority = 3, isOutcomeSound = true),
  GAME_OVER(R.raw.sfx_game_over, cooldownMs = 350L, priority = 3, isOutcomeSound = true),
  PLAY_AGAIN(R.raw.sfx_play_again, cooldownMs = 180L, priority = 3, isOutcomeSound = true),
  VICTORY_CELEBRATION(
    R.raw.sfx_victory_celebration,
    cooldownMs = 450L,
    priority = 4,
    isOutcomeSound = true,
  ),
}

data class AudioSettings(
  val soundEnabled: Boolean = true,
  val musicEnabled: Boolean = true,
  val sfxEnabled: Boolean = true,
  val masterVolume: Float = 1.0f,
  val musicVolume: Float = 0.55f,
  val sfxVolume: Float = 1.0f,
)

interface SoundFeedbackController {
  fun updateAudioSettings(settings: AudioSettings)

  fun setSoundEnabled(enabled: Boolean)

  fun setMusicEnabled(enabled: Boolean)

  fun setSfxEnabled(enabled: Boolean)

  fun setMasterVolume(volume: Float)

  fun setMusicVolume(volume: Float)

  fun setSfxVolume(volume: Float)

  fun setVibrationEnabled(enabled: Boolean)

  fun onAppForeground()

  fun onAppBackground()

  fun startBackgroundMusic()

  fun stopBackgroundMusic()

  fun playButtonClick()

  fun playGameStart()

  fun playAnimalSelect()

  fun playLionMove()

  fun playWolfMove()

  fun playAiMove()

  fun playMovePlayer1()

  fun playMovePlayer2()

  fun playWin()

  fun playDraw()

  fun playGameOver()

  fun playPlayAgain()

  fun playVictoryCelebration()

  fun vibrateMove()

  fun vibrateWin()

  fun release()
}

internal interface SfxEngine {
  fun setOnLoadCompleteListener(listener: (sampleId: Int, status: Int) -> Unit)

  fun load(context: Context, resId: Int): Int

  fun play(sampleId: Int, leftVolume: Float, rightVolume: Float, priority: Int): Int

  fun setVolume(streamId: Int, leftVolume: Float, rightVolume: Float)

  fun stop(streamId: Int)

  fun release()
}

internal interface BgmEngine {
  fun startOrResume(context: Context, resId: Int, volume: Float): Boolean

  fun setVolume(volume: Float)

  fun pause()

  fun stop()

  fun isPlaying(): Boolean

  fun release()
}

internal interface AudioFocusGateway {
  fun requestFocus(onFocusChange: (Int) -> Unit): Boolean

  fun abandonFocus()
}

private class AndroidSfxEngine : SfxEngine {
  private val soundPool: SoundPool =
    SoundPool.Builder()
      .setMaxStreams(6)
      .setAudioAttributes(
        AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_GAME)
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .build()
      )
      .build()

  override fun setOnLoadCompleteListener(listener: (sampleId: Int, status: Int) -> Unit) {
    soundPool.setOnLoadCompleteListener { _, sampleId, status -> listener(sampleId, status) }
  }

  override fun load(context: Context, resId: Int): Int {
    if (resId == 0) return 0
    return runCatching { soundPool.load(context, resId, 1) }.getOrDefault(0)
  }

  override fun play(sampleId: Int, leftVolume: Float, rightVolume: Float, priority: Int): Int {
    if (sampleId <= 0) return 0
    return runCatching {
      soundPool.play(
        sampleId,
        leftVolume.coerceIn(0f, 1f),
        rightVolume.coerceIn(0f, 1f),
        priority,
        0,
        1.0f,
      )
    }.getOrDefault(0)
  }

  override fun setVolume(streamId: Int, leftVolume: Float, rightVolume: Float) {
    if (streamId <= 0) return
    runCatching {
      soundPool.setVolume(
        streamId,
        leftVolume.coerceIn(0f, 1f),
        rightVolume.coerceIn(0f, 1f),
      )
    }
  }

  override fun stop(streamId: Int) {
    if (streamId <= 0) return
    runCatching { soundPool.stop(streamId) }
  }

  override fun release() {
    runCatching { soundPool.setOnLoadCompleteListener(null) }
    runCatching { soundPool.release() }
  }
}

private class AndroidBgmEngine : BgmEngine {
  private var mediaPlayer: MediaPlayer? = null
  private var isPrepared: Boolean = false
  private var currentResId: Int = 0

  override fun startOrResume(context: Context, resId: Int, volume: Float): Boolean {
    if (resId == 0) return false
    val clamped = volume.coerceIn(0f, 1f)
    if (clamped <= 0f) {
      pause()
      return false
    }

    if (mediaPlayer != null && isPrepared && currentResId == resId) {
      runCatching {
        mediaPlayer?.setVolume(clamped, clamped)
        if (mediaPlayer?.isPlaying != true) {
          mediaPlayer?.start()
        }
        return true
      }
    }

    release()
    return runCatching {
      val attributes =
        AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_GAME)
          .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
          .build()

      val player = MediaPlayer()
      player.setAudioAttributes(attributes)
      player.isLooping = true
      player.setOnErrorListener { mp, _, _ ->
        runCatching { mp.reset() }
        isPrepared = false
        true
      }

      val afd = runCatching { context.resources.openRawResourceFd(resId) }.getOrNull()
      if (afd != null) {
        afd.use { descriptor ->
          player.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
        }
        player.prepare()
        mediaPlayer = player
      } else {
        player.release()
        val fallback = MediaPlayer.create(context, resId) ?: return false
        fallback.setAudioAttributes(attributes)
        fallback.isLooping = true
        mediaPlayer = fallback
      }

      currentResId = resId
      isPrepared = true
      mediaPlayer?.setVolume(clamped, clamped)
      mediaPlayer?.start()
      true
    }.getOrElse {
      release()
      false
    }
  }

  override fun setVolume(volume: Float) {
    val clamped = volume.coerceIn(0f, 1f)
    runCatching {
      mediaPlayer?.setVolume(clamped, clamped)
    }
  }

  override fun pause() {
    runCatching {
      val mp = mediaPlayer ?: return
      if (isPrepared && mp.isPlaying) {
        mp.pause()
      }
    }
  }

  override fun stop() {
    runCatching {
      val mp = mediaPlayer ?: return
      if (isPrepared) {
        if (mp.isPlaying) {
          mp.pause()
        }
        mp.seekTo(0)
      }
    }
  }

  override fun isPlaying(): Boolean {
    return runCatching { mediaPlayer?.isPlaying == true }.getOrDefault(false)
  }

  override fun release() {
    runCatching { mediaPlayer?.setOnErrorListener(null) }
    runCatching {
      if (isPrepared && mediaPlayer?.isPlaying == true) {
        mediaPlayer?.stop()
      }
    }
    runCatching { mediaPlayer?.release() }
    mediaPlayer = null
    isPrepared = false
    currentResId = 0
  }
}

private class AndroidAudioFocusGateway(context: Context) : AudioFocusGateway {
  private val audioManager =
    context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

  private var focusRequest: AudioFocusRequest? = null
  private var legacyListener: AudioManager.OnAudioFocusChangeListener? = null
  private var hasFocus: Boolean = false

  override fun requestFocus(onFocusChange: (Int) -> Unit): Boolean {
    val manager = audioManager ?: return true
    if (hasFocus) return true

    val listener = AudioManager.OnAudioFocusChangeListener { change -> onFocusChange(change) }
    val result =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val request =
          AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
              AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            )
            .setWillPauseWhenDucked(false)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener(listener)
            .build()
        focusRequest = request
        manager.requestAudioFocus(request)
      } else {
        legacyListener = listener
        @Suppress("DEPRECATION")
        manager.requestAudioFocus(
          listener,
          AudioManager.STREAM_MUSIC,
          AudioManager.AUDIOFOCUS_GAIN,
        )
      }
    hasFocus = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
    return hasFocus || result != AudioManager.AUDIOFOCUS_REQUEST_FAILED
  }

  override fun abandonFocus() {
    val manager = audioManager ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      focusRequest?.let { req ->
        runCatching { manager.abandonAudioFocusRequest(req) }
      }
      focusRequest = null
    } else {
      legacyListener?.let { listener ->
        @Suppress("DEPRECATION")
        runCatching { manager.abandonAudioFocus(listener) }
      }
      legacyListener = null
    }
    hasFocus = false
  }
}

class SoundManager internal constructor(
  context: Context,
  private val sfxEngine: SfxEngine,
  private val bgmEngine: BgmEngine,
  private val audioFocusGateway: AudioFocusGateway,
  private val clockMs: () -> Long = { System.currentTimeMillis() },
) : SoundFeedbackController {

  constructor(context: Context) : this(
    context = context.applicationContext,
    sfxEngine = AndroidSfxEngine(),
    bgmEngine = AndroidBgmEngine(),
    audioFocusGateway = AndroidAudioFocusGateway(context.applicationContext),
  )

  private data class PendingPlay(
    val effect: GameSoundEffect,
    val requestedAtMs: Long,
  )

  private val appContext = context.applicationContext
  private val lock = Any()

  private var audioSettings = AudioSettings()
  @Volatile private var vibrationEnabled: Boolean = true
  private var isForeground: Boolean = true
  @Volatile private var isReleased: Boolean = false
  private var musicShouldBeActive: Boolean = true
  private var pausedByFocusLoss: Boolean = false
  private var duckMultiplier: Float = 1.0f

  private val effectToSampleId = EnumMap<GameSoundEffect, Int>(GameSoundEffect::class.java)
  private val sampleIdToEffect = ConcurrentHashMap<Int, GameSoundEffect>()
  private val loadedSampleIds = HashSet<Int>()
  private val pendingPlays = HashMap<Int, PendingPlay>()
  private val activeStreams = EnumMap<GameSoundEffect, Int>(GameSoundEffect::class.java)
  private val lastPlayMs = EnumMap<GameSoundEffect, Long>(GameSoundEffect::class.java)

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

  init {
    sfxEngine.setOnLoadCompleteListener { sampleId, status ->
      handleSampleLoaded(sampleId, status)
    }
    preloadSounds()
  }

  private fun preloadSounds() {
    synchronized(lock) {
      if (isReleased) return
      for (effect in GameSoundEffect.entries) {
        if (!isValidRawResource(effect.resId)) continue
        val sampleId = sfxEngine.load(appContext, effect.resId)
        if (sampleId > 0) {
          effectToSampleId[effect] = sampleId
          sampleIdToEffect[sampleId] = effect
        }
      }
    }
  }

  private fun isValidRawResource(resId: Int): Boolean {
    if (resId == 0) return false
    return runCatching { appContext.resources.getResourceName(resId) }.isSuccess
  }

  private fun handleSampleLoaded(sampleId: Int, status: Int) {
    synchronized(lock) {
      if (isReleased || status != 0) {
        pendingPlays.remove(sampleId)
        return
      }
      loadedSampleIds.add(sampleId)
      val pending = pendingPlays.remove(sampleId) ?: return
      val ageMs = clockMs() - pending.requestedAtMs
      if (ageMs <= 600L) {
        playLoadedEffectLocked(pending.effect, sampleId)
      }
    }
  }

  fun isEffectLoaded(effect: GameSoundEffect): Boolean {
    synchronized(lock) {
      val sampleId = effectToSampleId[effect] ?: return false
      return sampleId in loadedSampleIds
    }
  }

  fun getRegisteredSampleId(effect: GameSoundEffect): Int {
    synchronized(lock) {
      return effectToSampleId[effect] ?: 0
    }
  }

  fun currentEffectiveSfxVolume(): Float {
    synchronized(lock) {
      return effectiveSfxVolumeLocked()
    }
  }

  fun currentEffectiveMusicVolume(): Float {
    synchronized(lock) {
      return effectiveMusicVolumeLocked()
    }
  }

  private fun effectiveSfxVolumeLocked(): Float {
    if (isReleased || !audioSettings.soundEnabled || !audioSettings.sfxEnabled) return 0f
    return (audioSettings.masterVolume.coerceIn(0f, 1f) *
      audioSettings.sfxVolume.coerceIn(0f, 1f) *
      duckMultiplier)
      .coerceIn(0f, 1f)
  }

  private fun effectiveMusicVolumeLocked(): Float {
    if (isReleased || !audioSettings.soundEnabled || !audioSettings.musicEnabled) return 0f
    return (audioSettings.masterVolume.coerceIn(0f, 1f) *
      audioSettings.musicVolume.coerceIn(0f, 1f) *
      duckMultiplier)
      .coerceIn(0f, 1f)
  }

  override fun updateAudioSettings(settings: AudioSettings) {
    synchronized(lock) {
      if (isReleased) return
      audioSettings =
        settings.copy(
          masterVolume = settings.masterVolume.coerceIn(0f, 1f),
          musicVolume = settings.musicVolume.coerceIn(0f, 1f),
          sfxVolume = settings.sfxVolume.coerceIn(0f, 1f),
        )
      syncVolumesAndMusicLocked()
    }
  }

  override fun setSoundEnabled(enabled: Boolean) {
    synchronized(lock) {
      if (isReleased) return
      audioSettings = audioSettings.copy(soundEnabled = enabled)
      syncVolumesAndMusicLocked()
    }
  }

  override fun setMusicEnabled(enabled: Boolean) {
    synchronized(lock) {
      if (isReleased) return
      audioSettings = audioSettings.copy(musicEnabled = enabled)
      if (enabled) {
        musicShouldBeActive = true
        pausedByFocusLoss = false
      }
      syncVolumesAndMusicLocked()
    }
  }

  override fun setSfxEnabled(enabled: Boolean) {
    synchronized(lock) {
      if (isReleased) return
      audioSettings = audioSettings.copy(sfxEnabled = enabled)
      syncVolumesAndMusicLocked()
    }
  }

  override fun setMasterVolume(volume: Float) {
    synchronized(lock) {
      if (isReleased) return
      audioSettings = audioSettings.copy(masterVolume = volume.coerceIn(0f, 1f))
      syncVolumesAndMusicLocked()
    }
  }

  override fun setMusicVolume(volume: Float) {
    synchronized(lock) {
      if (isReleased) return
      audioSettings = audioSettings.copy(musicVolume = volume.coerceIn(0f, 1f))
      syncVolumesAndMusicLocked()
    }
  }

  override fun setSfxVolume(volume: Float) {
    synchronized(lock) {
      if (isReleased) return
      audioSettings = audioSettings.copy(sfxVolume = volume.coerceIn(0f, 1f))
      syncVolumesAndMusicLocked()
    }
  }

  override fun setVibrationEnabled(enabled: Boolean) {
    vibrationEnabled = enabled
  }

  override fun onAppForeground() {
    synchronized(lock) {
      if (isReleased) return
      isForeground = true
      syncVolumesAndMusicLocked()
    }
  }

  override fun onAppBackground() {
    synchronized(lock) {
      if (isReleased) return
      isForeground = false
      bgmEngine.pause()
      audioFocusGateway.abandonFocus()
    }
  }

  override fun startBackgroundMusic() {
    synchronized(lock) {
      if (isReleased) return
      musicShouldBeActive = true
      pausedByFocusLoss = false
      syncVolumesAndMusicLocked()
    }
  }

  override fun stopBackgroundMusic() {
    synchronized(lock) {
      if (isReleased) return
      musicShouldBeActive = false
      bgmEngine.setVolume(0f)
      bgmEngine.stop()
      audioFocusGateway.abandonFocus()
    }
  }

  private fun syncVolumesAndMusicLocked() {
    val sfxVol = effectiveSfxVolumeLocked()
    if (sfxVol <= 0f) {
      pendingPlays.clear()
      activeStreams.values.forEach { streamId ->
        sfxEngine.setVolume(streamId, 0f, 0f)
        sfxEngine.stop(streamId)
      }
      activeStreams.clear()
    } else {
      activeStreams.values.forEach { streamId ->
        sfxEngine.setVolume(streamId, sfxVol, sfxVol)
      }
    }

    val musicVol = effectiveMusicVolumeLocked()
    val shouldPlayMusic =
      !isReleased &&
        isForeground &&
        musicShouldBeActive &&
        !pausedByFocusLoss &&
        musicVol > 0f &&
        isValidRawResource(R.raw.bgm_jungle_arena)

    if (!shouldPlayMusic) {
      bgmEngine.setVolume(0f)
      if (!audioSettings.soundEnabled || !audioSettings.musicEnabled || musicVol <= 0f || !musicShouldBeActive) {
        bgmEngine.stop()
        audioFocusGateway.abandonFocus()
      } else {
        bgmEngine.pause()
      }
      return
    }

    val focusGranted = audioFocusGateway.requestFocus { change -> handleAudioFocusChange(change) }
    if (!focusGranted) {
      bgmEngine.pause()
      return
    }

    if (bgmEngine.isPlaying()) {
      bgmEngine.setVolume(musicVol)
    } else {
      bgmEngine.startOrResume(appContext, R.raw.bgm_jungle_arena, musicVol)
    }
  }

  private fun handleAudioFocusChange(focusChange: Int) {
    synchronized(lock) {
      if (isReleased) return
      when (focusChange) {
        AudioManager.AUDIOFOCUS_LOSS -> {
          pausedByFocusLoss = true
          duckMultiplier = 1.0f
          bgmEngine.pause()
          audioFocusGateway.abandonFocus()
        }
        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
          pausedByFocusLoss = true
          bgmEngine.pause()
        }
        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
          duckMultiplier = 0.25f
          val bgmVol = effectiveMusicVolumeLocked()
          bgmEngine.setVolume(bgmVol)
          val sfxVol = effectiveSfxVolumeLocked()
          activeStreams.values.forEach { sfxEngine.setVolume(it, sfxVol, sfxVol) }
        }
        AudioManager.AUDIOFOCUS_GAIN -> {
          pausedByFocusLoss = false
          duckMultiplier = 1.0f
          syncVolumesAndMusicLocked()
        }
      }
    }
  }

  private fun playEffect(effect: GameSoundEffect) {
    synchronized(lock) {
      if (isReleased) return
      val volume = effectiveSfxVolumeLocked()
      if (volume <= 0f) return

      val now = clockMs()
      val previousPlay = lastPlayMs[effect]
      if (previousPlay != null && now - previousPlay < effect.cooldownMs) {
        return
      }

      val sampleId = effectToSampleId[effect] ?: return
      if (sampleId !in loadedSampleIds) {
        pendingPlays[sampleId] = PendingPlay(effect = effect, requestedAtMs = now)
        return
      }

      playLoadedEffectLocked(effect, sampleId)
    }
  }

  private fun playLoadedEffectLocked(effect: GameSoundEffect, sampleId: Int) {
    val volume = effectiveSfxVolumeLocked()
    if (isReleased || volume <= 0f) return

    val now = clockMs()
    val previousPlay = lastPlayMs[effect]
    if (previousPlay != null && now - previousPlay < effect.cooldownMs) {
      return
    }

    // Stop any previous active stream for the same event to avoid overlapping glitches
    activeStreams.remove(effect)?.let { prevStream ->
      sfxEngine.stop(prevStream)
    }

    // Outcome sounds (Win, Draw, Game Over, Play Again, Game Start) supersede older outcome sounds
    if (effect.isOutcomeSound) {
      GameSoundEffect.entries
        .filter { it.isOutcomeSound && it != effect }
        .forEach { otherOutcome ->
          activeStreams.remove(otherOutcome)?.let { prevOutcomeStream ->
            sfxEngine.stop(prevOutcomeStream)
          }
        }
    }

    lastPlayMs[effect] = now
    val streamId = sfxEngine.play(sampleId, volume, volume, effect.priority)
    if (streamId > 0) {
      activeStreams[effect] = streamId
      sfxEngine.setVolume(streamId, volume, volume)
    }
  }

  override fun playButtonClick() = playEffect(GameSoundEffect.BUTTON_CLICK)

  override fun playGameStart() = playEffect(GameSoundEffect.GAME_START)

  override fun playAnimalSelect() = playEffect(GameSoundEffect.ANIMAL_SELECT)

  override fun playLionMove() = playEffect(GameSoundEffect.LION_MOVE)

  override fun playWolfMove() = playEffect(GameSoundEffect.WOLF_MOVE)

  override fun playAiMove() = playEffect(GameSoundEffect.AI_MOVE)

  override fun playMovePlayer1() = playLionMove()

  override fun playMovePlayer2() = playWolfMove()

  override fun playWin() = playEffect(GameSoundEffect.WIN)

  override fun playDraw() = playEffect(GameSoundEffect.DRAW)

  override fun playGameOver() = playEffect(GameSoundEffect.GAME_OVER)

  override fun playPlayAgain() = playEffect(GameSoundEffect.PLAY_AGAIN)

  override fun playVictoryCelebration() = playEffect(GameSoundEffect.VICTORY_CELEBRATION)

  override fun vibrateMove() {
    triggerVibration(durationMs = 22L, amplitude = 90)
  }

  override fun vibrateWin() {
    triggerVibration(durationMs = 70L, amplitude = 180)
  }

  private fun triggerVibration(durationMs: Long, amplitude: Int) {
    if (!vibrationEnabled || isReleased) return
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

  override fun release() {
    synchronized(lock) {
      if (isReleased) return
      isReleased = true
      pendingPlays.clear()
      activeStreams.values.forEach { streamId -> sfxEngine.stop(streamId) }
      activeStreams.clear()
      loadedSampleIds.clear()
      sampleIdToEffect.clear()
      effectToSampleId.clear()
      sfxEngine.release()
      bgmEngine.stop()
      bgmEngine.release()
      audioFocusGateway.abandonFocus()
      if (sharedInstance === this) {
        sharedInstance = null
      }
    }
  }

  companion object {
    @Volatile private var sharedInstance: SoundManager? = null

    fun getInstance(context: Context): SoundManager {
      val existing = sharedInstance
      if (existing != null && !existing.isReleased) {
        return existing
      }
      return synchronized(this) {
        val rechecked = sharedInstance
        if (rechecked != null && !rechecked.isReleased) {
          rechecked
        } else {
          SoundManager(context.applicationContext).also { created ->
            sharedInstance = created
          }
        }
      }
    }

    internal fun clearSharedInstance() {
      synchronized(this) {
        sharedInstance?.release()
        sharedInstance = null
      }
    }
  }
}
