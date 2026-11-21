package com.example.neontictactoe

import android.content.Context
import android.media.AudioManager
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import com.example.R
import com.example.neontictactoe.audio.AudioFocusGateway
import com.example.neontictactoe.audio.AudioSettings
import com.example.neontictactoe.audio.BgmEngine
import com.example.neontictactoe.audio.GameSoundEffect
import com.example.neontictactoe.audio.SfxEngine
import com.example.neontictactoe.audio.SoundManager
import com.example.neontictactoe.data.GamePreferencesRepository
import com.example.neontictactoe.data.StatisticsRepository
import com.example.neontictactoe.ui.AppViewModel
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private class SpySfxEngine : SfxEngine {
  data class PlayCall(val sampleId: Int, val leftVol: Float, val rightVol: Float, val priority: Int)
  data class VolumeCall(val streamId: Int, val leftVol: Float, val rightVol: Float)

  private var loadListener: ((Int, Int) -> Unit)? = null
  private var nextSampleId = 1
  private var nextStreamId = 100

  val loadedResIds = mutableMapOf<Int, Int>()
  val playCalls = mutableListOf<PlayCall>()
  val volumeCalls = mutableListOf<VolumeCall>()
  val stoppedStreams = mutableListOf<Int>()
  var released = false

  override fun setOnLoadCompleteListener(listener: (sampleId: Int, status: Int) -> Unit) {
    loadListener = listener
  }

  override fun load(context: Context, resId: Int): Int {
    val id = nextSampleId++
    loadedResIds[resId] = id
    return id
  }

  fun completeAllLoads(status: Int = 0) {
    loadedResIds.values.forEach { sampleId -> loadListener?.invoke(sampleId, status) }
  }

  fun completeLoad(sampleId: Int, status: Int = 0) {
    loadListener?.invoke(sampleId, status)
  }

  override fun play(sampleId: Int, leftVolume: Float, rightVolume: Float, priority: Int): Int {
    playCalls.add(PlayCall(sampleId, leftVolume, rightVolume, priority))
    return nextStreamId++
  }

  override fun setVolume(streamId: Int, leftVolume: Float, rightVolume: Float) {
    volumeCalls.add(VolumeCall(streamId, leftVolume, rightVolume))
  }

  override fun stop(streamId: Int) {
    stoppedStreams.add(streamId)
  }

  override fun release() {
    released = true
  }
}

private class SpyBgmEngine : BgmEngine {
  var playing = false
  var lastVolume = -1f
  val volumeHistory = mutableListOf<Float>()
  var startCount = 0
  var pauseCount = 0
  var stopCount = 0
  var released = false

  override fun startOrResume(context: Context, resId: Int, volume: Float): Boolean {
    if (resId == 0 || volume <= 0f) return false
    startCount++
    playing = true
    lastVolume = volume
    volumeHistory.add(volume)
    return true
  }

  override fun setVolume(volume: Float) {
    lastVolume = volume
    volumeHistory.add(volume)
  }

  override fun pause() {
    pauseCount++
    playing = false
  }

  override fun stop() {
    stopCount++
    playing = false
  }

  override fun isPlaying(): Boolean = playing

  override fun release() {
    released = true
    playing = false
  }
}

private class SpyAudioFocusGateway(var grantFocus: Boolean = true) : AudioFocusGateway {
  var requestCount = 0
  var abandonCount = 0
  var activeListener: ((Int) -> Unit)? = null

  override fun requestFocus(onFocusChange: (Int) -> Unit): Boolean {
    requestCount++
    activeListener = onFocusChange
    return grantFocus
  }

  override fun abandonFocus() {
    abandonCount++
    activeListener = null
  }

