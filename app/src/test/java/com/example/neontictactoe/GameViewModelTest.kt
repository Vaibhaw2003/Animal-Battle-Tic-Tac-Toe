package com.example.neontictactoe

import com.example.neontictactoe.audio.SoundFeedbackController
import com.example.neontictactoe.data.GameStatistics
import com.example.neontictactoe.data.StatisticsDataSource
import com.example.neontictactoe.domain.AIDifficulty
import com.example.neontictactoe.domain.AIEngine
import com.example.neontictactoe.domain.AchievementId
import com.example.neontictactoe.domain.AnimalType
import com.example.neontictactoe.domain.BoardSize
import com.example.neontictactoe.domain.MatchFormat
import com.example.neontictactoe.domain.PlayMode
import com.example.neontictactoe.domain.PlayerSlot
import com.example.neontictactoe.ui.MatchSetupConfig
import com.example.neontictactoe.ui.game.GameStatus
import com.example.neontictactoe.ui.game.GameViewModel
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private class InMemoryStatisticsDataSource : StatisticsDataSource {
  private val state = MutableStateFlow(GameStatistics())
  override val statisticsFlow: Flow<GameStatistics> = state.asStateFlow()

  override suspend fun recordMatchResult(
    isAiMatch: Boolean,
    winner: PlayerSlot?,
    boardSize: BoardSize,
    aiDifficulty: AIDifficulty,
  ): List<AchievementId> {
    val cur = state.value
    val games = cur.totalGamesPlayed + 1
    var wins = cur.totalWins
    var losses = cur.totalLosses
    var draws = cur.totalDraws
    var p1Wins = cur.player1Wins
    var p2Wins = cur.player2Wins
    var aiWins = cur.aiWins
    var streak = cur.currentWinningStreak
    var bestStreak = cur.bestWinningStreak
    var mediumPlusWins = cur.mediumPlusAiWins
    var hardPlusWins = cur.hardOrExpertAiWins
    var expertWins = cur.expertAiWins
    var largeWins = cur.largeBoardWins

    when (winner) {
      PlayerSlot.PLAYER_1 -> {
        wins += 1
        p1Wins += 1
        streak += 1
        bestStreak = max(bestStreak, streak)
        if (boardSize != BoardSize.CLASSIC_3X3) largeWins += 1
        if (isAiMatch) {
          if (aiDifficulty != AIDifficulty.EASY) mediumPlusWins += 1
          if (aiDifficulty == AIDifficulty.HARD || aiDifficulty == AIDifficulty.EXPERT) {
            hardPlusWins += 1
          }
          if (aiDifficulty == AIDifficulty.EXPERT) expertWins += 1
        }
      }
      PlayerSlot.PLAYER_2 -> {
        if (isAiMatch) {
          losses += 1
          aiWins += 1
          streak = 0
        } else {
          wins += 1
          p2Wins += 1
          streak = 0
          if (boardSize != BoardSize.CLASSIC_3X3) largeWins += 1
        }
      }
      null -> {
        draws += 1
        streak = 0
      }
    }

    val unlocked = cur.unlockedAchievements.toMutableSet()
    val unlockedAnimals = cur.unlockedAnimals.toMutableSet()
    val newlyUnlocked = mutableListOf<AchievementId>()

    fun check(id: AchievementId, condition: Boolean) {
      if (condition && unlocked.add(id)) {
        newlyUnlocked.add(id)
        id.rewardAnimal?.let { unlockedAnimals.add(it) }
      }
    }

    check(AchievementId.FIRST_VICTORY, wins >= 1)
    check(AchievementId.JUNGLE_CHALLENGER, wins >= 3)
    check(AchievementId.SKY_STRIKER, mediumPlusWins >= 1)
    check(AchievementId.PACK_VETERAN, games >= 5)
    check(AchievementId.GIANT_CONQUEROR, largeWins >= 1)
    check(AchievementId.ALPHA_STREAK, bestStreak >= 3)
    check(AchievementId.DRAGON_ASCENDANT, hardPlusWins >= 1)
    check(AchievementId.APEX_LEGEND, wins >= 10 || expertWins >= 1)

    state.value =
      cur.copy(
        totalGamesPlayed = games,
        totalWins = wins,
        totalLosses = losses,
        totalDraws = draws,
        player1Wins = p1Wins,
        player2Wins = p2Wins,
        aiWins = aiWins,
        currentWinningStreak = streak,
        bestWinningStreak = bestStreak,
        mediumPlusAiWins = mediumPlusWins,
        hardOrExpertAiWins = hardPlusWins,
        expertAiWins = expertWins,
        largeBoardWins = largeWins,
        unlockedAchievements = unlocked,
        unlockedAnimals = unlockedAnimals,
      )
    return newlyUnlocked
  }

  override suspend fun resetStatistics() {
    state.value = GameStatistics()
  }
}

