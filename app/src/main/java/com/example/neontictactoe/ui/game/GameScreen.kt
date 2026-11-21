package com.example.neontictactoe.ui.game

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.neontictactoe.domain.AnimalType
import com.example.neontictactoe.domain.GameBoardState
import com.example.neontictactoe.domain.GameResult
import com.example.neontictactoe.domain.MatchFormat
import com.example.neontictactoe.domain.PlayerSlot
import com.example.neontictactoe.ui.components.AnimatedAnimalIcon
import com.example.neontictactoe.ui.components.CyberpunkBackground
import com.example.neontictactoe.ui.components.NeonButton
import com.example.neontictactoe.ui.components.NeonIconButton
import com.example.neontictactoe.ui.theme.LocalNeonColors
import com.example.neontictactoe.ui.theme.accentColor
import com.example.neontictactoe.ui.theme.withNeonGlow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun GameScreen(
  uiState: GameUiState,
  soundEnabled: Boolean,
  onToggleSound: () -> Unit,
  onCellClick: (Int) -> Unit,
  onRestartClick: () -> Unit,
  onNextRoundClick: () -> Unit,
  onSettingsClick: () -> Unit,
  onHomeClick: () -> Unit,
  onBackClick: () -> Unit,
) {
  val neon = LocalNeonColors.current
  val board = uiState.board
  val p1Animal = uiState.player1Animal
  val p2Animal = uiState.player2Animal
  val p1Color = p1Animal.accentColor()
  val p2Color = p2Animal.accentColor()

  CyberpunkBackground(modifier = Modifier.testTag("game_screen")) {
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .statusBarsPadding()
          .navigationBarsPadding(),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(
        modifier =
          Modifier
            .fillMaxSize()
            .widthIn(max = 620.dp)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        // 1. Sleek Top Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          NeonIconButton(
            icon = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = stringResource(R.string.cd_back),
            onClick = onBackClick,
            accentColor = neon.primaryCyan,
            testTag = "game_back_button",
          )

          Column(
            modifier = Modifier.weight(1f).padding(horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text(
              text = uiState.config.boardSize.title.uppercase(),
              style = MaterialTheme.typography.titleMedium.withNeonGlow(neon.primaryCyan, 12f),
              color = neon.textPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text =
                if (uiState.config.matchFormat == MatchFormat.SINGLE_ROUND) {
                  uiState.config.boardSize.ruleLabel.uppercase()
                } else {
                  "ROUND ${uiState.currentRound}/${uiState.config.matchFormat.maxRounds} • ${uiState.config.boardSize.ruleLabel.uppercase()}"
                },
              style = MaterialTheme.typography.labelSmall,
              color = neon.emeraldAccent,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag("round_progress_text"),
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NeonIconButton(
              icon =
                if (soundEnabled) Icons.AutoMirrored.Rounded.VolumeUp
                else Icons.AutoMirrored.Rounded.VolumeOff,
              contentDescription = stringResource(R.string.cd_sound_toggle),
              onClick = onToggleSound,
              accentColor = if (soundEnabled) neon.primaryCyan else neon.textSecondary,
              size = 48.dp,
              testTag = "game_sound_toggle",
            )
            NeonIconButton(
              icon = Icons.Rounded.Settings,
              contentDescription = stringResource(R.string.cd_settings),
              onClick = onSettingsClick,
              accentColor = neon.emeraldAccent,
              size = 48.dp,
              testTag = "game_settings_button",
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Compact Glowing Animal Player HUD Cards
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          PlayerHudCard(
            name = uiState.player1DisplayName,
            animal = p1Animal,
            score = uiState.player1Score,
            isActive = !board.isGameOver && board.currentTurn == PlayerSlot.PLAYER_1,
            accentColor = p1Color,
            modifier = Modifier.weight(1f),
            testTag = "hud_player_1",
          )

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = neon.cardSurface.copy(alpha = 0.92f),
            border = BorderStroke(1.2.dp, neon.emeraldAccent.copy(alpha = 0.9f)),
          ) {
            Text(
              text = "VS",
              style =
                MaterialTheme.typography.labelLarge
                  .copy(fontSize = 11.sp)
                  .withNeonGlow(neon.emeraldAccent, 10f),
              color = neon.textPrimary,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            )
          }

          PlayerHudCard(
            name = uiState.player2DisplayName,
            animal = p2Animal,
            score = uiState.player2Score,
            isActive = !board.isGameOver && board.currentTurn == PlayerSlot.PLAYER_2,
            accentColor = p2Color,
            modifier = Modifier.weight(1f),
            testTag = "hud_player_2",
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Turn / Result Neon Ribbon
        val statusText =
          when (uiState.gameStatus) {
            GameStatus.IN_PROGRESS -> {
              if (uiState.isAiThinking) "${p2Animal.title.uppercase()} AI IS THINKING..."
              else if (uiState.currentPlayer == PlayerSlot.PLAYER_1)
                "${uiState.player1DisplayName.uppercase()}'S TURN (${p1Animal.title.uppercase()})"
              else
                "${uiState.player2DisplayName.uppercase()}'S TURN (${p2Animal.title.uppercase()})"
            }
            GameStatus.WON -> {
              val winnerName =
                if (uiState.winner == PlayerSlot.PLAYER_1) uiState.player1DisplayName
                else uiState.player2DisplayName
              if (uiState.isSeriesComplete && uiState.config.matchFormat != MatchFormat.SINGLE_ROUND) {
                val champ = uiState.seriesWinnerName ?: winnerName
                "${champ.uppercase()} WINS SERIES!"
              } else {
                "${winnerName.uppercase()} WINS!"
              }
            }
            GameStatus.DRAW -> "DRAW BATTLE!"
          }

        val statusColor =
          when (uiState.gameStatus) {
            GameStatus.IN_PROGRESS ->
              if (uiState.currentPlayer == PlayerSlot.PLAYER_1) p1Color else p2Color
            GameStatus.WON -> neon.winLine
            GameStatus.DRAW -> neon.emeraldAccent
          }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = statusColor.copy(alpha = 0.14f),
          border = BorderStroke(1.2.dp, statusColor.copy(alpha = 0.88f)),
          modifier = Modifier.testTag("game_status_banner"),
        ) {
          Text(
            text = statusText,
            style = MaterialTheme.typography.labelLarge.withNeonGlow(statusColor, 12f),
            color = neon.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
          )
        }

        if (uiState.recentUnlocks.isNotEmpty()) {
          Spacer(modifier = Modifier.height(6.dp))
          val firstUnlock = uiState.recentUnlocks.first()
          val rewardLabel =
            firstUnlock.rewardAnimal?.let { " • ${it.title.uppercase()} UNLOCKED!" } ?: ""
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = neon.emeraldAccent.copy(alpha = 0.18f),
            border = BorderStroke(1.dp, neon.emeraldAccent),
            modifier = Modifier.testTag("recent_unlock_banner"),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.EmojiEvents,
                contentDescription = null,
                tint = neon.winLine,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = "ACHIEVEMENT: ${firstUnlock.title.uppercase()}$rewardLabel",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = neon.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }

        // 3. Large, Perfectly Centered Glowing Animal Battle Board
        BoxWithConstraints(
          modifier =
            Modifier
              .weight(1f)
              .fillMaxWidth()
              .padding(vertical = 6.dp),
          contentAlignment = Alignment.Center,
        ) {
          val boardSide = min(maxWidth, maxHeight)
          key(uiState.boardGeneration) {
            AnimalBattleBoard(
              board = board,
              player1Animal = p1Animal,
              player2Animal = p2Animal,
              canInteract = !uiState.isAiThinking && !board.isGameOver,
              onCellClick = onCellClick,
              modifier = Modifier.size(boardSide),
            )
          }

          if (uiState.gameStatus == GameStatus.WON && uiState.winner != null) {
            val winnerAnimal =
              if (uiState.winner == PlayerSlot.PLAYER_1) p1Animal else p2Animal
            AnimalVictoryParticlesOverlay(
              winnerAnimal = winnerAnimal,
              modifier = Modifier.size(boardSide),
            )
          }
        }

        // 4. Compact Scoreboard + Action Bar
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = neon.cardSurface.copy(alpha = 0.88f),
          border = BorderStroke(1.1.dp, neon.gridLine.copy(alpha = 0.85f)),
          modifier = Modifier.fillMaxWidth().testTag("game_scoreboard"),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            ScoreStatItem(
              label = uiState.player1DisplayName,
              value = uiState.player1Score,
              color = p1Color,
              modifier = Modifier.weight(1f),
            )
            ScoreStatItem(
              label = "DRAWS",
              value = uiState.drawCount,
              color = neon.emeraldAccent,
              modifier = Modifier.weight(1f),
            )
            ScoreStatItem(
              label = uiState.player2DisplayName,
              value = uiState.player2Score,
              color = p2Color,
              modifier = Modifier.weight(1f),
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          val showNextRound =
            board.isGameOver &&
              uiState.config.matchFormat != MatchFormat.SINGLE_ROUND &&
              !uiState.isSeriesComplete

          if (showNextRound) {
            NeonButton(
              text = "NEXT ROUND",
              onClick = onNextRoundClick,
              icon = Icons.Rounded.PlayArrow,
              accentColor = neon.winLine,
              isPrimary = true,
              modifier = Modifier.weight(1f),
              testTag = "next_round_button",
            )
          } else {
            NeonButton(
              text = if (board.isGameOver) "PLAY AGAIN" else "RESTART",
              onClick = onRestartClick,
              icon = Icons.Rounded.Refresh,
              accentColor = neon.primaryCyan,
              isPrimary = board.isGameOver,
              modifier = Modifier.weight(1f),
              testTag = "restart_button",
            )
          }

          NeonButton(
            text = "HOME",
            onClick = onHomeClick,
            icon = Icons.Rounded.Home,
            accentColor = neon.emeraldAccent,
            modifier = Modifier.weight(1f),
            testTag = "game_home_button",
          )
        }
      }
    }
  }
}

@Composable
private fun PlayerHudCard(
  name: String,
  animal: AnimalType,
  score: Int,
  isActive: Boolean,
  accentColor: Color,
  modifier: Modifier = Modifier,
  testTag: String,
) {
  val neon = LocalNeonColors.current
  val pulseTransition = rememberInfiniteTransition(label = "hud_pulse")
  val borderAlpha by
    pulseTransition.animateFloat(
      initialValue = 0.62f,
      targetValue = 1.0f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 720, easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "hud_border_alpha",
    )

  val glowPaint = remember {
    Paint().apply {
      asFrameworkPaint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
      }
    }
  }

  Surface(
    modifier =
      modifier
        .testTag(testTag)
        .drawBehind {
          if (isActive) {
            val radiusPx = 15.dp.toPx()
            drawIntoCanvas { canvas ->
              val fw = glowPaint.asFrameworkPaint()
              fw.color = accentColor.copy(alpha = borderAlpha * 0.72f).toArgb()
              fw.strokeWidth = 3.5f * density
              fw.maskFilter = BlurMaskFilter(9f * density, BlurMaskFilter.Blur.NORMAL)
              canvas.nativeCanvas.drawRoundRect(
                0f,
                0f,
                size.width,
                size.height,
                radiusPx,
                radiusPx,
                fw,
              )
            }
          }
        },
    shape = RoundedCornerShape(15.dp),
    color =
      if (isActive) accentColor.copy(alpha = 0.18f)
      else neon.cardSurface.copy(alpha = 0.86f),
    border =
      BorderStroke(
        width = if (isActive) 1.9.dp else 1.dp,
        color =
          if (isActive) accentColor.copy(alpha = borderAlpha)
          else accentColor.copy(alpha = 0.35f),
      ),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      AnimatedAnimalIcon(
        animal = animal,
        modifier = Modifier.size(30.dp),
        animate = false,
      )
      Spacer(modifier = Modifier.width(8.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = name,
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = neon.textPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = "${animal.title.uppercase()} • $score",
          style = MaterialTheme.typography.labelSmall,
          color = accentColor,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun ScoreStatItem(
  label: String,
  value: Int,
  color: Color,
  modifier: Modifier = Modifier,
) {
  val neon = LocalNeonColors.current
  Row(
    modifier = modifier.padding(horizontal = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center,
  ) {
    Text(
      text = label.take(8).uppercase(),
      style = MaterialTheme.typography.labelSmall,
      color = neon.textSecondary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.weight(1f, fill = false),
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = value.toString(),
      style =
        MaterialTheme.typography.titleMedium
          .copy(fontWeight = FontWeight.Bold)
          .withNeonGlow(color, 10f),
      color = color,
      maxLines = 1,
    )
  }
}

@Composable
private fun AnimalBattleBoard(
  board: GameBoardState,
  player1Animal: AnimalType,
  player2Animal: AnimalType,
  canInteract: Boolean,
  onCellClick: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val neon = LocalNeonColors.current
  val gridSize = board.boardSize.gridSize
  val entranceScale = remember { Animatable(0.92f) }

  LaunchedEffect(Unit) {
    entranceScale.animateTo(1f, animationSpec = tween(280, easing = FastOutSlowInEasing))
  }

  val winLine = (board.result as? GameResult.Win)?.winningLine
  val winLineProgress = remember(winLine) { Animatable(0f) }
  LaunchedEffect(winLine) {
    if (winLine != null) {
      winLineProgress.snapTo(0f)
      winLineProgress.animateTo(1f, animationSpec = tween(420, easing = FastOutSlowInEasing))
    } else {
      winLineProgress.snapTo(0f)
    }
  }

  val glowPaint = remember {
    Paint().apply {
      asFrameworkPaint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
        strokeCap = android.graphics.Paint.Cap.ROUND
      }
    }
  }

  val boardCorner = 24.dp
  val strokeScale =
    when (gridSize) {
      3 -> 1.05f
      5 -> 0.92f
      7 -> 0.82f
      else -> 0.72f
    }

  Box(
    modifier =
      modifier
        .scale(entranceScale.value)
        .drawBehind {
          val rPx = boardCorner.toPx()
          drawIntoCanvas { canvas ->
            val fw = glowPaint.asFrameworkPaint()
            fw.color = neon.primaryCyan.copy(alpha = 0.74f).toArgb()
            fw.strokeWidth = 5.5f * density
            fw.maskFilter = BlurMaskFilter(15f * density, BlurMaskFilter.Blur.NORMAL)
            canvas.nativeCanvas.drawRoundRect(0f, 0f, size.width, size.height, rPx, rPx, fw)
          }
        }
        .clip(RoundedCornerShape(boardCorner))
        .background(
          brush =
            Brush.radialGradient(
              colors =
                listOf(
                  neon.secondaryBackground.copy(alpha = 0.95f),
                  neon.background.copy(alpha = 0.98f),
                )
            )
        )
        .border(
          width = 2.4.dp,
          brush =
            Brush.linearGradient(
              colors =
                listOf(
                  neon.primaryCyan,
                  neon.emeraldAccent,
                  neon.primaryCyan,
                )
            ),
          shape = RoundedCornerShape(boardCorner),
        )
        .padding(8.dp)
        .testTag("game_board"),
  ) {
    // Glowing Neon Grid Lines + Intersection Nodes
    Canvas(modifier = Modifier.fillMaxSize()) {
      drawRoundRect(
        color = neon.gridLine.copy(alpha = 0.25f),
        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
        style = Stroke(width = 2.6.dp.toPx()),
      )

      val cellW = size.width / gridSize
      val cellH = size.height / gridSize
      val lineStroke = (if (gridSize >= 7) 1.5f else 2.2f) * density

      for (i in 1 until gridSize) {
        val x = cellW * i
        drawIntoCanvas { canvas ->
          val fw = glowPaint.asFrameworkPaint()
          fw.color = neon.primaryCyan.copy(alpha = 0.45f).toArgb()
          fw.strokeWidth = lineStroke * 2.4f
          fw.maskFilter = BlurMaskFilter(5f * density, BlurMaskFilter.Blur.NORMAL)
          canvas.nativeCanvas.drawLine(x, 6f, x, size.height - 6f, fw)
        }
        drawLine(
          color = neon.gridLine,
          start = Offset(x, 6f),
          end = Offset(x, size.height - 6f),
          strokeWidth = lineStroke,
          cap = StrokeCap.Round,
        )

        val y = cellH * i
        drawIntoCanvas { canvas ->
          val fw = glowPaint.asFrameworkPaint()
          fw.color = neon.primaryCyan.copy(alpha = 0.45f).toArgb()
          fw.strokeWidth = lineStroke * 2.4f
          fw.maskFilter = BlurMaskFilter(5f * density, BlurMaskFilter.Blur.NORMAL)
          canvas.nativeCanvas.drawLine(6f, y, size.width - 6f, y, fw)
        }
        drawLine(
          color = neon.gridLine,
          start = Offset(6f, y),
          end = Offset(size.width - 6f, y),
          strokeWidth = lineStroke,
          cap = StrokeCap.Round,
        )
      }

      if (gridSize <= 7) {
        for (r in 1 until gridSize) {
          for (c in 1 until gridSize) {
            drawCircle(
              color = neon.emeraldAccent.copy(alpha = 0.58f),
              radius = 1.6f * density,
              center = Offset(cellW * c, cellH * r),
            )
          }
        }
      }
    }

    // Interactive Cell Grid
    Column(modifier = Modifier.fillMaxSize()) {
      for (row in 0 until gridSize) {
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
          for (col in 0 until gridSize) {
            val index = row * gridSize + col
            val cellSlot = board.cells[index]
            val cellAnimal =
              when (cellSlot) {
                PlayerSlot.PLAYER_1 -> player1Animal
                PlayerSlot.PLAYER_2 -> player2Animal
                null -> null
              }
            val isWinning = index in board.winningCellSet
            val isLastMove = index == board.lastMoveIndex
            val cellDesc =
              when (cellAnimal) {
                null -> "Row ${row + 1} Column ${col + 1}, Empty"
                else -> "Row ${row + 1} Column ${col + 1}, ${cellAnimal.title}"
              }

            Box(
              modifier =
                Modifier
                  .weight(1f)
                  .fillMaxSize()
                  .then(
                    if (isWinning) {
                      Modifier.background(neon.winLine.copy(alpha = 0.16f))
                    } else if (isLastMove && cellAnimal != null) {
                      Modifier.background(cellAnimal.accentColor().copy(alpha = 0.10f))
                    } else {
                      Modifier
                    }
                  )
                  .clickable(
                    enabled = canInteract && cellSlot == null,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = neon.primaryCyan),
                    onClick = { onCellClick(index) },
                  )
                  .semantics { contentDescription = cellDesc }
                  .testTag("cell_$index"),
              contentAlignment = Alignment.Center,
            ) {
              if (cellAnimal != null) {
                AnimatedAnimalIcon(
                  animal = cellAnimal,
                  modifier = Modifier.fillMaxSize(0.86f),
                  animate = true,
                  isWinningCell = isWinning,
                  strokeScale = strokeScale,
                )
              }
            }
          }
        }
      }
    }

    // Animated Golden/White Neon Winning Line
    if (winLine != null && winLineProgress.value > 0f) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val cellW = size.width / gridSize
        val cellH = size.height / gridSize
        val start =
          Offset(
            x = (winLine.startCol + 0.5f) * cellW,
            y = (winLine.startRow + 0.5f) * cellH,
          )
        val end =
          Offset(
            x = (winLine.endCol + 0.5f) * cellW,
            y = (winLine.endRow + 0.5f) * cellH,
          )
        val p = winLineProgress.value
        val currentEnd =
          Offset(
            x = start.x + (end.x - start.x) * p,
            y = start.y + (end.y - start.y) * p,
          )
        val baseStroke = (if (gridSize >= 7) 5.5f else 8.5f) * density

        drawIntoCanvas { canvas ->
          val fw = glowPaint.asFrameworkPaint()
          fw.color = neon.winLine.copy(alpha = 0.95f).toArgb()
          fw.strokeWidth = baseStroke * 2.2f
          fw.maskFilter = BlurMaskFilter(14f * density, BlurMaskFilter.Blur.NORMAL)
          canvas.nativeCanvas.drawLine(start.x, start.y, currentEnd.x, currentEnd.y, fw)
        }
        drawLine(
          color = neon.winLine,
          start = start,
          end = currentEnd,
          strokeWidth = baseStroke * 1.15f,
          cap = StrokeCap.Round,
        )
        drawLine(
          color = neon.winLineCore,
          start = start,
          end = currentEnd,
          strokeWidth = baseStroke * 0.48f,
          cap = StrokeCap.Round,
        )

        drawCircle(color = neon.winLine.copy(alpha = 0.45f), radius = baseStroke * 1.4f, center = currentEnd)
        drawCircle(color = neon.winLineCore, radius = baseStroke * 0.6f, center = currentEnd)
      }
    }
  }
}

