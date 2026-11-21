package com.example.neontictactoe.domain

data class WinningLine(
  val cells: List<Int>,
  val startRow: Int,
  val startCol: Int,
  val endRow: Int,
  val endCol: Int,
  val winner: PlayerSlot,
)

sealed interface GameResult {
  data object InProgress : GameResult

  data class Win(
    val winner: PlayerSlot,
    val winningLine: WinningLine,
  ) : GameResult

  data object Draw : GameResult
}

data class GameBoardState(
  val boardSize: BoardSize,
  val cells: List<PlayerSlot?>,
  val currentTurn: PlayerSlot,
  val result: GameResult = GameResult.InProgress,
  val moveCount: Int = 0,
  val lastMoveIndex: Int? = null,
  val moveHistory: List<Int> = emptyList(),
) {
  val isGameOver: Boolean
    get() = result !is GameResult.InProgress

  val winningCellSet: Set<Int>
    get() =
      when (val r = result) {
        is GameResult.Win -> r.winningLine.cells.toSet()
        else -> emptySet()
      }
}