private class RecordingSoundFeedback : SoundFeedbackController {
  var moveP1Count: Int = 0
  var moveP2Count: Int = 0
  var winCount: Int = 0
  var drawCount: Int = 0
  var gameOverCount: Int = 0
  var vibrateWinCount: Int = 0

  override fun setSoundEnabled(enabled: Boolean) {}

  override fun setVibrationEnabled(enabled: Boolean) {}

  override fun playButtonClick() {}

  override fun playMovePlayer1() {
    moveP1Count++
  }

  override fun playMovePlayer2() {
    moveP2Count++
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

  override fun vibrateMove() {}

  override fun vibrateWin() {
    vibrateWinCount++
  }

  override fun release() {}
}

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

  @Test
  fun `game viewmodel updates score unlocks achievements restarts safely and handles best of 3`() =
    runBlocking {
      val testDispatcher = UnconfinedTestDispatcher()
      Dispatchers.setMain(testDispatcher)
      try {
        val statsRepo = InMemoryStatisticsDataSource()
        val fakeSound = RecordingSoundFeedback()
        val vm = GameViewModel(statsRepo, fakeSound, testDispatcher, aiThinkDelayMs = 0L)

        vm.startNewMatch(
          MatchSetupConfig(
            playMode = PlayMode.ANIMAL_BATTLE_MULTIPLAYER,
            boardSize = BoardSize.CLASSIC_3X3,
            matchFormat = MatchFormat.BEST_OF_3,
            isAiOpponent = false,
            player1Name = "Alpha",
            player2Name = "Omega",
            player1Animal = AnimalType.LION,
            player2Animal = AnimalType.WOLF,
          )
        )

        // Player 1 wins round 1: 0, 3, 1, 4, 2
        listOf(0, 3, 1, 4, 2).forEach { vm.onCellClicked(it) }

        assertEquals(1, vm.uiState.value.player1Score)
        assertEquals(0, vm.uiState.value.player2Score)
        assertEquals(1, fakeSound.winCount)
        assertEquals(1, fakeSound.vibrateWinCount)
        assertEquals(3, fakeSound.moveP1Count)
        assertEquals(2, fakeSound.moveP2Count)
        assertTrue(AchievementId.FIRST_VICTORY in vm.uiState.value.recentUnlocks)

        val statsAfterRound = withTimeout(1000) {
          statsRepo.statisticsFlow.first { it.totalGamesPlayed == 1 }
        }
        assertEquals(1, statsAfterRound.player1Wins)

        // Advance to round 2 and restart
        vm.startNextRound()
        assertEquals(2, vm.uiState.value.currentRound)
        vm.restartMatch()
        assertEquals(0, vm.uiState.value.board.moveCount)
        assertEquals(1, statsRepo.statisticsFlow.first().totalGamesPlayed)
      } finally {
        Dispatchers.resetMain()
      }
    }