private data class AnimalVictoryParticle(
  val angleRad: Float,
  val speedFraction: Float,
  val radiusDp: Float,
  val isPawShape: Boolean,
  val colorIndex: Int,
)

@Composable
private fun AnimalVictoryParticlesOverlay(
  winnerAnimal: AnimalType,
  modifier: Modifier = Modifier,
) {
  val neon = LocalNeonColors.current
  val progress = remember(winnerAnimal) { Animatable(0f) }

  val particles = remember {
    val rng = Random(8821)
    List(36) { idx ->
      AnimalVictoryParticle(
        angleRad = (idx.toFloat() / 36f) * (2f * PI.toFloat()) + rng.nextFloat() * 0.25f,
        speedFraction = 0.28f + rng.nextFloat() * 0.66f,
        radiusDp = 2.8f + rng.nextFloat() * 3.8f,
        isPawShape = idx % 3 == 0,
        colorIndex = idx % 3,
      )
    }
  }

  LaunchedEffect(winnerAnimal) {
    progress.snapTo(0f)
    progress.animateTo(1f, animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing))
  }

  Canvas(modifier = modifier) {
    val p = progress.value
    if (p <= 0f || p >= 1f) return@Canvas
    val center = Offset(size.width / 2f, size.height / 2f)
    val maxR = size.minDimension * 0.55f
    val alpha = (1f - p).coerceIn(0f, 1f)
    val winnerColor = winnerAnimal.accentColor()

    for (particle in particles) {
      val dist = maxR * particle.speedFraction * p
      val pos =
        Offset(
          x = center.x + cos(particle.angleRad) * dist,
          y = center.y + sin(particle.angleRad) * dist,
        )
      val color =
        when (particle.colorIndex) {
          0 -> winnerColor
          1 -> neon.winLine
          else -> neon.emeraldAccent
        }
      val rPx = particle.radiusDp * density * (1f - p * 0.30f)
      if (particle.isPawShape) {
        drawPawPrintBurst(center = pos, radius = rPx * 1.45f, color = color.copy(alpha = alpha))
      } else {
        drawCircle(color = color.copy(alpha = alpha * 0.35f), radius = rPx * 2.4f, center = pos)
        drawCircle(color = color.copy(alpha = alpha), radius = rPx, center = pos)
      }
    }
  }
}

private fun DrawScope.drawPawPrintBurst(
  center: Offset,
  radius: Float,
  color: Color,
) {
  // Main pad
  drawCircle(color = color, radius = radius * 0.68f, center = center)
  // Three toe beans arcing above the pad
  val toeR = radius * 0.32f
  drawCircle(color = color, radius = toeR, center = Offset(center.x - radius * 0.72f, center.y - radius * 0.68f))
  drawCircle(color = color, radius = toeR, center = Offset(center.x, center.y - radius * 0.96f))
  drawCircle(color = color, radius = toeR, center = Offset(center.x + radius * 0.72f, center.y - radius * 0.68f))
}
