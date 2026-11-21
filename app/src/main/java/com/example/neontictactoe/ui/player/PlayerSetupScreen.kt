package com.example.neontictactoe.ui.player

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
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.neontictactoe.domain.AIDifficulty
import com.example.neontictactoe.domain.BoardSize
import com.example.neontictactoe.domain.MatchFormat
import com.example.neontictactoe.domain.PlayerSlot
import com.example.neontictactoe.ui.MatchSetupConfig
import com.example.neontictactoe.ui.components.AnimatedAnimalIcon
import com.example.neontictactoe.ui.components.CyberpunkBackground
import com.example.neontictactoe.ui.components.NeonButton
import com.example.neontictactoe.ui.components.NeonCard
import com.example.neontictactoe.ui.components.NeonTopBar
import com.example.neontictactoe.ui.theme.LocalNeonColors
import com.example.neontictactoe.ui.theme.accentColor

@Composable
fun PlayerSetupScreen(
  config: MatchSetupConfig,
  onPlayer1NameChange: (String) -> Unit,
  onPlayer2NameChange: (String) -> Unit,
  onToggleAiOpponent: (Boolean) -> Unit,
  onSelectAnimalForSlot: (PlayerSlot) -> Unit,
  onDifficultySelected: (AIDifficulty) -> Unit,
  onBoardSizeSelected: (BoardSize) -> Unit,
  onMatchFormatSelected: (MatchFormat) -> Unit,
  onStartMatchClick: () -> Unit,
  onBackClick: () -> Unit,
) {
  val neon = LocalNeonColors.current
  val p1Color = config.player1Animal.accentColor()
  val p2Color = config.player2Animal.accentColor()

  CyberpunkBackground(
    modifier = Modifier.testTag("player_setup_screen"),
    showRetroFloor = false,
  ) {
    Box(
      modifier = Modifier.fillMaxSize().navigationBarsPadding(),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(modifier = Modifier.fillMaxSize().widthIn(max = 600.dp)) {
        NeonTopBar(
          title = if (config.isAiOpponent) "YOU VS AI" else "ANIMAL SETUP",
          onBackClick = onBackClick,
        )

        Column(
          modifier =
            Modifier
              .weight(1f)
              .verticalScroll(rememberScrollState())
              .padding(horizontal = 20.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          // Opponent mode toggle (VS AI vs 2 Players)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            OpponentChip(
              label = "VS AI",
              selected = config.isAiOpponent,
              accent = neon.primaryCyan,
              onClick = { onToggleAiOpponent(true) },
              modifier = Modifier.weight(1f),
              testTag = "toggle_vs_ai",
            )
            OpponentChip(
              label = "2 PLAYERS",
              selected = !config.isAiOpponent,
              accent = neon.redO,
              onClick = { onToggleAiOpponent(false) },
              modifier = Modifier.weight(1f),
              testTag = "toggle_2_players",
            )
          }

          // Player 1 Card
          NeonCard(
            accentColor = p1Color,
            isSelected = true,
            testTag = "player1_card",
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = p1Color.copy(alpha = 0.14f),
                border = BorderStroke(1.2.dp, p1Color.copy(alpha = 0.75f)),
                onClick = { onSelectAnimalForSlot(PlayerSlot.PLAYER_1) },
                modifier = Modifier.testTag("setup_p1_animal_button"),
              ) {
                Column(
                  modifier = Modifier.padding(6.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  AnimatedAnimalIcon(
                    animal = config.player1Animal,
                    modifier = Modifier.size(46.dp),
                    animate = false,
                  )
                  Text(
                    text = "SWAP",
                    style = MaterialTheme.typography.labelSmall,
                    color = p1Color,
                  )
                }
              }

              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text =
                    if (config.isAiOpponent) "YOU • ${config.player1Animal.title.uppercase()}"
                    else "PLAYER 1 • ${config.player1Animal.title.uppercase()}",
                  style = MaterialTheme.typography.labelLarge,
                  color = p1Color,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                  value = config.player1Name,
                  onValueChange = onPlayer1NameChange,
                  singleLine = true,
                  placeholder = { Text("Enter Player 1 name") },
                  colors =
                    OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = p1Color,
                      unfocusedBorderColor = p1Color.copy(alpha = 0.5f),
                      focusedTextColor = neon.textPrimary,
                      unfocusedTextColor = neon.textPrimary,
                      cursorColor = p1Color,
                    ),
                  modifier = Modifier.fillMaxWidth().testTag("player1_name_input"),
                )
              }
            }
          }

          // VS Label + Quick Change Animals button
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = neon.cardSurface,
              border = BorderStroke(1.2.dp, neon.emeraldAccent),
            ) {
              Text(
                text = "VS",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = neon.emeraldAccent,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
              )
            }
          }

          // Player 2 or AI Card
          NeonCard(
            accentColor = p2Color,
            isSelected = true,
            testTag = "player2_card",
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = p2Color.copy(alpha = 0.14f),
                border = BorderStroke(1.2.dp, p2Color.copy(alpha = 0.75f)),
                onClick = { onSelectAnimalForSlot(PlayerSlot.PLAYER_2) },
                modifier = Modifier.testTag("setup_p2_animal_button"),
              ) {
                Column(
                  modifier = Modifier.padding(6.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  AnimatedAnimalIcon(
                    animal = config.player2Animal,
                    modifier = Modifier.size(46.dp),
                    animate = false,
                  )
                  Text(
                    text = "SWAP",
                    style = MaterialTheme.typography.labelSmall,
                    color = p2Color,
                  )
                }
              }

              Spacer(modifier = Modifier.width(12.dp))
              if (config.isAiOpponent) {
                Column(modifier = Modifier.weight(1f)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Rounded.SmartToy,
                      contentDescription = null,
                      tint = p2Color,
                      modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "${config.player2Animal.title.uppercase()} AI",
                      style = MaterialTheme.typography.labelLarge,
                      color = p2Color,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "Difficulty: ${config.difficulty.title} — ${config.difficulty.description}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = neon.textSecondary,
                  )
                }
              } else {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "PLAYER 2 • ${config.player2Animal.title.uppercase()}",
                    style = MaterialTheme.typography.labelLarge,
                    color = p2Color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Spacer(modifier = Modifier.height(6.dp))
                  OutlinedTextField(
                    value = config.player2Name,
                    onValueChange = onPlayer2NameChange,
                    singleLine = true,
                    placeholder = { Text("Enter Player 2 name") },
                    colors =
                      OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = p2Color,
                        unfocusedBorderColor = p2Color.copy(alpha = 0.5f),
                        focusedTextColor = neon.textPrimary,
                        unfocusedTextColor = neon.textPrimary,
                        cursorColor = p2Color,
                      ),
                    modifier = Modifier.fillMaxWidth().testTag("player2_name_input"),
                  )
                }
              }
            }
          }

          NeonButton(
            text = "CHANGE ANIMALS",
            onClick = { onSelectAnimalForSlot(PlayerSlot.PLAYER_1) },
            icon = Icons.Rounded.Pets,
            accentColor = neon.emeraldAccent,
            modifier = Modifier.fillMaxWidth(),
            testTag = "setup_change_animals_button",
          )

          // AI Difficulty Selection
          if (config.isAiOpponent) {
            Text(
              text = "AI DIFFICULTY",
              style = MaterialTheme.typography.labelLarge,
              color = neon.primaryCyan,
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              AIDifficulty.entries.forEach { diff ->
                val selected = config.difficulty == diff
                Surface(
                  modifier =
                    Modifier.weight(1f).testTag("difficulty_${diff.name.lowercase()}"),
                  shape = RoundedCornerShape(12.dp),
                  color =
                    if (selected) neon.redO.copy(alpha = 0.24f)
                    else neon.cardSurface.copy(alpha = 0.85f),
                  border =
                    BorderStroke(
                      width = if (selected) 1.8.dp else 1.dp,
                      color = if (selected) neon.redO else neon.gridLine.copy(alpha = 0.65f),
                    ),
                  onClick = { onDifficultySelected(diff) },
                ) {
                  Box(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center,
                  ) {
                    Text(
                      text = diff.title,
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = if (selected) neon.textPrimary else neon.textSecondary,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                  }
                }
              }
            }
          }

          // Match Format (Single / Best of 3 / Best of 5)
          Text(
            text = "MATCH FORMAT",
            style = MaterialTheme.typography.labelLarge,
            color = neon.emeraldAccent,
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            MatchFormat.entries.forEach { format ->
              val selected = config.matchFormat == format
              Surface(
                modifier =
                  Modifier.weight(1f).testTag("format_${format.name.lowercase()}"),
                shape = RoundedCornerShape(12.dp),
                color =
                  if (selected) neon.emeraldAccent.copy(alpha = 0.22f)
                  else neon.cardSurface.copy(alpha = 0.85f),
                border =
                  BorderStroke(
                    width = if (selected) 1.8.dp else 1.dp,
                    color = if (selected) neon.emeraldAccent else neon.gridLine.copy(alpha = 0.65f),
                  ),
                onClick = { onMatchFormatSelected(format) },
              ) {
                Box(
                  modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                  contentAlignment = Alignment.Center,
                ) {
                  Text(
                    text = format.title,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (selected) neon.textPrimary else neon.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
              }
            }
          }

          // Board Size Selector
          Text(
            text = "BOARD SIZE",
            style = MaterialTheme.typography.labelLarge,
            color = neon.primaryCyan,
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            BoardSize.entries.forEach { size ->
              val selected = config.boardSize == size
              Surface(
                modifier =
                  Modifier.weight(1f).testTag("setup_board_${size.gridSize}x${size.gridSize}"),
                shape = RoundedCornerShape(12.dp),
                color =
                  if (selected) neon.primaryCyan.copy(alpha = 0.22f)
                  else neon.cardSurface.copy(alpha = 0.85f),
                border =
                  BorderStroke(
                    width = if (selected) 1.8.dp else 1.dp,
                    color = if (selected) neon.primaryCyan else neon.gridLine.copy(alpha = 0.65f),
                  ),
                onClick = { onBoardSizeSelected(size) },
              ) {
                Column(
                  modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  Text(
                    text = "${size.gridSize}×${size.gridSize}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (selected) neon.textPrimary else neon.textSecondary,
                    maxLines = 1,
                  )
                  Text(
                    text = "${size.winLength} in row",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) neon.primaryCyan else neon.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(6.dp))
        }

        Box(
          modifier =
            Modifier
              .fillMaxWidth()
              .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
          NeonButton(
            text = "START BATTLE",
            onClick = onStartMatchClick,
            icon = Icons.Rounded.PlayArrow,
            accentColor = neon.primaryCyan,
            isPrimary = true,
            modifier = Modifier.fillMaxWidth(),
            testTag = "start_match_button",
          )
        }
      }
    }
  }
}

@Composable
private fun OpponentChip(
  label: String,
  selected: Boolean,
  accent: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String,
) {
  val neon = LocalNeonColors.current
  Surface(
    modifier = modifier.testTag(testTag),
    shape = RoundedCornerShape(12.dp),
    color = if (selected) accent.copy(alpha = 0.22f) else neon.cardSurface.copy(alpha = 0.85f),
    border = BorderStroke(if (selected) 1.8.dp else 1.dp, if (selected) accent else neon.gridLine),
    onClick = onClick,
  ) {
    Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) neon.textPrimary else neon.textSecondary,
      )
    }
  }
}
