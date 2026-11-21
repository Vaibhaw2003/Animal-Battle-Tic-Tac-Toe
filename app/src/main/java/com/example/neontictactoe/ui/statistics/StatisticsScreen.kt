package com.example.neontictactoe.ui.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.neontictactoe.data.GameStatistics
import com.example.neontictactoe.domain.AchievementId
import com.example.neontictactoe.domain.AnimalType
import com.example.neontictactoe.ui.components.CyberpunkBackground
import com.example.neontictactoe.ui.components.NeonButton
import com.example.neontictactoe.ui.components.NeonCard
import com.example.neontictactoe.ui.components.NeonTopBar
import com.example.neontictactoe.ui.theme.LocalNeonColors
import java.util.Locale

@Composable
fun StatisticsScreen(
  stats: GameStatistics,
  onResetStatistics: () -> Unit,
  onBackClick: () -> Unit,
) {
  val neon = LocalNeonColors.current
  var showResetDialog by remember { mutableStateOf(false) }

  CyberpunkBackground(
    modifier = Modifier.testTag("statistics_screen"),
    showRetroFloor = false,
  ) {
    Box(
      modifier = Modifier.fillMaxSize().navigationBarsPadding(),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(modifier = Modifier.fillMaxSize().widthIn(max = 600.dp)) {
        NeonTopBar(
          title = "STATISTICS",
          onBackClick = onBackClick,
        )

        Column(
          modifier =
            Modifier
              .weight(1f)
              .verticalScroll(rememberScrollState())
              .padding(horizontal = 20.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          // Win Rate Hero Card
          NeonCard(
            accentColor = neon.primaryCyan,
            isSelected = true,
            testTag = "stats_win_rate_card",
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "WIN PERCENTAGE",
                  style = MaterialTheme.typography.labelLarge,
                  color = neon.primaryCyan,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = String.format(Locale.US, "%.1f%%", stats.winPercentage),
                  style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                  color = neon.textPrimary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text =
                    if (stats.totalGamesPlayed == 0) "Play a match to start tracking stats!"
                    else "${stats.totalWins} wins across ${stats.totalGamesPlayed} matches",
                  style = MaterialTheme.typography.bodyMedium,
                  color = neon.textSecondary,
                )
              }

              Box(
                modifier = Modifier.size(86.dp),
                contentAlignment = Alignment.Center,
              ) {
                val trackColor = neon.gridLine.copy(alpha = 0.35f)
                val arcColor = neon.emeraldAccent
                val sweep = (stats.winPercentage / 100f).coerceIn(0f, 1f) * 360f
                Canvas(modifier = Modifier.fillMaxSize()) {
                  val strokeW = 9.dp.toPx()
                  drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round),
                  )
                  if (sweep > 0f) {
                    drawArc(
                      color = arcColor,
                      startAngle = -90f,
                      sweepAngle = sweep,
                      useCenter = false,
                      style = Stroke(width = strokeW, cap = StrokeCap.Round),
                    )
                  }
                }
                Text(
                  text = "${stats.winPercentage.toInt()}%",
                  style = MaterialTheme.typography.labelLarge,
                  color = neon.textPrimary,
                )
              }
            }
          }

          // Streak Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            StreakCard(
              title = "CURRENT STREAK",
              value = stats.currentWinningStreak,
              accent = neon.primaryCyan,
              icon = {
                Icon(
                  imageVector = Icons.Rounded.LocalFireDepartment,
                  contentDescription = null,
                  tint = neon.primaryCyan,
                )
              },
              modifier = Modifier.weight(1f),
              testTag = "stat_current_streak",
            )
            StreakCard(
              title = "BEST STREAK",
              value = stats.bestWinningStreak,
              accent = neon.winLine,
              icon = {
                Icon(
                  imageVector = Icons.Rounded.EmojiEvents,
                  contentDescription = null,
                  tint = neon.winLine,
                )
              },
              modifier = Modifier.weight(1f),
              testTag = "stat_best_streak",
            )
          }

          // Match Breakdown Grid
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            StatTile(
              label = "TOTAL GAMES",
              value = stats.totalGamesPlayed,
              accent = neon.primaryCyan,
              modifier = Modifier.weight(1f),
              testTag = "stat_total_games",
            )
            StatTile(
              label = "TOTAL WINS",
              value = stats.totalWins,
              accent = neon.emeraldAccent,
              modifier = Modifier.weight(1f),
              testTag = "stat_total_wins",
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            StatTile(
              label = "TOTAL LOSSES",
              value = stats.totalLosses,
              accent = neon.redO,
              modifier = Modifier.weight(1f),
              testTag = "stat_total_losses",
            )
            StatTile(
              label = "TOTAL DRAWS",
              value = stats.totalDraws,
              accent = neon.textSecondary,
              modifier = Modifier.weight(1f),
              testTag = "stat_total_draws",
            )
          }

          // Player, AI & Unlock Breakdown
          NeonCard(accentColor = neon.gridLine) {
            Text(
              text = "ARENA BREAKDOWN",
              style = MaterialTheme.typography.labelLarge,
              color = neon.emeraldAccent,
            )
            Spacer(modifier = Modifier.height(10.dp))
            BreakdownRow("Player 1 Wins", stats.player1Wins.toString(), neon.blueX)
            Spacer(modifier = Modifier.height(8.dp))
            BreakdownRow("Player 2 Wins", stats.player2Wins.toString(), neon.redO)
            Spacer(modifier = Modifier.height(8.dp))
            BreakdownRow("AI Wins", stats.aiWins.toString(), neon.purpleNeon)
            Spacer(modifier = Modifier.height(8.dp))
            BreakdownRow(
              "Unlocked Animals",
              "${stats.unlockedAnimals.size} / ${AnimalType.entries.size}",
              neon.emeraldAccent,
            )
            Spacer(modifier = Modifier.height(8.dp))
            BreakdownRow(
              "Achievements",
              "${stats.unlockedAchievements.size} / ${AchievementId.entries.size}",
              neon.winLine,
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          NeonButton(
            text = "RESET STATISTICS",
            onClick = { showResetDialog = true },
            icon = Icons.Rounded.DeleteOutline,
            accentColor = neon.redO,
            modifier = Modifier.fillMaxWidth(),
            testTag = "reset_stats_button",
          )

          Spacer(modifier = Modifier.height(20.dp))
        }
      }
    }
  }

  if (showResetDialog) {
    AlertDialog(
      onDismissRequest = { showResetDialog = false },
      containerColor = neon.secondaryBackground,
      titleContentColor = neon.textPrimary,
      textContentColor = neon.textSecondary,
      title = { Text("Reset Statistics?", style = MaterialTheme.typography.titleLarge) },
      text = {
        Text(
          "This will permanently clear all recorded games, wins, losses, draws, winning streaks, and unlocked achievements.",
          style = MaterialTheme.typography.bodyMedium,
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            showResetDialog = false
            onResetStatistics()
          },
          modifier = Modifier.testTag("stats_confirm_reset_button"),
        ) {
          Text("RESET", color = neon.redO, style = MaterialTheme.typography.labelLarge)
        }
      },
      dismissButton = {
        TextButton(
          onClick = { showResetDialog = false },
          modifier = Modifier.testTag("stats_cancel_reset_button"),
        ) {
          Text("CANCEL", color = neon.textSecondary, style = MaterialTheme.typography.labelLarge)
        }
      },
    )
  }
}

@Composable
private fun StreakCard(
  title: String,
  value: Int,
  accent: Color,
  icon: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String,
) {
  val neon = LocalNeonColors.current
  NeonCard(
    accentColor = accent,
    modifier = modifier,
    testTag = testTag,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f).padding(end = 6.dp)) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelSmall,
          color = neon.textSecondary,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = value.toString(),
          style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
          color = accent,
          maxLines = 1,
        )
      }
      icon()
    }
  }
}

@Composable
private fun StatTile(
  label: String,
  value: Int,
  accent: Color,
  modifier: Modifier = Modifier,
  testTag: String,
) {
  val neon = LocalNeonColors.current
  NeonCard(
    accentColor = accent,
    modifier = modifier,
    testTag = testTag,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = neon.textSecondary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = value.toString(),
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
      color = neon.textPrimary,
      maxLines = 1,
    )
  }
}

@Composable
private fun BreakdownRow(
  label: String,
  valueText: String,
  color: Color,
) {
  val neon = LocalNeonColors.current
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyLarge,
      color = neon.textPrimary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.weight(1f).padding(end = 8.dp),
    )
    Text(
      text = valueText,
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      color = color,
      maxLines = 1,
    )
  }
}