  @Test
  fun `game viewmodel automatically executes valid ai move across all difficulties`() = runBlocking {
    val testDispatcher = UnconfinedTestDispatcher()
    Dispatchers.setMain(testDispatcher)
    try {
      val statsRepo = InMemoryStatisticsDataSource()
      for (difficulty in AIDifficulty.entries) {
        val vm = GameViewModel(statsRepo, RecordingSoundFeedback(), testDispatcher, aiThinkDelayMs = 0L)
        vm.startNewMatch(
          MatchSetupConfig(
            playMode = PlayMode.LION_VS_AI,
            boardSize = BoardSize.CLASSIC_3X3,
            difficulty = difficulty,
            matchFormat = MatchFormat.SINGLE_ROUND,
            isAiOpponent = true,
            player1Name = "Player",
            player1Animal = AnimalType.LION,
            player2Animal = AnimalType.WOLF,
          )
        )

        vm.onCellClicked(0)

        val stateAfterTurn = vm.uiState.value
        assertFalse(stateAfterTurn.isAiThinking)
        assertEquals(2, stateAfterTurn.board.moveCount)
        assertEquals(PlayerSlot.PLAYER_1, stateAfterTurn.board.cells[0])
        assertEquals(1, stateAfterTurn.board.cells.count { it == PlayerSlot.PLAYER_2 })
        assertEquals(PlayerSlot.PLAYER_1, stateAfterTurn.board.currentTurn)
      }
    } finally {
      Dispatchers.resetMain()
    }
  }

  @Test
  fun `test1 player vs ai draw then play again starts clean new match preserving config and stats`() =
    runBlocking {
      val testDispatcher = UnconfinedTestDispatcher()
      Dispatchers.setMain(testDispatcher)
      try {
        val statsRepo = InMemoryStatisticsDataSource()
        val vm = GameViewModel(statsRepo, RecordingSoundFeedback(), testDispatcher, aiThinkDelayMs = 0L)
        vm.startNewMatch(
          MatchSetupConfig(
            playMode = PlayMode.LION_VS_AI,
            boardSize = BoardSize.CLASSIC_3X3,
            difficulty = AIDifficulty.EXPERT,
            matchFormat = MatchFormat.SINGLE_ROUND,
            player1Name = "Kael",
            player2Name = "Wolf",
            player1Animal = AnimalType.LION,
            player2Animal = AnimalType.WOLF,
            isAiOpponent = true,
          )
        )

        // Force deterministic 3x3 Draw vs Expert AI
        while (!vm.uiState.value.isGameOver) {
          val move =
            AIEngine.chooseMove(
              vm.uiState.value.board.cells,
              BoardSize.CLASSIC_3X3,
              PlayerSlot.PLAYER_1,
              AIDifficulty.EXPERT,
            )
          vm.onCellClicked(move)
        }

        val drawnState = vm.uiState.value
        assertEquals(GameStatus.DRAW, drawnState.gameStatus)
        assertTrue(drawnState.isDraw)
        assertTrue(drawnState.isGameOver)
        assertNull(drawnState.winner)
        assertEquals(1, drawnState.drawCount)

        // Post-draw clicks must be rejected and must not increment draw stats
        vm.onCellClicked(0)
        assertEquals(1, vm.uiState.value.drawCount)
        val statsAfterDraw = statsRepo.statisticsFlow.first()
        assertEquals(1, statsAfterDraw.totalDraws)
        assertEquals(1, statsAfterDraw.totalGamesPlayed)

        // PLAY AGAIN
        vm.resetGame()

        val rematchState = vm.uiState.value
        assertFalse(rematchState.isGameOver)
        assertFalse(rematchState.isDraw)
        assertEquals(GameStatus.IN_PROGRESS, rematchState.gameStatus)
        assertEquals(PlayerSlot.PLAYER_1, rematchState.currentPlayer)
        assertNull(rematchState.winner)
        assertTrue(rematchState.winningCells.isEmpty())
        assertTrue(rematchState.board.cells.all { it == null })
        assertEquals(0, rematchState.board.moveCount)
        assertFalse(rematchState.isAiThinking)
        assertEquals(1, rematchState.drawCount)
        assertEquals(PlayMode.LION_VS_AI, rematchState.config.playMode)
        assertEquals(AIDifficulty.EXPERT, rematchState.config.difficulty)
        assertEquals(AnimalType.LION, rematchState.config.player1Animal)
        assertEquals(AnimalType.WOLF, rematchState.config.player2Animal)
        assertEquals("Kael", rematchState.config.player1Name)

        // AI responds cleanly only after Player 1 plays
        vm.onCellClicked(0)
        val afterFirstTurn = vm.uiState.value
        assertEquals(PlayerSlot.PLAYER_1, afterFirstTurn.board.cells[0])
        assertEquals(2, afterFirstTurn.board.moveCount)
        assertEquals(1, afterFirstTurn.board.cells.count { it == PlayerSlot.PLAYER_2 })
      } finally {
        Dispatchers.resetMain()
      }
    }

