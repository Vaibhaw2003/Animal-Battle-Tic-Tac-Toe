package com.example.neontictactoe.domain

object GameEngine {

  private val directions =
    listOf(
      0 to 1, // Horizontal
      1 to 0, // Vertical
      1 to 1, // Diagonal (\)
      1 to -1, // Reverse Diagonal (/)
    )

  fun createInitialBoard(
    boardSize: BoardSize,
    startingPlayer: PlayerSlot = PlayerSlot.PLAYER_1,
  ): GameBoardState {
    return GameBoardState(
      boardSize = boardSize,
      cells = List(boardSize.totalCells) { null },
      currentTurn = startingPlayer,
      result = GameResult.InProgress,
      moveCount = 0,
      lastMoveIndex = null,
      moveHistory = emptyList(),
    )
  }

  fun isValidMove(state: GameBoardState, index: Int): Boolean {
    if (state.isGameOver) return false
    if (index !in state.cells.indices) return false
    return state.cells[index] == null
  }

  fun makeMove(state: GameBoardState, index: Int): GameBoardState? {
    if (!isValidMove(state, index)) return null

    val updatedCells = state.cells.toMutableList()
    val mover = state.currentTurn
    updatedCells[index] = mover

    val nextMoveCount = state.moveCount + 1
    val winLine =
      checkWinner(
        cells = updatedCells,
        gridSize = state.boardSize.gridSize,
        winLength = state.boardSize.winLength,
      )

    val result =
      when {
        winLine != null -> GameResult.Win(winner = winLine.winner, winningLine = winLine)
        updatedCells.none { it == null } -> GameResult.Draw
        else -> GameResult.InProgress
      }

    return state.copy(
      cells = updatedCells,
      currentTurn = if (result is GameResult.InProgress) mover.opponent() else mover,
      result = result,
      moveCount = nextMoveCount,
      lastMoveIndex = index,
      moveHistory = state.moveHistory + index,
    )
  }

  /**
   * Fast O(winLength) check whether placing [player] at [moveIndex] creates a winning line.
   */
  fun didMoveWin(
    cells: List<PlayerSlot?>,
    gridSize: Int,
    winLength: Int,
    moveIndex: Int,
    player: PlayerSlot,
  ): Boolean {
    if (moveIndex !in cells.indices || cells[moveIndex] != player) return false
    val row = moveIndex / gridSize
    val col = moveIndex % gridSize

    for ((dr, dc) in directions) {
      var count = 1

      var r = row + dr
      var c = col + dc
      while (r in 0 until gridSize && c in 0 until gridSize && cells[r * gridSize + c] == player) {
        count++
        if (count >= winLength) return true
        r += dr
        c += dc
      }

      r = row - dr
      c = col - dc
      while (r in 0 until gridSize && c in 0 until gridSize && cells[r * gridSize + c] == player) {
        count++
        if (count >= winLength) return true
        r -= dr
        c -= dc
      }
    }
    return false
  }

  fun checkWinner(
    cells: List<PlayerSlot?>,
    gridSize: Int,
    winLength: Int,
  ): WinningLine? {
    if (cells.size != gridSize * gridSize) return null

    for (row in 0 until gridSize) {
      for (col in 0 until gridSize) {
        val startPlayer = cells[row * gridSize + col] ?: continue
        for ((dr, dc) in directions) {
          val endRow = row + (winLength - 1) * dr
          val endCol = col + (winLength - 1) * dc
          if (endRow !in 0 until gridSize || endCol !in 0 until gridSize) continue

          var matched = true
          val matchedIndices = ArrayList<Int>(winLength)
          matchedIndices.add(row * gridSize + col)
          for (step in 1 until winLength) {
            val r = row + step * dr
            val c = col + step * dc
            val idx = r * gridSize + c
            if (cells[idx] != startPlayer) {
              matched = false
              break
            }
            matchedIndices.add(idx)
          }
          if (matched) {
            return WinningLine(
              cells = matchedIndices,
              startRow = row,
              startCol = col,
              endRow = endRow,
              endCol = endCol,
              winner = startPlayer,
            )
          }
        }
      }
    }
    return null
  }

  fun availableMoves(cells: List<PlayerSlot?>): List<Int> {
    val result = ArrayList<Int>(cells.size)
    for (i in cells.indices) {
      if (cells[i] == null) result.add(i)
    }
    return result
  }
}
