package com.example.neontictactoe.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.neontictactoe.audio.SoundFeedbackController
import com.example.neontictactoe.data.StatisticsDataSource
import com.example.neontictactoe.domain.AIDifficulty
import com.example.neontictactoe.domain.AIEngine
import com.example.neontictactoe.domain.AchievementId
import com.example.neontictactoe.domain.AnimalType
import com.example.neontictactoe.domain.BoardSize
import com.example.neontictactoe.domain.GameBoardState
import com.example.neontictactoe.domain.GameEngine
import com.example.neontictactoe.domain.GameResult
import com.example.neontictactoe.domain.MatchFormat
import com.example.neontictactoe.domain.PlayerSlot
import com.example.neontictactoe.ui.MatchSetupConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class GameStatus {
  IN_PROGRESS,
  WON,
  DRAW,
}

data class GameUiState(
  val config: MatchSetupConfig = MatchSetupConfig(),
  val board: GameBoardState = GameEngine.createInitialBoard(MatchSetupConfig().boardSize),
  val player1Score: Int = 0,
  val player2Score: Int = 0,
  val drawCount: Int = 0,
  val currentRound: Int = 1,
  val isAiThinking: Boolean = false,
  val boardGeneration: Int = 0,
  val recentUnlocks: List<AchievementId> = emptyList(),
) {
  val currentPlayer: PlayerSlot
    get() = board.currentTurn

  val gameStatus: GameStatus
    get() =
      when (board.result) {
        is GameResult.InProgress -> GameStatus.IN_PROGRESS
        is GameResult.Win -> GameStatus.WON
        is GameResult.Draw -> GameStatus.DRAW
      }

  val isGameOver: Boolean
    get() = board.isGameOver

  val isDraw: Boolean
    get() = board.result is GameResult.Draw

  val winningCells: Set<Int>
    get() = board.winningCellSet

  val winner: PlayerSlot?
    get() = (board.result as? GameResult.Win)?.winner

  val player1Animal: AnimalType
    get() = config.player1Animal

  val player2Animal: AnimalType
    get() = config.player2Animal

  fun animalFor(slot: PlayerSlot): AnimalType =
    if (slot == PlayerSlot.PLAYER_1) player1Animal else player2Animal

  val player1DisplayName: String
    get() = config.player1Name.ifBlank { player1Animal.title }

  val player2DisplayName: String
    get() =
      if (config.isAiOpponent) "${player2Animal.title} AI (${config.difficulty.title})"
      else config.player2Name.ifBlank { player2Animal.title }

  val isSeriesComplete: Boolean
    get() {
      val target = config.matchFormat.roundsToWin
      if (config.matchFormat == MatchFormat.SINGLE_ROUND) {
        return board.isGameOver
      }
      return player1Score >= target ||
        player2Score >= target ||
        (board.isGameOver && currentRound >= config.matchFormat.maxRounds)
    }

  val seriesWinnerName: String?
    get() {
      if (!isSeriesComplete) return null
      return when {
        player1Score > player2Score -> player1DisplayName
        player2Score > player1Score -> player2DisplayName
        else -> null
      }
    }
}