  @Test
  fun `test3 player wins then play again clears winning line and starts fresh match`() =
    runBlocking {
      val testDispatcher = UnconfinedTestDispatcher()
      Dispatchers.setMain(testDispatcher)
      try {
        val statsRepo = InMemoryStatisticsDataSource()
        val vm = GameViewModel(statsRepo, RecordingSoundFeedback(), testDispatcher, aiThinkDelayMs = 0L)
        vm.startNewMatch(
          MatchSetupConfig(
            playMode = PlayMode.LION_VS_WOLF_TWO_PLAYER,
            boardSize = BoardSize.CLASSIC_3X3,
            matchFormat = MatchFormat.SINGLE_ROUND,
            isAiOpponent = false,
            player1Name = "Sol",
            player2Name = "Luna",
          )
        )

        listOf(0, 3, 1, 4, 2).forEach { vm.onCellClicked(it) }
        val wonState = vm.uiState.value
        assertEquals(GameStatus.WON, wonState.gameStatus)
        assertEquals(PlayerSlot.PLAYER_1, wonState.winner)
        assertEquals(setOf(0, 1, 2), wonState.winningCells)
        assertEquals(1, wonState.player1Score)

        vm.resetGame()

        val afterReset = vm.uiState.value
        assertEquals(GameStatus.IN_PROGRESS, afterReset.gameStatus)
        assertFalse(afterReset.isGameOver)
        assertNull(afterReset.winner)
        assertTrue(afterReset.winningCells.isEmpty())
        assertTrue(afterReset.board.cells.all { it == null })
        assertEquals(1, afterReset.player1Score)
        assertEquals("Sol", afterReset.config.player1Name)
        assertEquals("Luna", afterReset.config.player2Name)

        vm.onCellClicked(4)
        assertEquals(PlayerSlot.PLAYER_1, vm.uiState.value.board.cells[4])
        assertEquals(PlayerSlot.PLAYER_2, vm.uiState.value.currentPlayer)
      } finally {
        Dispatchers.resetMain()
      }
    }

  @Test
  fun `test4 ai wins then play again resets cleanly and allows new match`() = runBlocking {
    val testDispatcher = UnconfinedTestDispatcher()
    Dispatchers.setMain(testDispatcher)
    try {
      val statsRepo = InMemoryStatisticsDataSource()
      val vm = GameViewModel(statsRepo, RecordingSoundFeedback(), testDispatcher, aiThinkDelayMs = 0L)
      vm.startNewMatch(
        MatchSetupConfig(
          playMode = PlayMode.LION_VS_AI,
          boardSize = BoardSize.CLASSIC_3X3,
          difficulty = AIDifficulty.EXPERT,
          matchFormat = MatchFormat.SINGLE_ROUND,
          isAiOpponent = true,
          player1Name = "Hero",
        )
      )

      vm.onCellClicked(1)
      vm.onCellClicked(7)
      vm.onCellClicked(5)
      if (!vm.uiState.value.isGameOver) {
        for (i in 0 until 9) {
          if (!vm.uiState.value.isGameOver && vm.uiState.value.board.cells[i] == null) {
            vm.onCellClicked(i)
          }
        }
      }

      val aiWonState = vm.uiState.value
      assertEquals(GameStatus.WON, aiWonState.gameStatus)
      assertEquals(PlayerSlot.PLAYER_2, aiWonState.winner)
      assertEquals(1, aiWonState.player2Score)

      val stats = statsRepo.statisticsFlow.first()
      assertEquals(1, stats.aiWins)
      assertEquals(1, stats.totalLosses)

      vm.resetGame()

      val fresh = vm.uiState.value
      assertEquals(GameStatus.IN_PROGRESS, fresh.gameStatus)
      assertFalse(fresh.isGameOver)
      assertNull(fresh.winner)
      assertTrue(fresh.winningCells.isEmpty())
      assertTrue(fresh.board.cells.all { it == null })
      assertEquals(1, fresh.player2Score)
      assertEquals(PlayerSlot.PLAYER_1, fresh.currentPlayer)

      vm.onCellClicked(4)
      assertEquals(PlayerSlot.PLAYER_1, vm.uiState.value.board.cells[4])
      assertEquals(2, vm.uiState.value.board.moveCount)
    } finally {
      Dispatchers.resetMain()
    }
  }