  fun dispatchFocusChange(change: Int) {
    activeListener?.invoke(change)
  }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SoundManagerRobolectricTest {

  @get:Rule val tempFolder = TemporaryFolder()

  @Test
  fun `real SoundManager loads all valid raw resources cleanly and releases without crash`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    GameSoundEffect.entries.forEach { effect ->
      assertNotEquals(0, effect.resId)
      val afd = context.resources.openRawResourceFd(effect.resId)
      assertTrue(afd.length > 0L)
      afd.close()
    }
    val bgmAfd = context.resources.openRawResourceFd(R.raw.bgm_jungle_arena)
    assertTrue(bgmAfd.length > 0L)
    bgmAfd.close()

    val manager = SoundManager(context)
    GameSoundEffect.entries.forEach { effect ->
      assertTrue(manager.getRegisteredSampleId(effect) > 0)
    }
    manager.setMasterVolume(0.5f)
    manager.setMusicVolume(0.5f)
    manager.setSfxVolume(0.5f)
    manager.setSoundEnabled(false)
    manager.onAppBackground()
    manager.release()
  }

  @Test
  fun `test1 sound on waits for load callback then plays all 11 effects and bgm`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sfx = SpySfxEngine()
    val bgm = SpyBgmEngine()
    val focus = SpyAudioFocusGateway()
    var nowMs = 1000L

    val manager = SoundManager(context, sfx, bgm, focus) { nowMs }

    // Before SoundPool completes loading, play() must NOT be called directly
    manager.playGameStart()
    assertEquals(0, sfx.playCalls.size)

    // Completing load triggers the pending Game Start sound once
    val gameStartSample = manager.getRegisteredSampleId(GameSoundEffect.GAME_START)
    sfx.completeLoad(gameStartSample, status = 0)
    assertEquals(1, sfx.playCalls.size)
    assertEquals(gameStartSample, sfx.playCalls[0].sampleId)

    // Complete remaining loads and verify every game sound event works
    sfx.completeAllLoads(status = 0)
    GameSoundEffect.entries.forEach { assertTrue(manager.isEffectLoaded(it)) }

    nowMs += 1000L
    manager.playButtonClick()
    nowMs += 1000L
    manager.playAnimalSelect()
    nowMs += 1000L
    manager.playLionMove()
    nowMs += 1000L
    manager.playWolfMove()
    nowMs += 1000L
    manager.playAiMove()
    nowMs += 1000L
    manager.playWin()
    nowMs += 1000L
    manager.playDraw()
    nowMs += 1000L
    manager.playGameOver()
    nowMs += 1000L
    manager.playPlayAgain()
    nowMs += 1000L
    manager.playVictoryCelebration()

    // 1 initial queued GAME_START + 10 remaining events = 11 total SFX plays
    assertEquals(11, sfx.playCalls.size)

