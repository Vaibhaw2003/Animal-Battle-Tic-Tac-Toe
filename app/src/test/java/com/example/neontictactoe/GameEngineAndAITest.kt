package com.example.neontictactoe

import com.example.neontictactoe.domain.AIDifficulty
import com.example.neontictactoe.domain.AIEngine
import com.example.neontictactoe.domain.BoardSize
import com.example.neontictactoe.domain.GameBoardState
import com.example.neontictactoe.domain.GameEngine
import com.example.neontictactoe.domain.GameResult
import com.example.neontictactoe.domain.PlayerSlot
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineAndAITest {

  @Test
  fun `all board sizes detect horizontal vertical diagonal and reverse diagonal wins`() {
    for (boardSize in BoardSize.entries) {
      val n = boardSize.gridSize
      val k = boardSize.winLength

      // Horizontal win on row 1 (offset col 1 if room)
      val hCells = MutableList<PlayerSlot?>(n * n) { null }
      val colStart = if (n > k) 1 else 0
      for (c in colStart until colStart + k) hCells[1 * n + c] = PlayerSlot.PLAYER_1
      val hWin = GameEngine.checkWinner(hCells, n, k)
      assertNotNull("Horizontal win failed for $boardSize", hWin)
      assertEquals(PlayerSlot.PLAYER_1, hWin?.winner)
      assertTrue(GameEngine.didMoveWin(hCells, n, k, 1 * n + colStart + 1, PlayerSlot.PLAYER_1))

      // Vertical win on last column
      val vCells = MutableList<PlayerSlot?>(n * n) { null }
      val rowStart = if (n > k) 1 else 0
      for (r in rowStart until rowStart + k) vCells[r * n + (n - 1)] = PlayerSlot.PLAYER_2
      val vWin = GameEngine.checkWinner(vCells, n, k)
      assertNotNull("Vertical win failed for $boardSize", vWin)
      assertEquals(PlayerSlot.PLAYER_2, vWin?.winner)
      assertTrue(GameEngine.didMoveWin(vCells, n, k, rowStart * n + (n - 1), PlayerSlot.PLAYER_2))

      // Main diagonal win (\)
      val dCells = MutableList<PlayerSlot?>(n * n) { null }
      for (step in 0 until k) dCells[step * n + step] = PlayerSlot.PLAYER_1
      val dWin = GameEngine.checkWinner(dCells, n, k)
      assertNotNull("Diagonal win failed for $boardSize", dWin)
      assertEquals(PlayerSlot.PLAYER_1, dWin?.winner)
      assertTrue(GameEngine.didMoveWin(dCells, n, k, 0, PlayerSlot.PLAYER_1))

      // Reverse diagonal win (/)
      val rdCells = MutableList<PlayerSlot?>(n * n) { null }
      for (step in 0 until k) rdCells[step * n + (n - 1 - step)] = PlayerSlot.PLAYER_2
      val rdWin = GameEngine.checkWinner(rdCells, n, k)
      assertNotNull("Reverse diagonal win failed for $boardSize", rdWin)
      assertEquals(PlayerSlot.PLAYER_2, rdWin?.winner)
      assertTrue(GameEngine.didMoveWin(rdCells, n, k, n - 1, PlayerSlot.PLAYER_2))
    }
  }

  @Test
  fun `draw detection works on 3x3 and 5x5 boards and ai rejects finished boards`() {
    var state3 = GameEngine.createInitialBoard(BoardSize.CLASSIC_3X3)
    for (move in listOf(0, 1, 2, 4, 3, 5, 7, 6, 8)) {
      state3 = requireNotNull(GameEngine.makeMove(state3, move))
    }
    assertTrue(state3.result is GameResult.Draw)
    assertTrue(state3.isGameOver)
    assertNull(GameEngine.makeMove(state3, 0))
    assertEquals(
      -1,
      AIEngine.chooseMove(
        state3.cells,
        BoardSize.CLASSIC_3X3,
        PlayerSlot.PLAYER_2,
        AIDifficulty.EXPERT,
      ),
    )

    // 5x5 Draw pattern (blocks of 2 alternating slots so no 4-in-a-row exists anywhere)
    val draw5Cells =
      List(25) { idx ->
        val r = idx / 5
        val c = idx % 5
        val base = (c / 2) % 2 == 0
        val flip = (r % 2) == 1
        if (base xor flip) PlayerSlot.PLAYER_1 else PlayerSlot.PLAYER_2
      }
    assertNull("5x5 draw board should have no winner", GameEngine.checkWinner(draw5Cells, 5, 4))
    assertEquals(
      -1,
      AIEngine.chooseMove(draw5Cells, BoardSize.MEDIUM_5X5, PlayerSlot.PLAYER_2, AIDifficulty.HARD),
    )

    // Already-won board with empty cells must also return -1 from AI
    val wonBoard = MutableList<PlayerSlot?>(9) { null }
    wonBoard[0] = PlayerSlot.PLAYER_1
    wonBoard[1] = PlayerSlot.PLAYER_1
    wonBoard[2] = PlayerSlot.PLAYER_1
    assertEquals(
      -1,
      AIEngine.chooseMove(wonBoard, BoardSize.CLASSIC_3X3, PlayerSlot.PLAYER_2, AIDifficulty.EXPERT),
    )
  }

  @Test
  fun `invalid moves are rejected on occupied out of bounds or finished games`() {
    var state = GameEngine.createInitialBoard(BoardSize.CLASSIC_3X3)
    assertNull(GameEngine.makeMove(state, -1))
    assertNull(GameEngine.makeMove(state, 9))

    state = requireNotNull(GameEngine.makeMove(state, 0)) // Player 1
    assertNull("Occupied cell should be rejected", GameEngine.makeMove(state, 0))
    assertFalse(GameEngine.isValidMove(state, 0))

    // Complete a quick Player 1 win on top row: P1(0), P2(3), P1(1), P2(4), P1(2)
    state = requireNotNull(GameEngine.makeMove(state, 3))
    state = requireNotNull(GameEngine.makeMove(state, 1))
    state = requireNotNull(GameEngine.makeMove(state, 4))
    state = requireNotNull(GameEngine.makeMove(state, 2))
    assertTrue(state.result is GameResult.Win)
    assertNull("Moves after win must be rejected", GameEngine.makeMove(state, 5))
  }

  @Test
  fun `ai never picks occupied cell across all difficulties and board sizes`() {
    val rng = Random(1234)
    for (boardSize in BoardSize.entries) {
      val n = boardSize.gridSize
      val board = MutableList<PlayerSlot?>(n * n) { null }
      board[0] = PlayerSlot.PLAYER_1
      board[n / 2 * n + n / 2] = PlayerSlot.PLAYER_2
      board[n * n - 1] = PlayerSlot.PLAYER_1

      for (difficulty in AIDifficulty.entries) {
        val move = AIEngine.chooseMove(board, boardSize, PlayerSlot.PLAYER_2, difficulty, rng)
        assertTrue("Invalid index $move for $boardSize $difficulty", move in board.indices)
        assertNull("AI chose occupied cell $move for $boardSize $difficulty", board[move])
      }
    }
  }

  @Test
  fun `medium hard and expert ai take wins and block threats across all board sizes`() {
    val tacticalDifficulties = listOf(AIDifficulty.MEDIUM, AIDifficulty.HARD, AIDifficulty.EXPERT)
    for (boardSize in BoardSize.entries) {
      val n = boardSize.gridSize
      val k = boardSize.winLength

      for (difficulty in tacticalDifficulties) {
        // 1. AI immediate win on row 0: (k - 1) P2s followed by empty cell at index (k - 1)
        val winBoard = MutableList<PlayerSlot?>(n * n) { null }
        for (c in 0 until k - 1) {
          winBoard[c] = PlayerSlot.PLAYER_2
        }
        for (c in 0 until k - 1) {
          winBoard[(n - 1) * n + c] = PlayerSlot.PLAYER_1
        }
        val winMove = AIEngine.chooseMove(winBoard, boardSize, PlayerSlot.PLAYER_2, difficulty, Random(42))
        assertEquals(
          "Expected immediate win at ${k - 1} for $boardSize $difficulty",
          k - 1,
          winMove,
        )

        // 2. AI must block human immediate diagonal win: (k - 1) P1s on main diagonal
        val blockBoard = MutableList<PlayerSlot?>(n * n) { null }
        for (step in 0 until k - 1) {
          blockBoard[step * n + step] = PlayerSlot.PLAYER_1
        }
        blockBoard[n - 1] = PlayerSlot.PLAYER_2
        val expectedBlock = (k - 1) * n + (k - 1)
        val blockMove =
          AIEngine.chooseMove(blockBoard, boardSize, PlayerSlot.PLAYER_2, difficulty, Random(42))
        assertEquals(
          "Expected immediate block at $expectedBlock for $boardSize $difficulty",
          expectedBlock,
          blockMove,
        )
      }
    }
  }

  @Test
  fun `expert ai creates or blocks open threats and completes full 9x9 simulation cleanly`() {
    val board5 = MutableList<PlayerSlot?>(25) { null }
    board5[2 * 5 + 1] = PlayerSlot.PLAYER_1
    board5[2 * 5 + 2] = PlayerSlot.PLAYER_1
    board5[0] = PlayerSlot.PLAYER_2
    val move5 = AIEngine.chooseMove(board5, BoardSize.MEDIUM_5X5, PlayerSlot.PLAYER_2, AIDifficulty.EXPERT)
    assertTrue(
      "Expected Expert AI to contest row 2 threat, got $move5",
      move5 == 2 * 5 + 0 || move5 == 2 * 5 + 3,
    )

    var state = GameEngine.createInitialBoard(BoardSize.EXPERT_9X9)
    while (!state.isGameOver) {
      val move = AIEngine.chooseMove(state.cells, state.boardSize, state.currentTurn, AIDifficulty.EXPERT)
      assertTrue("AI move $move out of bounds", move in state.cells.indices)
      assertNull("AI move $move occupied", state.cells[move])
      state = requireNotNull(GameEngine.makeMove(state, move))
    }
    assertTrue(state.result is GameResult.Win || state.result is GameResult.Draw)
  }

  @Test
  fun `expert ai never loses on 3x3 whether playing second or first`() {
    // Case 1: Human plays first (PLAYER_1), Expert AI plays second (PLAYER_2)
    for (firstMove in 0 until 9) {
      val afterHuman =
        requireNotNull(GameEngine.makeMove(GameEngine.createInitialBoard(BoardSize.CLASSIC_3X3), firstMove))
      verifyExpertNeverLoses(afterHuman, aiSlot = PlayerSlot.PLAYER_2)
    }

    // Case 2: Expert AI plays first (PLAYER_1), Human plays second (PLAYER_2)
    val aiFirst = AIEngine.chooseMove(List(9) { null }, BoardSize.CLASSIC_3X3, PlayerSlot.PLAYER_1, AIDifficulty.EXPERT)
    val afterAi =
      requireNotNull(GameEngine.makeMove(GameEngine.createInitialBoard(BoardSize.CLASSIC_3X3), aiFirst))
    verifyExpertNeverLoses(afterAi, aiSlot = PlayerSlot.PLAYER_1)
  }

  private fun verifyExpertNeverLoses(
    state: GameBoardState,
    aiSlot: PlayerSlot,
  ) {
    val humanSlot = aiSlot.opponent()
    when (val res = state.result) {
      is GameResult.Win -> {
        assertTrue("Expert AI ($aiSlot) lost a 3x3 branch!", res.winner != humanSlot)
        return
      }
      is GameResult.Draw -> return
      is GameResult.InProgress -> {
        if (state.currentTurn == aiSlot) {
          val aiMove = AIEngine.chooseMove(state.cells, state.boardSize, aiSlot, AIDifficulty.EXPERT)
          val next = requireNotNull(GameEngine.makeMove(state, aiMove))
          verifyExpertNeverLoses(next, aiSlot)
        } else {
          for (humanMove in GameEngine.availableMoves(state.cells)) {
            val next = requireNotNull(GameEngine.makeMove(state, humanMove))
            verifyExpertNeverLoses(next, aiSlot)
          }
        }
      }
    }
  }
}
