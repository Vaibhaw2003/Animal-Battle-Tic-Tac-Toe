package com.example.neontictactoe.ui.mode

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Grid3x3
import androidx.compose.material.icons.rounded.Grid4x4
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.neontictactoe.domain.BoardSize
import com.example.neontictactoe.domain.PlayMode
import com.example.neontictactoe.ui.components.CyberpunkBackground
import com.example.neontictactoe.ui.components.NeonButton
import com.example.neontictactoe.ui.components.NeonCard
import com.example.neontictactoe.ui.components.NeonTopBar
import com.example.neontictactoe.ui.theme.LocalNeonColors

@Composable
fun GameModeScreen(
  selectedMode: PlayMode,
  selectedBoardSize: BoardSize,
  onSelectMode: (PlayMode) -> Unit,
  onSelectBoardSize: (BoardSize) -> Unit,
  onContinueClick: () -> Unit,
  onBackClick: () -> Unit,
) {
  val neon = LocalNeonColors.current

  CyberpunkBackground(
    modifier = Modifier.testTag("game_mode_screen"),
    showRetroFloor = false,
  ) {
    Box(
      modifier = Modifier.fillMaxSize().navigationBarsPadding(),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(
        modifier = Modifier.fillMaxSize().widthIn(max = 600.dp),
      ) {
        NeonTopBar(
          title = "GAME MODES",
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
          Text(
            text = "SELECT BATTLE MODE",
            style = MaterialTheme.typography.labelLarge,
            color = neon.primaryCyan,
          )

          PlayMode.entries.forEach { mode ->
            val accent = modeAccentColor(mode)
            val isSelected = selectedMode == mode
            NeonCard(
              accentColor = accent,
              isSelected = isSelected,
              onClick = { onSelectMode(mode) },
              testTag = "mode_card_${mode.name.lowercase()}",
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
              ) {
                Icon(
                  imageVector = modeIcon(mode),
                  contentDescription = null,
                  tint = accent,
                  modifier = Modifier.size(28.dp),
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = mode.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = neon.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = mode.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = neon.textSecondary,
                  )
                }
                if (isSelected) {
                  Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Selected",
                    tint = accent,
                    modifier = Modifier.size(24.dp),
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "BOARD SIZE & WIN RULE",
            style = MaterialTheme.typography.labelLarge,
            color = neon.emeraldAccent,
          )

          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            BoardSize.entries.chunked(2).forEach { pair ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                pair.forEach { size ->
                  val isSelected = selectedBoardSize == size
                  Surface(
                    modifier =
                      Modifier
                        .weight(1f)
                        .testTag("board_size_${size.gridSize}x${size.gridSize}"),
                    shape = RoundedCornerShape(14.dp),
                    color =
                      if (isSelected) neon.primaryCyan.copy(alpha = 0.20f)
                      else neon.cardSurface.copy(alpha = 0.85f),
                    border =
                      BorderStroke(
                        width = if (isSelected) 2.dp else 1.1.dp,
                        color = if (isSelected) neon.primaryCyan else neon.gridLine.copy(alpha = 0.75f),
                      ),
                    onClick = { onSelectBoardSize(size) },
                  ) {
                    Column(
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                      horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                      Text(
                        text = size.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = neon.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                      )
                      Spacer(modifier = Modifier.height(4.dp))
                      Text(
                        text = size.ruleLabel.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) neon.emeraldAccent else neon.textSecondary,
                        maxLines = 1,
                      )
                    }
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
        }

        Box(
          modifier =
            Modifier
              .fillMaxWidth()
              .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
          NeonButton(
            text = "CONTINUE TO SETUP",
            onClick = onContinueClick,
            accentColor = neon.primaryCyan,
            isPrimary = true,
            modifier = Modifier.fillMaxWidth(),
            testTag = "mode_continue_button",
          )
        }
      }
    }
  }
}

@Composable
private fun modeAccentColor(mode: PlayMode): Color {
  val neon = LocalNeonColors.current
  return when (mode) {
    PlayMode.LION_VS_WOLF_TWO_PLAYER -> neon.redO
    PlayMode.LION_VS_AI -> neon.primaryCyan
    PlayMode.ANIMAL_BATTLE_MULTIPLAYER -> neon.emeraldAccent
    PlayMode.CLASSIC_3X3 -> neon.blueX
    PlayMode.MEDIUM_5X5 -> neon.emeraldAccent
    PlayMode.LARGE_7X7 -> neon.purpleNeon
    PlayMode.EXPERT_9X9 -> neon.redO
  }
}

private fun modeIcon(mode: PlayMode): ImageVector =
  when (mode) {
    PlayMode.LION_VS_WOLF_TWO_PLAYER -> Icons.Rounded.Pets
    PlayMode.LION_VS_AI -> Icons.Rounded.SmartToy
    PlayMode.ANIMAL_BATTLE_MULTIPLAYER -> Icons.Rounded.Groups
    PlayMode.CLASSIC_3X3 -> Icons.Rounded.Grid3x3
    PlayMode.MEDIUM_5X5 -> Icons.Rounded.GridOn
    PlayMode.LARGE_7X7 -> Icons.Rounded.Grid4x4
    PlayMode.EXPERT_9X9 -> Icons.Rounded.Extension
  }
