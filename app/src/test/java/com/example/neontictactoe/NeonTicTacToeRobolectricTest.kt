package com.example.neontictactoe

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.neontictactoe.audio.SoundFeedbackController
import com.example.neontictactoe.data.GamePreferencesRepository
import com.example.neontictactoe.data.StatisticsRepository
import com.example.neontictactoe.domain.AIDifficulty
import com.example.neontictactoe.domain.AchievementId
import com.example.neontictactoe.domain.AnimalType
import com.example.neontictactoe.domain.BoardSize
import com.example.neontictactoe.domain.NeonThemeStyle
import com.example.neontictactoe.domain.PlayMode
import com.example.neontictactoe.domain.PlayerSlot
import com.example.neontictactoe.navigation.AppRoutes
import com.example.neontictactoe.navigation.NeonTicTacToeApp
import com.example.neontictactoe.ui.AppViewModel
import com.example.neontictactoe.ui.game.GameViewModel
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private class FakeSoundFeedback : SoundFeedbackController {
  var lastSoundEnabled: Boolean = true
  var lastMusicEnabled: Boolean = true
  var lastSfxEnabled: Boolean = true
  var lastMasterVolume: Float = 1.0f
  var lastMusicVolume: Float = 0.55f
  var lastSfxVolume: Float = 1.0f
  var lastVibrationEnabled: Boolean = true
  var clickCount: Int = 0
  var gameStartCount: Int = 0
  var animalSelectCount: Int = 0
  var moveP1Count: Int = 0
  var moveP2Count: Int = 0
  var aiMoveCount: Int = 0
  var winCount: Int = 0
  var victoryCelebrationCount: Int = 0
  var drawCount: Int = 0
  var gameOverCount: Int = 0
  var playAgainCount: Int = 0
  var vibrateMoveCount: Int = 0
  var vibrateWinCount: Int = 0

  override fun updateAudioSettings(settings: com.example.neontictactoe.audio.AudioSettings) {
    lastSoundEnabled = settings.soundEnabled
    lastMusicEnabled = settings.musicEnabled
    lastSfxEnabled = settings.sfxEnabled
    lastMasterVolume = settings.masterVolume
    lastMusicVolume = settings.musicVolume
    lastSfxVolume = settings.sfxVolume
  }

  override fun setSoundEnabled(enabled: Boolean) {
    lastSoundEnabled = enabled
  }

  override fun setMusicEnabled(enabled: Boolean) {
    lastMusicEnabled = enabled
  }

  override fun setSfxEnabled(enabled: Boolean) {
    lastSfxEnabled = enabled
  }

  override fun setMasterVolume(volume: Float) {
    lastMasterVolume = volume
  }

  override fun setMusicVolume(volume: Float) {
    lastMusicVolume = volume
  }

  override fun setSfxVolume(volume: Float) {
    lastSfxVolume = volume
  }

  override fun setVibrationEnabled(enabled: Boolean) {
    lastVibrationEnabled = enabled
  }

  override fun onAppForeground() {}

  override fun onAppBackground() {}

  override fun startBackgroundMusic() {}

  override fun stopBackgroundMusic() {}

  override fun playButtonClick() {
    clickCount++
  }

  override fun playGameStart() {
    gameStartCount++
  }

  override fun playAnimalSelect() {
    animalSelectCount++
  }

  override fun playLionMove() {
    moveP1Count++
  }

  override fun playWolfMove() {
    moveP2Count++
  }

  override fun playAiMove() {
    aiMoveCount++
  }

  override fun playMovePlayer1() {
    playLionMove()
  }

  override fun playMovePlayer2() {
    playWolfMove()
  }

  override fun playWin() {
    winCount++
  }

  override fun playDraw() {
    drawCount++
  }

  override fun playGameOver() {
    gameOverCount++
  }

  override fun playPlayAgain() {
    playAgainCount++
  }

  override fun playVictoryCelebration() {
    victoryCelebrationCount++
  }

  override fun vibrateMove() {
    vibrateMoveCount++
  }

  override fun vibrateWin() {
    vibrateWinCount++
  }

  override fun release() {}
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp-xhdpi")
class NeonTicTacToeRobolectricTest {

  @get:Rule(order = 0) val tempFolder = TemporaryFolder()

  @get:Rule(order = 1) val composeTestRule = createComposeRule()

  private fun createRepos(): Pair<GamePreferencesRepository, StatisticsRepository> {
    val prefsFile = File(tempFolder.newFolder(), "test_prefs.preferences_pb")
    val statsFile = File(tempFolder.newFolder(), "test_stats.preferences_pb")
    val prefsDs = PreferenceDataStoreFactory.create(produceFile = { prefsFile })
    val statsDs = PreferenceDataStoreFactory.create(produceFile = { statsFile })
    return GamePreferencesRepository(prefsDs) to StatisticsRepository(statsDs)
  }

  private fun advanceNavigation() {
    composeTestRule.mainClock.advanceTimeBy(350)
    composeTestRule.waitForIdle()
  }

  @Test
  fun `preferences statistics achievements and animal unlocks persist in datastore`() = runBlocking {
    val folder = tempFolder.newFolder()
    val prefsFile = File(folder, "persist_prefs.preferences_pb")
    val statsFile = File(folder, "persist_stats.preferences_pb")

    val prefsDs = PreferenceDataStoreFactory.create(produceFile = { prefsFile })
    val statsDs = PreferenceDataStoreFactory.create(produceFile = { statsFile })

    val prefsRepo = GamePreferencesRepository(prefsDs)
    val statsRepo = StatisticsRepository(statsDs)

    prefsRepo.setSoundEnabled(false)
    prefsRepo.setVibrationEnabled(false)
    prefsRepo.setThemeStyle(NeonThemeStyle.EMERALD_CANOPY)
    prefsRepo.setPlayer1Name("CyanKing")
    prefsRepo.setPlayer2Name("GoldFang")
    prefsRepo.setPlayer1Animal(AnimalType.LION)
    prefsRepo.setPlayer2Animal(AnimalType.WOLF)
    prefsRepo.setDefaultDifficulty(AIDifficulty.EXPERT)
    prefsRepo.setDefaultBoardSize(BoardSize.MEDIUM_5X5)

    // Initial animals: only Lion & Wolf unlocked
    val initialStats = statsRepo.statisticsFlow.first()
    assertEquals(setOf(AnimalType.LION, AnimalType.WOLF), initialStats.unlockedAnimals)

    // Match 1: Win vs Medium AI on 5x5 -> unlocks FIRST_VICTORY, SKY_STRIKER (Eagle), GIANT_CONQUEROR (Panda)
    val unlocks1 =
      statsRepo.recordMatchResult(
        isAiMatch = true,
        winner = PlayerSlot.PLAYER_1,
        boardSize = BoardSize.MEDIUM_5X5,
        aiDifficulty = AIDifficulty.MEDIUM,
      )
    assertTrue(AchievementId.FIRST_VICTORY in unlocks1)
    assertTrue(AchievementId.SKY_STRIKER in unlocks1)
    assertTrue(AchievementId.GIANT_CONQUEROR in unlocks1)

    // Match 2: Win vs Expert AI -> unlocks DRAGON_ASCENDANT (Dragon), APEX_LEGEND
    val unlocks2 =
      statsRepo.recordMatchResult(
        isAiMatch = true,
        winner = PlayerSlot.PLAYER_1,
        boardSize = BoardSize.CLASSIC_3X3,
        aiDifficulty = AIDifficulty.EXPERT,
      )
    assertTrue(AchievementId.DRAGON_ASCENDANT in unlocks2)
    assertTrue(AchievementId.APEX_LEGEND in unlocks2)

    // Match 3: Win 3rd match in a row -> unlocks JUNGLE_CHALLENGER (Tiger), ALPHA_STREAK (Bear)
    val unlocks3 =
      statsRepo.recordMatchResult(
        isAiMatch = true,
        winner = PlayerSlot.PLAYER_1,
        boardSize = BoardSize.CLASSIC_3X3,
        aiDifficulty = AIDifficulty.EASY,
      )
    assertTrue(AchievementId.JUNGLE_CHALLENGER in unlocks3)
    assertTrue(AchievementId.ALPHA_STREAK in unlocks3)

    // Match 4 (AI loss) & Match 5 (draw) -> 5 games completed -> unlocks PACK_VETERAN (Fox)
    statsRepo.recordMatchResult(isAiMatch = true, winner = PlayerSlot.PLAYER_2)
    val unlocks5 = statsRepo.recordMatchResult(isAiMatch = false, winner = null)
    assertTrue(AchievementId.PACK_VETERAN in unlocks5)

    val savedPrefs = prefsRepo.preferencesFlow.first()
    assertEquals(false, savedPrefs.soundEnabled)
    assertEquals(false, savedPrefs.vibrationEnabled)
    assertEquals(NeonThemeStyle.EMERALD_CANOPY, savedPrefs.themeStyle)
    assertEquals("CyanKing", savedPrefs.player1Name)
    assertEquals("GoldFang", savedPrefs.player2Name)
    assertEquals(AnimalType.LION, savedPrefs.player1Animal)
    assertEquals(AnimalType.WOLF, savedPrefs.player2Animal)
    assertEquals(AIDifficulty.EXPERT, savedPrefs.defaultDifficulty)
    assertEquals(BoardSize.MEDIUM_5X5, savedPrefs.defaultBoardSize)

    val savedStats = statsRepo.statisticsFlow.first()
    assertEquals(5, savedStats.totalGamesPlayed)
    assertEquals(3, savedStats.totalWins)
    assertEquals(1, savedStats.totalLosses)
    assertEquals(1, savedStats.totalDraws)
    assertEquals(3, savedStats.bestWinningStreak)
    assertEquals(AnimalType.entries.toSet(), savedStats.unlockedAnimals)
    assertEquals(AchievementId.entries.toSet(), savedStats.unlockedAchievements)

    statsRepo.resetStatistics()
    val cleared = statsRepo.statisticsFlow.first()
    assertEquals(0, cleared.totalGamesPlayed)
    assertEquals(setOf(AnimalType.LION, AnimalType.WOLF), cleared.unlockedAnimals)
    assertTrue(cleared.unlockedAchievements.isEmpty())
  }

  @Test
  fun `splash screen transitions to home on tap`() {
    val (prefsRepo, statsRepo) = createRepos()
    val fakeSound = FakeSoundFeedback()
    val appVm = AppViewModel(prefsRepo, statsRepo, fakeSound)
    val gameFactory = GameViewModel.Factory(statsRepo, fakeSound)

    composeTestRule.setContent {
      NeonTicTacToeApp(
        appViewModel = appVm,
        gameViewModelFactory = gameFactory,
        startDestination = AppRoutes.ROUTE_SPLASH,
      )
    }

    composeTestRule.onNodeWithTag("splash_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("splash_screen").performClick()
    advanceNavigation()
    composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()
  }

  @Test
  fun `home buttons sound toggle animal selection achievements ai and two player navigate properly`() {
    val (prefsRepo, statsRepo) = createRepos()
    val fakeSound = FakeSoundFeedback()
    val appVm = AppViewModel(prefsRepo, statsRepo, fakeSound)
    val gameFactory = GameViewModel.Factory(statsRepo, fakeSound)

    composeTestRule.setContent {
      NeonTicTacToeApp(
        appViewModel = appVm,
        gameViewModelFactory = gameFactory,
        startDestination = AppRoutes.ROUTE_HOME,
      )
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()

    // Sound toggle updates AppViewModel & SoundFeedbackController
    composeTestRule.onNodeWithTag("home_sound_toggle").performClick()
    composeTestRule.waitForIdle()
    assertFalse(fakeSound.lastSoundEnabled)
    composeTestRule.onNodeWithTag("home_sound_toggle").performClick()
    composeTestRule.waitForIdle()
    assertTrue(fakeSound.lastSoundEnabled)

    // Quick Play -> Game -> Back -> Home
    composeTestRule.onNodeWithTag("play_game_button").performScrollTo().performClick()
    advanceNavigation()
    composeTestRule.onNodeWithTag("game_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("game_back_button").performClick()
    advanceNavigation()
    composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()

    // Play vs AI -> Player Setup (YOU VS AI) -> Back
    composeTestRule.onNodeWithTag("play_vs_ai_button").performScrollTo().performClick()
    advanceNavigation()
    composeTestRule.onNodeWithTag("player_setup_screen").assertIsDisplayed()
    composeTestRule.onNodeWithText("YOU VS AI").assertIsDisplayed()
    composeTestRule.onNodeWithTag("back_button").performClick()
    advanceNavigation()
    composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()

    // Two Player -> Player Setup (ANIMAL SETUP) -> Back
    composeTestRule.onNodeWithTag("two_player_button").performScrollTo().performClick()
    advanceNavigation()
    composeTestRule.onNodeWithTag("player_setup_screen").assertIsDisplayed()
    composeTestRule.onNodeWithText("ANIMAL SETUP").assertIsDisplayed()
    composeTestRule.onNodeWithTag("back_button").performClick()
    advanceNavigation()

    // Achievements screen renders summary & achievement cards -> Back
    composeTestRule.onNodeWithTag("achievements_button").performScrollTo().performClick()
    advanceNavigation()
    composeTestRule.onNodeWithTag("achievements_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("achievements_summary_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("achievement_card_first_victory").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("back_button").performClick()
    advanceNavigation()
    composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()
  }

  @Test
  fun `animal selection swaps unlocked starters gates locked animals and unlocks after achievement`() {
    val (prefsRepo, statsRepo) = createRepos()
    val fakeSound = FakeSoundFeedback()
    val appVm = AppViewModel(prefsRepo, statsRepo, fakeSound)
    val gameFactory = GameViewModel.Factory(statsRepo, fakeSound)

    composeTestRule.setContent {
      NeonTicTacToeApp(
        appViewModel = appVm,
        gameViewModelFactory = gameFactory,
        startDestination = AppRoutes.ROUTE_HOME,
      )
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("animal_selection_button").performScrollTo().performClick()
    advanceNavigation()
    composeTestRule.onNodeWithTag("animal_selection_screen").assertIsDisplayed()

    // Clicking Wolf for Player 1 automatically swaps Lion to Player 2
    composeTestRule.onNodeWithTag("animal_card_wolf").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals(AnimalType.WOLF, appVm.matchSetup.value.player1Animal)
    assertEquals(AnimalType.LION, appVm.matchSetup.value.player2Animal)

    // Clicking locked Dragon shows locked banner
    composeTestRule.onNodeWithTag("animal_card_dragon").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("animal_locked_banner").assertIsDisplayed()

    // Simulate beating Hard AI to unlock Dragon via DataStore
    runBlocking {
      statsRepo.recordMatchResult(
        isAiMatch = true,
        winner = PlayerSlot.PLAYER_1,
        boardSize = BoardSize.CLASSIC_3X3,
        aiDifficulty = AIDifficulty.HARD,
      )
    }
    composeTestRule.waitForIdle()

    // Now selecting Dragon for Player 1 succeeds cleanly
    composeTestRule.onNodeWithTag("animal_card_dragon").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals(AnimalType.DRAGON, appVm.matchSetup.value.player1Animal)

    composeTestRule.onNodeWithTag("animal_confirm_button").performClick()
    advanceNavigation()
    composeTestRule.onNodeWithTag("player_setup_screen").assertIsDisplayed()
  }

  @Test
  fun `all 7 game modes board sizes player setup options and game flow work end to end`() {
    val (prefsRepo, statsRepo) = createRepos()
    val fakeSound = FakeSoundFeedback()
    val appVm = AppViewModel(prefsRepo, statsRepo, fakeSound)
    val gameFactory = GameViewModel.Factory(statsRepo, fakeSound)

    composeTestRule.setContent {
      NeonTicTacToeApp(
        appViewModel = appVm,
        gameViewModelFactory = gameFactory,
        startDestination = AppRoutes.ROUTE_HOME,
      )
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("game_modes_button").performScrollTo().performClick()
    advanceNavigation()
    composeTestRule.onNodeWithTag("game_mode_screen").assertIsDisplayed()

    // Verify all 7 PlayModes are clickable
    for (mode in PlayMode.entries) {
      composeTestRule.onNodeWithTag("mode_card_${mode.name.lowercase()}").performScrollTo().performClick()
    }
    // Verify all 4 board sizes are clickable, end on 3x3
    for (size in BoardSize.entries) {
      composeTestRule.onNodeWithTag("board_size_${size.gridSize}x${size.gridSize}").performScrollTo().performClick()
    }
    composeTestRule.onNodeWithTag("board_size_3x3").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("mode_continue_button").performClick()
    advanceNavigation()

    // Player Setup: opponent toggle, name editing, difficulty, format, board size
    composeTestRule.onNodeWithTag("player_setup_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("toggle_vs_ai").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("difficulty_hard").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("toggle_2_players").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("player1_name_input").performScrollTo().performTextReplacement("Nova")
    composeTestRule.onNodeWithTag("player2_name_input").performScrollTo().performTextReplacement("Vega")
    composeTestRule.onNodeWithTag("format_best_of_3").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("setup_board_3x3").performScrollTo().performClick()

    composeTestRule.onNodeWithTag("start_match_button").performClick()
    advanceNavigation()

    // Play a full Player 1 winning round on 3x3 (0, 3, 1, 4, 2)
    composeTestRule.onNodeWithTag("game_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cell_0").performClick()
    composeTestRule.onNodeWithTag("cell_3").performClick()
    composeTestRule.onNodeWithTag("cell_1").performClick()
    composeTestRule.onNodeWithTag("cell_4").performClick()
    composeTestRule.onNodeWithTag("cell_2").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("NOVA WINS!").assertIsDisplayed()
    composeTestRule.onNodeWithTag("next_round_button").assertIsDisplayed()

    // Verify in-game sound toggle & settings route and back
    composeTestRule.onNodeWithTag("game_sound_toggle").performClick()
    assertFalse(fakeSound.lastSoundEnabled)
    composeTestRule.onNodeWithTag("game_settings_button").performClick()
    advanceNavigation()
    composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("back_button").performClick()
    advanceNavigation()
    composeTestRule.onNodeWithTag("game_screen").assertIsDisplayed()

    // Next Round -> Home
    composeTestRule.onNodeWithTag("next_round_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("game_home_button").performClick()
    advanceNavigation()
    composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()

    // Statistics shows 1 game played & Reset clears it
    composeTestRule.onNodeWithTag("statistics_button").performScrollTo().performClick()
    advanceNavigation()
    composeTestRule.onNodeWithTag("statistics_screen").assertIsDisplayed()
    composeTestRule.onNodeWithText("1 wins across 1 matches").assertIsDisplayed()

    composeTestRule.onNodeWithTag("reset_stats_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("stats_confirm_reset_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("stats_win_rate_card").performScrollTo()
    composeTestRule.onNodeWithText("Play a match to start tracking stats!").assertIsDisplayed()
  }

  @Test
  fun `settings screen updates audio vibration themes names defaults and dialogs`() {
    val (prefsRepo, statsRepo) = createRepos()
    val fakeSound = FakeSoundFeedback()
    val appVm = AppViewModel(prefsRepo, statsRepo, fakeSound)
    val gameFactory = GameViewModel.Factory(statsRepo, fakeSound)

    composeTestRule.setContent {
      NeonTicTacToeApp(
        appViewModel = appVm,
        gameViewModelFactory = gameFactory,
        startDestination = AppRoutes.ROUTE_SETTINGS,
      )
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()

    composeTestRule.onNodeWithTag("settings_sound_switch").performScrollTo().performClick()
    assertFalse(fakeSound.lastSoundEnabled)
    composeTestRule.onNodeWithTag("settings_music_switch").performScrollTo().performClick()
    assertFalse(fakeSound.lastMusicEnabled)
    composeTestRule.onNodeWithTag("settings_sfx_switch").performScrollTo().performClick()
    assertFalse(fakeSound.lastSfxEnabled)
    composeTestRule.onNodeWithTag("settings_vibration_switch").performScrollTo().performClick()
    assertFalse(fakeSound.lastVibrationEnabled)

    composeTestRule.onNodeWithTag("theme_option_emerald_canopy").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("settings_p1_name").performScrollTo().performTextReplacement("Apex")
    composeTestRule.onNodeWithTag("settings_p2_name").performScrollTo().performTextReplacement("Pulse")
    composeTestRule.onNodeWithTag("settings_diff_expert").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("settings_board_7x7").performScrollTo().performClick()

    composeTestRule.onNodeWithTag("about_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("about_close_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("privacy_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("privacy_ok_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("settings_reset_stats_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("settings_confirm_reset_button").performClick()
    composeTestRule.waitForIdle()

    runBlocking {
      val prefs =
        withTimeout(3000) {
          prefsRepo.preferencesFlow.first {
            !it.soundEnabled &&
              !it.musicEnabled &&
              !it.sfxEnabled &&
              !it.vibrationEnabled &&
              it.themeStyle == NeonThemeStyle.EMERALD_CANOPY &&
              it.player1Name == "Apex" &&
              it.player2Name == "Pulse" &&
              it.defaultDifficulty == AIDifficulty.EXPERT &&
              it.defaultBoardSize == BoardSize.LARGE_7X7
          }
        }
      assertFalse(prefs.soundEnabled)
      assertFalse(prefs.musicEnabled)
      assertFalse(prefs.sfxEnabled)
      assertFalse(prefs.vibrationEnabled)
      assertEquals(NeonThemeStyle.EMERALD_CANOPY, prefs.themeStyle)
      assertEquals("Apex", prefs.player1Name)
      assertEquals("Pulse", prefs.player2Name)
      assertEquals(AIDifficulty.EXPERT, prefs.defaultDifficulty)
      assertEquals(BoardSize.LARGE_7X7, prefs.defaultBoardSize)
    }
  }

  @Test
  @Config(sdk = [36], qualifiers = "w320dp-h640dp-mdpi")
  fun `compact 320dp screen renders home animals achievements modes setup 9x9 game stats and settings`() {
    val (prefsRepo, statsRepo) = createRepos()
    val fakeSound = FakeSoundFeedback()
    val appVm = AppViewModel(prefsRepo, statsRepo, fakeSound)
    val gameFactory = GameViewModel.Factory(statsRepo, fakeSound)

    composeTestRule.setContent {
      NeonTicTacToeApp(
        appViewModel = appVm,
        gameViewModelFactory = gameFactory,
        startDestination = AppRoutes.ROUTE_HOME,
      )
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()

    // Animal Selection on 320dp
    composeTestRule.onNodeWithTag("animal_selection_button").performScrollTo().performClick()
    advanceNavigation()
    composeTestRule.onNodeWithTag("animal_selection_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("back_button").performClick()
    advanceNavigation()

    // Achievements on 320dp
    composeTestRule.onNodeWithTag("achievements_button").performScrollTo().performClick()
    advanceNavigation()
    composeTestRule.onNodeWithTag("achievements_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("back_button").performClick()
    advanceNavigation()

    // Navigate to Game Modes -> select 9x9 -> Setup -> Start 9x9 Game
    composeTestRule.onNodeWithTag("game_modes_button").performScrollTo().performClick()
    advanceNavigation()
    composeTestRule.onNodeWithTag("board_size_9x9").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("mode_continue_button").performClick()
    advanceNavigation()
    composeTestRule.onNodeWithTag("start_match_button").performClick()
    advanceNavigation()

    composeTestRule.onNodeWithTag("game_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("game_board").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cell_0").performClick()
    composeTestRule.onNodeWithTag("game_home_button").performClick()
    advanceNavigation()

    // Statistics & Settings on 320dp
    composeTestRule.onNodeWithTag("statistics_button").performScrollTo().performClick()
    advanceNavigation()
    composeTestRule.onNodeWithTag("statistics_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("back_button").performClick()
    advanceNavigation()

    composeTestRule.onNodeWithTag("settings_button").performScrollTo().performClick()
    advanceNavigation()
    composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
  }

  @Test
  fun `test2 test3 and test5 two player draw then play again then win then play again works in ui`() {
    val (prefsRepo, statsRepo) = createRepos()
    val fakeSound = FakeSoundFeedback()
    val appVm = AppViewModel(prefsRepo, statsRepo, fakeSound)
    val gameFactory = GameViewModel.Factory(statsRepo, fakeSound)

    composeTestRule.setContent {
      NeonTicTacToeApp(
        appViewModel = appVm,
        gameViewModelFactory = gameFactory,
        startDestination = AppRoutes.ROUTE_HOME,
      )
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("two_player_button").performScrollTo().performClick()
    advanceNavigation()
    composeTestRule.onNodeWithTag("format_single_round").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("start_match_button").performClick()
    advanceNavigation()

    // Round 1 -> Draw (0, 1, 2, 4, 3, 5, 7, 6, 8)
    val drawMoves = listOf(0, 1, 2, 4, 3, 5, 7, 6, 8)
    drawMoves.forEach { idx -> composeTestRule.onNodeWithTag("cell_$idx").performClick() }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("DRAW BATTLE!").assertIsDisplayed()
    composeTestRule.onNodeWithText("PLAY AGAIN").assertIsDisplayed()

    // Click PLAY AGAIN -> board cleared, draw banner removed, Player 1 turn active
    composeTestRule.onNodeWithTag("restart_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("DRAW BATTLE!").assertDoesNotExist()
    composeTestRule.onNodeWithText("RESTART").assertIsDisplayed()

    // Round 2 (TEST 5) -> Another consecutive Draw -> PLAY AGAIN again
    drawMoves.forEach { idx -> composeTestRule.onNodeWithTag("cell_$idx").performClick() }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("DRAW BATTLE!").assertIsDisplayed()
    composeTestRule.onNodeWithTag("restart_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("DRAW BATTLE!").assertDoesNotExist()

    // Round 3 (TEST 3 UI) -> Player 1 wins -> PLAY AGAIN clears winning banner & starts fresh match
    listOf(0, 3, 1, 4, 2).forEach { idx -> composeTestRule.onNodeWithTag("cell_$idx").performClick() }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("PLAYER 1 WINS!").assertIsDisplayed()
    composeTestRule.onNodeWithText("PLAY AGAIN").assertIsDisplayed()

    composeTestRule.onNodeWithTag("restart_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("PLAYER 1 WINS!").assertDoesNotExist()
    composeTestRule.onNodeWithText("RESTART").assertIsDisplayed()

    // Fresh match accepts new moves immediately
    composeTestRule.onNodeWithTag("cell_4").performClick()
    composeTestRule.waitForIdle()

    runBlocking {
      val stats = withTimeout(3000) { statsRepo.statisticsFlow.first { it.totalGamesPlayed == 3 } }
      assertEquals(2, stats.totalDraws)
      assertEquals(1, stats.totalWins)
    }
  }
}