    // Background music starts with positive volume and loops
    manager.startBackgroundMusic()
    assertTrue(bgm.isPlaying())
    assertEquals(1, focus.requestCount)
    assertEquals(0.55f, bgm.lastVolume, 0.001f)
  }

  @Test
  fun `test2 sound off disables all effects and immediately stops background music`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sfx = SpySfxEngine()
    val bgm = SpyBgmEngine()
    val focus = SpyAudioFocusGateway()
    var nowMs = 1000L

    val manager = SoundManager(context, sfx, bgm, focus) { nowMs }
    sfx.completeAllLoads()
    manager.startBackgroundMusic()
    assertTrue(bgm.isPlaying())

    // Trigger an active SFX stream then disable Master Sound
    manager.playWin()
    assertEquals(1, sfx.playCalls.size)

    manager.setSoundEnabled(false)
    assertFalse(bgm.isPlaying())
    assertTrue(bgm.stopCount >= 1)
    assertTrue(sfx.stoppedStreams.contains(100))

    // No effects can play while sound is OFF
    nowMs += 1000L
    manager.playButtonClick()
    manager.playLionMove()
    manager.playDraw()
    manager.playPlayAgain()
    assertEquals(1, sfx.playCalls.size)

    // Independent music vs SFX toggles
    manager.setSoundEnabled(true)
    assertTrue(bgm.isPlaying())

    manager.setMusicEnabled(false)
    assertFalse(bgm.isPlaying())
    nowMs += 1000L
    manager.playButtonClick()
    assertEquals(2, sfx.playCalls.size)

    manager.setMusicEnabled(true)
    assertTrue(bgm.isPlaying())
    manager.setSfxEnabled(false)
    nowMs += 1000L
    manager.playButtonClick()
    assertEquals(2, sfx.playCalls.size)
    assertTrue(bgm.isPlaying())
  }

  @Test
  fun `test3 and test4 volume 50 percent scales audio and volume 0 percent mutes completely`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sfx = SpySfxEngine()
    val bgm = SpyBgmEngine()
    val focus = SpyAudioFocusGateway()
    var nowMs = 1000L

    val manager = SoundManager(context, sfx, bgm, focus) { nowMs }
    sfx.completeAllLoads()
    manager.updateAudioSettings(
      AudioSettings(
        soundEnabled = true,
        musicEnabled = true,
        sfxEnabled = true,
        masterVolume = 1.0f,
        musicVolume = 1.0f,
        sfxVolume = 1.0f,
      )
    )
    manager.startBackgroundMusic()
    assertEquals(1.0f, bgm.lastVolume, 0.001f)

    // Volume 50%
    manager.setSfxVolume(0.5f)
    manager.setMusicVolume(0.5f)
    assertEquals(0.5f, bgm.lastVolume, 0.001f)

    manager.playLionMove()
    assertEquals(1, sfx.playCalls.size)
    assertEquals(0.5f, sfx.playCalls.last().leftVol, 0.001f)
    assertEquals(0.5f, sfx.playCalls.last().rightVol, 0.001f)

    // Master volume 50% + SFX volume 50% -> 25% effective on active streams via SoundPool.setVolume
    manager.setMasterVolume(0.5f)
    assertEquals(0.25f, bgm.lastVolume, 0.001f)
    assertEquals(0.25f, sfx.volumeCalls.last().leftVol, 0.001f)

    // Volume 0% -> completely mutes and stops corresponding audio
    manager.setSfxVolume(0f)
    assertEquals(0f, manager.currentEffectiveSfxVolume(), 0.0001f)
    assertEquals(0f, sfx.volumeCalls.last().leftVol, 0.0001f)
    nowMs += 1000L
    manager.playButtonClick()
    assertEquals(1, sfx.playCalls.size)
    assertTrue(bgm.isPlaying()) // Music still at 0.25f

    manager.setMusicVolume(0f)
    assertEquals(0f, manager.currentEffectiveMusicVolume(), 0.0001f)
    assertEquals(0f, bgm.lastVolume, 0.0001f)
    assertFalse(bgm.isPlaying())

    // Restore music & SFX, then mute via master volume 0%
    manager.setSfxVolume(1.0f)
    manager.setMusicVolume(1.0f)
    assertTrue(bgm.isPlaying())
    manager.setMasterVolume(0f)
    assertFalse(bgm.isPlaying())
    assertEquals(0f, manager.currentEffectiveSfxVolume(), 0.0001f)
    assertEquals(0f, manager.currentEffectiveMusicVolume(), 0.0001f)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `test5 sound preferences persist across datastore restart and hydrate SoundManager`() =
    runBlocking {
      val testDispatcher = UnconfinedTestDispatcher()
      Dispatchers.setMain(testDispatcher)
      try {
        val folder = tempFolder.newFolder()
        val prefsFile = File(folder, "sound_persist_prefs.preferences_pb")
        val statsFile = File(folder, "sound_persist_stats.preferences_pb")

        val dsScope1 = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val prefsDs1 = PreferenceDataStoreFactory.create(scope = dsScope1, produceFile = { prefsFile })
        val statsDs1 = PreferenceDataStoreFactory.create(scope = dsScope1, produceFile = { statsFile })
        val prefsRepo1 = GamePreferencesRepository(prefsDs1)
        val statsRepo1 = StatisticsRepository(statsDs1)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val vm1 =
          AppViewModel(
            prefsRepo1,
            statsRepo1,
            SoundManager(context, SpySfxEngine(), SpyBgmEngine(), SpyAudioFocusGateway()),
          )
        vm1.setMasterVolume(0.5f)
        vm1.setMusicVolume(0.3f)
        vm1.setSfxVolume(0.8f)
        vm1.setMusicEnabled(false)
        vm1.setSfxEnabled(true)

        withTimeout(3000) {
          prefsRepo1.preferencesFlow.first {
            !it.musicEnabled &&
              it.sfxEnabled &&
              it.masterVolume == 0.5f &&
              it.musicVolume == 0.3f &&
              it.sfxVolume == 0.8f
          }
        }
        dsScope1.coroutineContext.job.cancelAndJoin()

        // Simulate app restart with new DataStore instances pointing to same file + fresh SoundManager
        val dsScope2 = CoroutineScope(Dispatchers.IO + SupervisorJob())
        try {
          val prefsDs2 = PreferenceDataStoreFactory.create(scope = dsScope2, produceFile = { prefsFile })
          val statsDs2 = PreferenceDataStoreFactory.create(scope = dsScope2, produceFile = { statsFile })
          val prefsRepo2 = GamePreferencesRepository(prefsDs2)
          val statsRepo2 = StatisticsRepository(statsDs2)
          val sfx2 = SpySfxEngine().also { it.completeAllLoads() }
          val bgm2 = SpyBgmEngine()
          val restoredManager = SoundManager(context, sfx2, bgm2, SpyAudioFocusGateway())

          val vm2 = AppViewModel(prefsRepo2, statsRepo2, restoredManager)
          val restoredPrefs = withTimeout(3000) { vm2.preferences.first { !it.musicEnabled } }

          assertTrue(restoredPrefs.soundEnabled)
          assertFalse(restoredPrefs.musicEnabled)
          assertTrue(restoredPrefs.sfxEnabled)
          assertEquals(0.5f, restoredPrefs.masterVolume, 0.001f)
          assertEquals(0.3f, restoredPrefs.musicVolume, 0.001f)
          assertEquals(0.8f, restoredPrefs.sfxVolume, 0.001f)
          assertEquals(0.4f, restoredManager.currentEffectiveSfxVolume(), 0.001f)
          assertEquals(0f, restoredManager.currentEffectiveMusicVolume(), 0.001f)
          assertFalse(bgm2.isPlaying())
        } finally {
          dsScope2.coroutineContext.job.cancelAndJoin()
        }
      } finally {
        Dispatchers.resetMain()
      }
    }

  @Test
  fun `test9 rapid button taps debounce cleanly audio focus ducks and release stops leaks`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sfx = SpySfxEngine()
    val bgm = SpyBgmEngine()
    val focus = SpyAudioFocusGateway()
    var nowMs = 5000L

    val manager = SoundManager(context, sfx, bgm, focus) { nowMs }
    sfx.completeAllLoads()
    manager.setMusicVolume(0.8f)
    manager.startBackgroundMusic()

    // 10 rapid taps within 10ms -> only 1 sound plays (no glitches or overlapping bursts)
    repeat(10) {
      manager.playButtonClick()
      nowMs += 5L
    }
    assertEquals(1, sfx.playCalls.size)

    // After cooldown expires, second tap stops first click stream before starting new one
    nowMs += 100L
    manager.playButtonClick()
    assertEquals(2, sfx.playCalls.size)
    assertTrue(sfx.stoppedStreams.contains(100))

    // Audio focus duck -> reduces BGM volume cleanly, gain -> restores
    focus.dispatchFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
    assertEquals(0.2f, bgm.lastVolume, 0.001f)
    focus.dispatchFocusChange(AudioManager.AUDIOFOCUS_GAIN)
    assertEquals(0.8f, bgm.lastVolume, 0.001f)

    // Transient focus loss pauses BGM, regain resumes BGM
    focus.dispatchFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
    assertFalse(bgm.isPlaying())
    focus.dispatchFocusChange(AudioManager.AUDIOFOCUS_GAIN)
    assertTrue(bgm.isPlaying())

    // App background pauses BGM, foreground resumes BGM
    manager.onAppBackground()
    assertFalse(bgm.isPlaying())
    manager.onAppForeground()
    assertTrue(bgm.isPlaying())

    // Release cleans up SoundPool, MediaPlayer, focus, and ignores later plays
    manager.release()
    assertTrue(sfx.released)
    assertTrue(bgm.released)
    assertFalse(bgm.isPlaying())
    assertTrue(focus.abandonCount >= 1)

    nowMs += 1000L
    manager.playButtonClick()
    assertEquals(2, sfx.playCalls.size)
  }
}
