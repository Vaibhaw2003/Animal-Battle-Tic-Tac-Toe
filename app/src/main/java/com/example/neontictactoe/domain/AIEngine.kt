package com.example.neontictactoe.domain

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object AIEngine {

  private val directions = listOf(0 to 1, 1 to 0, 1 to 1, 1 to -1)

  fun chooseMove(
    board: List<PlayerSlot?>,
    boardSize: BoardSize,
    aiPlayer: PlayerSlot,
    difficulty: AIDifficulty,
    random: Random = Random.Default,
  ): Int {
    if (board.size != boardSize.totalCells) return -1
    if (GameEngine.checkWinner(board, boardSize.gridSize, boardSize.winLength) != null) {
      return -1
    }
    val available = GameEngine.availableMoves(board)
    if (available.isEmpty()) return -1
    if (available.size == 1) return available.first()

    return when (difficulty) {
      AIDifficulty.EASY -> chooseEasyMove(board, boardSize, aiPlayer, available, random)
      AIDifficulty.MEDIUM -> chooseMediumMove(board, boardSize, aiPlayer, available, random)
      AIDifficulty.HARD -> chooseHardMove(board, boardSize, aiPlayer, available, random)
      AIDifficulty.EXPERT -> chooseExpertMove(board, boardSize, aiPlayer, available)
    }
  }

  private fun chooseEasyMove(
    board: List<PlayerSlot?>,
    boardSize: BoardSize,
    aiPlayer: PlayerSlot,
    available: List<Int>,
    random: Random,
  ): Int {
    if (random.nextFloat() < 0.18f) {
      findImmediateWinningMove(board, boardSize.gridSize, boardSize.winLength, aiPlayer)?.let {
        return it
      }
    }
    return available[random.nextInt(available.size)]
  }

  private fun chooseMediumMove(
    board: List<PlayerSlot?>,
    boardSize: BoardSize,
    aiPlayer: PlayerSlot,
    available: List<Int>,
    random: Random,
  ): Int {
    val gridSize = boardSize.gridSize
    val winLength = boardSize.winLength
    val humanPlayer = aiPlayer.opponent()

    findImmediateWinningMove(board, gridSize, winLength, aiPlayer)?.let { return it }
    findImmediateWinningMove(board, gridSize, winLength, humanPlayer)?.let { return it }

    if (random.nextFloat() < 0.75f) {
      val center = (gridSize / 2) * gridSize + (gridSize / 2)
      if (board[center] == null && random.nextFloat() < 0.70f) {
        return center
      }
      val candidates = gatherCandidateMoves(board, gridSize, available, maxCandidates = 6)
      if (candidates.isNotEmpty()) {
        return candidates[random.nextInt(min(3, candidates.size))]
      }
    }
    return available[random.nextInt(available.size)]
  }

  private fun chooseHardMove(
    board: List<PlayerSlot?>,
    boardSize: BoardSize,
    aiPlayer: PlayerSlot,
    available: List<Int>,
    random: Random,
  ): Int {
    val gridSize = boardSize.gridSize
    val winLength = boardSize.winLength
    val humanPlayer = aiPlayer.opponent()

    findImmediateWinningMove(board, gridSize, winLength, aiPlayer)?.let { return it }
    findImmediateWinningMove(board, gridSize, winLength, humanPlayer)?.let { return it }

    if (boardSize == BoardSize.CLASSIC_3X3) {
      return chooseMinimax3x3(
        board = board,
        aiPlayer = aiPlayer,
        maxDepth = 6,
        allowSuboptimalTieBreak = true,
        random = random,
      )
    }

    findOpenThreatMove(board, gridSize, winLength, aiPlayer)?.let { return it }
    findOpenThreatMove(board, gridSize, winLength, humanPlayer)?.let { return it }

    return chooseHeuristicAlphaBeta(
      board = board,
      gridSize = gridSize,
      winLength = winLength,
      aiPlayer = aiPlayer,
      maxDepth = 2,
      maxCandidates = 11,
      available = available,
    )
  }

  private fun chooseExpertMove(
    board: List<PlayerSlot?>,
    boardSize: BoardSize,
    aiPlayer: PlayerSlot,
    available: List<Int>,
  ): Int {
    val gridSize = boardSize.gridSize
    val winLength = boardSize.winLength
    val humanPlayer = aiPlayer.opponent()

    findImmediateWinningMove(board, gridSize, winLength, aiPlayer)?.let { return it }
    findImmediateWinningMove(board, gridSize, winLength, humanPlayer)?.let { return it }

    if (boardSize == BoardSize.CLASSIC_3X3) {
      return chooseMinimax3x3(
        board = board,
        aiPlayer = aiPlayer,
        maxDepth = 9,
        allowSuboptimalTieBreak = false,
        random = Random.Default,
      )
    }

    findOpenThreatMove(board, gridSize, winLength, aiPlayer)?.let { return it }
    findOpenThreatMove(board, gridSize, winLength, humanPlayer)?.let { return it }

    findForkMove(board, gridSize, winLength, aiPlayer, available)?.let { return it }
    findForkMove(board, gridSize, winLength, humanPlayer, available)?.let { return it }

    val maxCand = if (gridSize <= 5) 14 else 12
    return chooseHeuristicAlphaBeta(
      board = board,
      gridSize = gridSize,
      winLength = winLength,
      aiPlayer = aiPlayer,
      maxDepth = 3,
      maxCandidates = maxCand,
      available = available,
    )
  }

  private fun findImmediateWinningMove(
    board: List<PlayerSlot?>,
    gridSize: Int,
    winLength: Int,
    player: PlayerSlot,
  ): Int? {
    val mutable = board.toMutableList()
    for (idx in mutable.indices) {
      if (mutable[idx] == null) {
        mutable[idx] = player
        val won = GameEngine.didMoveWin(mutable, gridSize, winLength, idx, player)
        mutable[idx] = null
        if (won) return idx
      }
    }
    return null
  }

  private fun findForkMove(
    board: List<PlayerSlot?>,
    gridSize: Int,
    winLength: Int,
    player: PlayerSlot,
    available: List<Int>,
  ): Int? {
    val candidates = gatherCandidateMoves(board, gridSize, available, maxCandidates = 18)
    val mutable = board.toMutableList()

    for (idx in candidates) {
      mutable[idx] = player
      var winningFollowUps = 0
      for (follow in candidates) {
        if (follow != idx && mutable[follow] == null) {
          mutable[follow] = player
          if (GameEngine.didMoveWin(mutable, gridSize, winLength, follow, player)) {
            winningFollowUps++
          }
          mutable[follow] = null
          if (winningFollowUps >= 2) {
            mutable[idx] = null
            return idx
          }
        }
      }
      mutable[idx] = null
    }
    return null
  }

  private fun findOpenThreatMove(
    board: List<PlayerSlot?>,
    gridSize: Int,
    winLength: Int,
    player: PlayerSlot,
  ): Int? {
    if (winLength < 4) return null
    val mutable = board.toMutableList()
    val candidates =
      gatherCandidateMoves(board, gridSize, GameEngine.availableMoves(board), maxCandidates = 20)
    for (idx in candidates) {
      mutable[idx] = player
      val createsOpenThreat = hasOpenThreatAroundCell(mutable, gridSize, winLength, idx, player)
      mutable[idx] = null
      if (createsOpenThreat) return idx
    }
    return null
  }

  private fun hasOpenThreatAroundCell(
    board: List<PlayerSlot?>,
    gridSize: Int,
    winLength: Int,
    moveIndex: Int,
    player: PlayerSlot,
  ): Boolean {
    val targetLen = winLength - 1
    val row = moveIndex / gridSize
    val col = moveIndex % gridSize

    for ((dr, dc) in directions) {
      var back = 0
      var r = row - dr
      var c = col - dc
      while (r in 0 until gridSize && c in 0 until gridSize && board[r * gridSize + c] == player) {
        back++
        r -= dr
        c -= dc
      }
      val beforeOpen =
        r in 0 until gridSize && c in 0 until gridSize && board[r * gridSize + c] == null

      var forward = 0
      r = row + dr
      c = col + dc
      while (r in 0 until gridSize && c in 0 until gridSize && board[r * gridSize + c] == player) {
        forward++
        r += dr
        c += dc
      }
      val afterOpen =
        r in 0 until gridSize && c in 0 until gridSize && board[r * gridSize + c] == null

      if (1 + back + forward >= targetLen && beforeOpen && afterOpen) {
        return true
      }
    }
    return false
  }

  private fun chooseMinimax3x3(
    board: List<PlayerSlot?>,
    aiPlayer: PlayerSlot,
    maxDepth: Int,
    allowSuboptimalTieBreak: Boolean,
    random: Random,
  ): Int {
    val occupiedCount = board.count { it != null }
    if (occupiedCount == 0) {
      return 4
    }
    if (occupiedCount == 1 && board[4] == null) {
      return 4
    }

    val mutable = board.toMutableList()
    var bestScore = Int.MIN_VALUE
    val scoredMoves = ArrayList<Pair<Int, Int>>()

    for (idx in mutable.indices) {
      if (mutable[idx] == null) {
        mutable[idx] = aiPlayer
        val score =
          if (GameEngine.didMoveWin(mutable, 3, 3, idx, aiPlayer)) {
            99
          } else {
            minimax3x3(
              board = mutable,
              depth = 1,
              maxDepth = maxDepth,
              isMaximizing = false,
              alpha = Int.MIN_VALUE,
              beta = Int.MAX_VALUE,
              aiPlayer = aiPlayer,
              lastMove = idx,
              lastMover = aiPlayer,
            )
          }
        mutable[idx] = null
        scoredMoves.add(idx to score)
        if (score > bestScore) {
          bestScore = score
        }
      }
    }

    val bestMoves = scoredMoves.filter { it.second == bestScore }.map { it.first }
    if (allowSuboptimalTieBreak && scoredMoves.size > 3 && random.nextFloat() < 0.12f) {
      val nonLosing = scoredMoves.filter { it.second >= 0 }.map { it.first }
      if (nonLosing.isNotEmpty()) {
        return nonLosing[random.nextInt(nonLosing.size)]
      }
    }

    val priorityOrder = listOf(4, 0, 2, 6, 8, 1, 3, 5, 7)
    for (preferred in priorityOrder) {
      if (preferred in bestMoves) return preferred
    }
    return bestMoves.first()
  }

  private fun minimax3x3(
    board: MutableList<PlayerSlot?>,
    depth: Int,
    maxDepth: Int,
    isMaximizing: Boolean,
    alpha: Int,
    beta: Int,
    aiPlayer: PlayerSlot,
    lastMove: Int,
    lastMover: PlayerSlot,
  ): Int {
    if (GameEngine.didMoveWin(board, 3, 3, lastMove, lastMover)) {
      return if (lastMover == aiPlayer) 100 - depth else depth - 100
    }
    if (board.none { it == null } || depth >= maxDepth) return 0

    var a = alpha
    var b = beta
    if (isMaximizing) {
      var best = Int.MIN_VALUE
      for (i in board.indices) {
        if (board[i] == null) {
          board[i] = aiPlayer
          val score = minimax3x3(board, depth + 1, maxDepth, false, a, b, aiPlayer, i, aiPlayer)
          board[i] = null
          best = max(best, score)
          a = max(a, best)
          if (b <= a) break
        }
      }
      return best
    } else {
      val human = aiPlayer.opponent()
      var best = Int.MAX_VALUE
      for (i in board.indices) {
        if (board[i] == null) {
          board[i] = human
          val score = minimax3x3(board, depth + 1, maxDepth, true, a, b, aiPlayer, i, human)
          board[i] = null
          best = min(best, score)
          b = min(b, best)
          if (b <= a) break
        }
      }
      return best
    }
  }

  private fun chooseHeuristicAlphaBeta(
    board: List<PlayerSlot?>,
    gridSize: Int,
    winLength: Int,
    aiPlayer: PlayerSlot,
    maxDepth: Int,
    maxCandidates: Int,
    available: List<Int>,
  ): Int {
    if (board.all { it == null }) {
      return (gridSize / 2) * gridSize + (gridSize / 2)
    }

    val mutable = board.toMutableList()
    val candidates = gatherCandidateMoves(mutable, gridSize, available, maxCandidates)
    var bestMove = candidates.first()
    var bestScore = Int.MIN_VALUE
    var alpha = Int.MIN_VALUE
    val beta = Int.MAX_VALUE

    for (move in candidates) {
      mutable[move] = aiPlayer
      val score =
        alphaBetaLarge(
          board = mutable,
          gridSize = gridSize,
          winLength = winLength,
          depth = 1,
          maxDepth = maxDepth,
          isMaximizing = false,
          alpha = alpha,
          beta = beta,
          aiPlayer = aiPlayer,
          maxCandidates = maxCandidates,
          lastMove = move,
          lastMover = aiPlayer,
        )
      mutable[move] = null
      if (score > bestScore) {
        bestScore = score
        bestMove = move
      }
      alpha = max(alpha, bestScore)
    }
    return bestMove
  }

  private fun alphaBetaLarge(
    board: MutableList<PlayerSlot?>,
    gridSize: Int,
    winLength: Int,
    depth: Int,
    maxDepth: Int,
    isMaximizing: Boolean,
    alpha: Int,
    beta: Int,
    aiPlayer: PlayerSlot,
    maxCandidates: Int,
    lastMove: Int,
    lastMover: PlayerSlot,
  ): Int {
    if (GameEngine.didMoveWin(board, gridSize, winLength, lastMove, lastMover)) {
      return if (lastMover == aiPlayer) 1_000_000 - depth * 1000 else depth * 1000 - 1_000_000
    }

    val available = GameEngine.availableMoves(board)
    if (available.isEmpty()) return 0
    if (depth >= maxDepth) {
      return evaluateBoardHeuristic(board, gridSize, winLength, aiPlayer)
    }

    val candidates = gatherCandidateMoves(board, gridSize, available, maxCandidates)
    var a = alpha
    var b = beta

    if (isMaximizing) {
      var best = Int.MIN_VALUE
      for (move in candidates) {
        board[move] = aiPlayer
        val score =
          alphaBetaLarge(
            board = board,
            gridSize = gridSize,
            winLength = winLength,
            depth = depth + 1,
            maxDepth = maxDepth,
            isMaximizing = false,
            alpha = a,
            beta = b,
            aiPlayer = aiPlayer,
            maxCandidates = maxCandidates,
            lastMove = move,
            lastMover = aiPlayer,
          )
        board[move] = null
        best = max(best, score)
        a = max(a, best)
        if (b <= a) break
      }
      return best
    } else {
      val human = aiPlayer.opponent()
      var best = Int.MAX_VALUE
      for (move in candidates) {
        board[move] = human
        val score =
          alphaBetaLarge(
            board = board,
            gridSize = gridSize,
            winLength = winLength,
            depth = depth + 1,
            maxDepth = maxDepth,
            isMaximizing = true,
            alpha = a,
            beta = b,
            aiPlayer = aiPlayer,
            maxCandidates = maxCandidates,
            lastMove = move,
            lastMover = human,
          )
        board[move] = null
        best = min(best, score)
        b = min(b, best)
        if (b <= a) break
      }
      return best
    }
  }

  private fun gatherCandidateMoves(
    board: List<PlayerSlot?>,
    gridSize: Int,
    available: List<Int>,
    maxCandidates: Int,
  ): List<Int> {
    if (available.size <= maxCandidates) {
      return orderByProximityAndCenter(board, gridSize, available)
    }
    val occupied = ArrayList<Pair<Int, Int>>()
    for (i in board.indices) {
      if (board[i] != null) {
        occupied.add((i / gridSize) to (i % gridSize))
      }
    }
    if (occupied.isEmpty()) {
      return orderByProximityAndCenter(board, gridSize, available).take(maxCandidates)
    }

    val near = ArrayList<Int>()
    for (idx in available) {
      val r = idx / gridSize
      val c = idx % gridSize
      val isClose = occupied.any { (or, oc) -> max(abs(r - or), abs(c - oc)) <= 2 }
      if (isClose) near.add(idx)
    }

    val pool = if (near.isNotEmpty()) near else available
    return orderByProximityAndCenter(board, gridSize, pool).take(maxCandidates)
  }

  private fun orderByProximityAndCenter(
    board: List<PlayerSlot?>,
    gridSize: Int,
    moves: List<Int>,
  ): List<Int> {
    val center = (gridSize - 1) / 2.0
    return moves.sortedByDescending { idx ->
      val r = idx / gridSize
      val c = idx % gridSize
      var neighborBonus = 0
      for (dr in -1..1) {
        for (dc in -1..1) {
          if (dr == 0 && dc == 0) continue
          val nr = r + dr
          val nc = c + dc
          if (nr in 0 until gridSize && nc in 0 until gridSize && board[nr * gridSize + nc] != null) {
            neighborBonus += 12
          }
        }
      }
      val distPenalty = ((abs(r - center) + abs(c - center)) * 2).toInt()
      neighborBonus - distPenalty
    }
  }

  private fun evaluateBoardHeuristic(
    board: List<PlayerSlot?>,
    gridSize: Int,
    winLength: Int,
    aiPlayer: PlayerSlot,
  ): Int {
    val human = aiPlayer.opponent()
    var score = 0

    for (r in 0 until gridSize) {
      for (c in 0 until gridSize) {
        for ((dr, dc) in directions) {
          val endR = r + (winLength - 1) * dr
          val endC = c + (winLength - 1) * dc
          if (endR !in 0 until gridSize || endC !in 0 until gridSize) continue

          var aiCount = 0
          var humanCount = 0
          for (step in 0 until winLength) {
            val cell = board[(r + step * dr) * gridSize + (c + step * dc)]
            if (cell == aiPlayer) {
              aiCount++
            } else if (cell == human) {
              humanCount++
            }
          }
          if (aiCount > 0 && humanCount == 0) {
            score += weightForCount(aiCount, winLength)
          } else if (humanCount > 0 && aiCount == 0) {
            score -= (weightForCount(humanCount, winLength) * 1.2).toInt()
          }
        }
      }
    }

    val center = gridSize / 2
    for (i in board.indices) {
      val sym = board[i] ?: continue
      val r = i / gridSize
      val c = i % gridSize
      val centerValue = max(0, gridSize - (abs(r - center) + abs(c - center)))
      if (sym == aiPlayer) score += centerValue else score -= centerValue
    }
    return score
  }

  private fun weightForCount(count: Int, winLength: Int): Int {
    if (count >= winLength) return 500_000
    return when (winLength - count) {
      1 -> 6500
      2 -> 420
      3 -> 45
      else -> 6
    }
  }
}