class GameViewModel(
  private val statisticsRepository: StatisticsDataSource,
  private val soundFeedback: SoundFeedbackController,
  private val computeDispatcher: CoroutineDispatcher = Dispatchers.Default,
  private val aiThinkDelayMs: Long = 320L,
) : ViewModel() {

  private val _uiState = MutableStateFlow(GameUiState())
  val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

  private var aiJob: Job? = null

  fun startNewMatch(config: MatchSetupConfig) {
    aiJob?.cancel()
    aiJob = null

    val safeP2Animal =
      if (config.player2Animal == config.player1Animal) {
        if (config.player1Animal == AnimalType.WOLF) AnimalType.LION else AnimalType.WOLF
      } else {
        config.player2Animal
      }
    val sanitizedConfig =
      config.copy(
        player1Name = config.player1Name.trim().ifBlank { config.player1Animal.title },
        player2Name = config.player2Name.trim().ifBlank { safeP2Animal.title },
        player2Animal = safeP2Animal,
      )

    _uiState.update { previous ->
      GameUiState(
        config = sanitizedConfig,
        board = GameEngine.createInitialBoard(sanitizedConfig.boardSize, PlayerSlot.PLAYER_1),
        player1Score = 0,
        player2Score = 0,
        drawCount = 0,
        currentRound = 1,
        isAiThinking = false,
        boardGeneration = previous.boardGeneration + 1,
        recentUnlocks = emptyList(),
      )
    }
    soundFeedback.playGameStart()
  }

  /**
   * Resets the board for a rematch ("PLAY AGAIN" / "RESTART"), preserving the current
   * game mode, animals, AI difficulty, player names, and session/lifetime scores.
   * For completed Best-of-3 / Best-of-5 series, begins a fresh round-1 series.
   */
  fun resetGame() {
    aiJob?.cancel()
    aiJob = null

    var shouldTriggerAi = false
    var targetGeneration = 0

    _uiState.update { state ->
      val startNewSeries =
        state.isSeriesComplete && state.config.matchFormat != MatchFormat.SINGLE_ROUND
      val nextRound =
        when {
          state.config.matchFormat == MatchFormat.SINGLE_ROUND -> 1
          startNewSeries -> 1
          else -> state.currentRound
        }
      val startingPlayer = startingPlayerForRound(nextRound)
      val nextGeneration = state.boardGeneration + 1

      shouldTriggerAi = state.config.isAiOpponent && startingPlayer == PlayerSlot.PLAYER_2
      targetGeneration = nextGeneration

      state.copy(
        board = GameEngine.createInitialBoard(state.config.boardSize, startingPlayer),
        player1Score = if (startNewSeries) 0 else state.player1Score,
        player2Score = if (startNewSeries) 0 else state.player2Score,
        drawCount = if (startNewSeries) 0 else state.drawCount,
        currentRound = nextRound,
        isAiThinking = shouldTriggerAi,
        boardGeneration = nextGeneration,
        recentUnlocks = emptyList(),
      )
    }

    soundFeedback.playPlayAgain()

    if (shouldTriggerAi) {
      triggerAiMove(expectedGeneration = targetGeneration)
    }
  }

  fun restartMatch() {
    resetGame()
  }

  fun startNextRound() {
    val state = _uiState.value
    if (state.isSeriesComplete) {
      resetGame()
      return
    }
    aiJob?.cancel()
    aiJob = null

    val nextRound = state.currentRound + 1
    val startingPlayer = startingPlayerForRound(nextRound)
    val nextGeneration = state.boardGeneration + 1
    val shouldTriggerAi = state.config.isAiOpponent && startingPlayer == PlayerSlot.PLAYER_2

    _uiState.update {
      it.copy(
        board = GameEngine.createInitialBoard(it.config.boardSize, startingPlayer),
        currentRound = nextRound,
        isAiThinking = shouldTriggerAi,
        boardGeneration = nextGeneration,
        recentUnlocks = emptyList(),
      )
    }
    soundFeedback.playGameStart()
    if (shouldTriggerAi) {
      triggerAiMove(expectedGeneration = nextGeneration)
    }
  }

  fun onCellClicked(index: Int) {
    val state = _uiState.value
    if (state.isAiThinking) return
    if (state.board.isGameOver) return
    if (state.config.isAiOpponent && state.board.currentTurn == PlayerSlot.PLAYER_2) return

    applyMove(index = index, expectedGeneration = state.boardGeneration, fromAi = false)
  }

  private fun applyMove(
    index: Int,
    expectedGeneration: Int,
    fromAi: Boolean,
  ) {
    val currentState = _uiState.value
    if (currentState.boardGeneration != expectedGeneration) return
    if (currentState.board.isGameOver) {
      if (currentState.isAiThinking) {
        _uiState.update { it.copy(isAiThinking = false) }
      }
      return
    }
    if (fromAi && (!currentState.config.isAiOpponent || currentState.board.currentTurn != PlayerSlot.PLAYER_2)) {
      _uiState.update { it.copy(isAiThinking = false) }
      return
    }
    if (!fromAi && currentState.config.isAiOpponent && currentState.board.currentTurn == PlayerSlot.PLAYER_2) {
      return
    }

    val mover = currentState.board.currentTurn
    val nextBoard = GameEngine.makeMove(currentState.board, index)
    if (nextBoard == null) {
      if (currentState.isAiThinking) {
        _uiState.update { it.copy(isAiThinking = false) }
      }
      return
    }

    val moverAnimal =
      if (mover == PlayerSlot.PLAYER_1) currentState.player1Animal else currentState.player2Animal
    when {
      fromAi -> soundFeedback.playAiMove()
      moverAnimal == AnimalType.WOLF -> soundFeedback.playWolfMove()
      moverAnimal == AnimalType.LION -> soundFeedback.playLionMove()
      mover == PlayerSlot.PLAYER_1 -> soundFeedback.playLionMove()
      else -> soundFeedback.playWolfMove()
    }
    soundFeedback.vibrateMove()

    var nextP1Score = currentState.player1Score
    var nextP2Score = currentState.player2Score
    var nextDraws = currentState.drawCount

    when (val res = nextBoard.result) {
      is GameResult.Win -> {
        if (res.winner == PlayerSlot.PLAYER_1) {
          nextP1Score += 1
          val winsSeries =
            currentState.config.matchFormat != MatchFormat.SINGLE_ROUND &&
              nextP1Score >= currentState.config.matchFormat.roundsToWin
          if (winsSeries) {
            soundFeedback.playVictoryCelebration()
          } else {
            soundFeedback.playWin()
          }
        } else {
          nextP2Score += 1
          if (currentState.config.isAiOpponent) {
            soundFeedback.playGameOver()
          } else {
            val winsSeries =
              currentState.config.matchFormat != MatchFormat.SINGLE_ROUND &&
                nextP2Score >= currentState.config.matchFormat.roundsToWin
            if (winsSeries) {
              soundFeedback.playVictoryCelebration()
            } else {
              soundFeedback.playWin()
            }
          }
        }
        soundFeedback.vibrateWin()
        recordStats(
          isAi = currentState.config.isAiOpponent,
          winner = res.winner,
          boardSize = currentState.board.boardSize,
          difficulty = currentState.config.difficulty,
          matchGeneration = expectedGeneration,
        )
      }
      is GameResult.Draw -> {
        nextDraws += 1
        soundFeedback.playDraw()
        recordStats(
          isAi = currentState.config.isAiOpponent,
          winner = null,
          boardSize = currentState.board.boardSize,
          difficulty = currentState.config.difficulty,
          matchGeneration = expectedGeneration,
        )
      }
      is GameResult.InProgress -> Unit
    }

    val shouldTriggerAiNext =
      nextBoard.result is GameResult.InProgress &&
        currentState.config.isAiOpponent &&
        nextBoard.currentTurn == PlayerSlot.PLAYER_2

    _uiState.update {
      if (it.boardGeneration != expectedGeneration) return@update it
      it.copy(
        board = nextBoard,
        player1Score = nextP1Score,
        player2Score = nextP2Score,
        drawCount = nextDraws,
        isAiThinking = shouldTriggerAiNext,
      )
    }

    if (shouldTriggerAiNext) {
      triggerAiMove(expectedGeneration = expectedGeneration)
    }
  }

  private fun triggerAiMove(expectedGeneration: Int) {
    aiJob?.cancel()
    aiJob =
      viewModelScope.launch {
        if (aiThinkDelayMs > 0L) {
          delay(aiThinkDelayMs)
        }
        if (!isActive) return@launch

        val snapshot = _uiState.value
        if (
          snapshot.boardGeneration != expectedGeneration ||
            snapshot.board.isGameOver ||
            !snapshot.config.isAiOpponent ||
            snapshot.board.currentTurn != PlayerSlot.PLAYER_2
        ) {
          if (snapshot.boardGeneration == expectedGeneration && snapshot.isAiThinking) {
            _uiState.update { it.copy(isAiThinking = false) }
          }
          return@launch
        }

        val expectedMoveCount = snapshot.board.moveCount
        val chosenIndex =
          withContext(computeDispatcher) {
            AIEngine.chooseMove(
              board = snapshot.board.cells,
              boardSize = snapshot.board.boardSize,
              aiPlayer = PlayerSlot.PLAYER_2,
              difficulty = snapshot.config.difficulty,
            )
          }

        if (!isActive) return@launch
        val latest = _uiState.value
        if (
          latest.boardGeneration != expectedGeneration ||
            latest.board.moveCount != expectedMoveCount ||
            latest.board.isGameOver ||
            latest.board.currentTurn != PlayerSlot.PLAYER_2
        ) {
          return@launch
        }

        if (chosenIndex >= 0) {
          applyMove(
            index = chosenIndex,
            expectedGeneration = expectedGeneration,
            fromAi = true,
          )
        } else {
          _uiState.update { it.copy(isAiThinking = false) }
        }
      }
  }

  private fun startingPlayerForRound(round: Int): PlayerSlot =
    if (round % 2 == 1) PlayerSlot.PLAYER_1 else PlayerSlot.PLAYER_2

  private fun recordStats(
    isAi: Boolean,
    winner: PlayerSlot?,
    boardSize: BoardSize,
    difficulty: AIDifficulty,
    matchGeneration: Int,
  ) {
    viewModelScope.launch {
      val unlocked =
        statisticsRepository.recordMatchResult(
          isAiMatch = isAi,
          winner = winner,
          boardSize = boardSize,
          aiDifficulty = difficulty,
        )
      if (unlocked.isNotEmpty()) {
        _uiState.update { current ->
          if (current.boardGeneration == matchGeneration) {
            current.copy(recentUnlocks = unlocked)
          } else {
            current
          }
        }
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    aiJob?.cancel()
    aiJob = null
  }

  class Factory(
    private val statisticsRepository: StatisticsDataSource,
    private val soundFeedback: SoundFeedbackController,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return GameViewModel(statisticsRepository, soundFeedback) as T
    }
  }
}