  @Test
  fun `test6 rapid play again clicks cancel pending ai jobs and prevent duplicate moves or stats`() =
    runBlocking {
      val scheduler = TestCoroutineScheduler()
      val testDispatcher = StandardTestDispatcher(scheduler)
      Dispatchers.setMain(testDispatcher)
      try {
        val statsRepo = InMemoryStatisticsDataSource()
        val vm = GameViewModel(statsRepo, RecordingSoundFeedback(), testDispatcher, aiThinkDelayMs = 300L)

        // Single Round vs AI: mash resetGame() 4 times while AI is mid-delay
        vm.startNewMatch(
          MatchSetupConfig(
            playMode = PlayMode.LION_VS_AI,
            boardSize = BoardSize.CLASSIC_3X3,
            difficulty = AIDifficulty.EXPERT,
            matchFormat = MatchFormat.SINGLE_ROUND,
            isAiOpponent = true,
          )
        )
        vm.onCellClicked(0)
        assertTrue(vm.uiState.value.isAiThinking)

        repeat(4) { vm.resetGame() }
        scheduler.advanceTimeBy(1000L)
        scheduler.runCurrent()

        val afterRapidSingle = vm.uiState.value
        assertEquals(0, afterRapidSingle.board.moveCount)
        assertTrue(afterRapidSingle.board.cells.all { it == null })
        assertFalse(afterRapidSingle.isAiThinking)
        assertEquals(PlayerSlot.PLAYER_1, afterRapidSingle.currentPlayer)
        assertEquals(0, statsRepo.statisticsFlow.first().totalGamesPlayed)

        // Best-of-3 Round 2 (AI starts): mash resetGame() repeatedly while AI is about to move
        vm.startNewMatch(
          MatchSetupConfig(
            playMode = PlayMode.LION_VS_AI,
            boardSize = BoardSize.CLASSIC_3X3,
            difficulty = AIDifficulty.MEDIUM,
            matchFormat = MatchFormat.BEST_OF_3,
            isAiOpponent = true,
          )
        )
        while (!vm.uiState.value.isGameOver) {
          val move =
            AIEngine.chooseMove(
              vm.uiState.value.board.cells,
              BoardSize.CLASSIC_3X3,
              PlayerSlot.PLAYER_1,
              AIDifficulty.EXPERT,
            )
          vm.onCellClicked(move)
          scheduler.advanceTimeBy(400L)
          scheduler.runCurrent()
        }
        assertFalse(vm.uiState.value.isSeriesComplete)

        vm.startNextRound()
        assertEquals(2, vm.uiState.value.currentRound)
        assertTrue(vm.uiState.value.isAiThinking)

        vm.resetGame()
        scheduler.advanceTimeBy(100L)
        vm.resetGame()
        scheduler.advanceTimeBy(100L)
        vm.resetGame()
        scheduler.advanceTimeBy(1000L)
        scheduler.runCurrent()

        // Exactly 1 AI opening move on the Round 2 board — zero duplicate moves!
        assertEquals(1, vm.uiState.value.board.moveCount)
        assertEquals(1, vm.uiState.value.board.cells.count { it == PlayerSlot.PLAYER_2 })
        assertEquals(PlayerSlot.PLAYER_1, vm.uiState.value.currentPlayer)
        assertFalse(vm.uiState.value.isAiThinking)
      } finally {
        Dispatchers.resetMain()
      }
    }
}
