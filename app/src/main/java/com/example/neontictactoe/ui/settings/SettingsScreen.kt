package com.example.neontictactoe.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.neontictactoe.data.UserPreferences
import com.example.neontictactoe.domain.AIDifficulty
import com.example.neontictactoe.domain.BoardSize
import com.example.neontictactoe.domain.NeonThemeStyle
import com.example.neontictactoe.ui.components.CyberpunkBackground
import com.example.neontictactoe.ui.components.NeonButton
import com.example.neontictactoe.ui.components.NeonCard
import com.example.neontictactoe.ui.components.NeonTopBar
import com.example.neontictactoe.ui.theme.LocalNeonColors
import com.example.neontictactoe.ui.theme.paletteForTheme

@Composable
fun SettingsScreen(
  preferences: UserPreferences,
  onSoundToggle: (Boolean) -> Unit,
  onVibrationToggle: (Boolean) -> Unit,
  onThemeSelected: (NeonThemeStyle) -> Unit,
  onPlayer1NameChange: (String) -> Unit,
  onPlayer2NameChange: (String) -> Unit,
  onDifficultySelected: (AIDifficulty) -> Unit,
  onBoardSizeSelected: (BoardSize) -> Unit,
  onResetStatistics: () -> Unit,
  onBackClick: () -> Unit,
) {
  val neon = LocalNeonColors.current
  var p1Draft by remember { mutableStateOf(preferences.player1Name) }
  var p2Draft by remember { mutableStateOf(preferences.player2Name) }
  var p1Edited by remember { mutableStateOf(false) }
  var p2Edited by remember { mutableStateOf(false) }
  if (!p1Edited && p1Draft != preferences.player1Name) {
    p1Draft = preferences.player1Name
  }
  if (!p2Edited && p2Draft != preferences.player2Name) {
    p2Draft = preferences.player2Name
  }
  var showResetDialog by remember { mutableStateOf(false) }
  var showAboutDialog by remember { mutableStateOf(false) }
  var showPrivacyDialog by remember { mutableStateOf(false) }

  CyberpunkBackground(
    modifier = Modifier.testTag("settings_screen"),
    showRetroFloor = false,
  ) {
    Box(
      modifier = Modifier.fillMaxSize().navigationBarsPadding(),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(modifier = Modifier.fillMaxSize().widthIn(max = 600.dp)) {
        NeonTopBar(
          title = "SETTINGS",
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
          // Audio & Haptics
          NeonCard(accentColor = neon.primaryCyan) {
            Text(
              text = "AUDIO & HAPTICS",
              style = MaterialTheme.typography.labelLarge,
              color = neon.primaryCyan,
            )
            Spacer(modifier = Modifier.height(10.dp))
            SettingsSwitchRow(
              title = "Sound Effects",
              subtitle = "Synth tones for animal moves, wins & buttons",
              checked = preferences.soundEnabled,
              onCheckedChange = onSoundToggle,
              testTag = "settings_sound_switch",
            )
            Spacer(modifier = Modifier.height(8.dp))
            SettingsSwitchRow(
              title = "Vibration Feedback",
              subtitle = "Tactile haptic pulse on grid taps",
              checked = preferences.vibrationEnabled,
              onCheckedChange = onVibrationToggle,
              testTag = "settings_vibration_switch",
            )
          }

          // Theme Selection
          NeonCard(accentColor = neon.emeraldAccent) {
            Text(
              text = "JUNGLE ARENA THEME",
              style = MaterialTheme.typography.labelLarge,
              color = neon.emeraldAccent,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              NeonThemeStyle.entries.forEach { style ->
                val preview = paletteForTheme(style)
                val selected = preferences.themeStyle == style
                Surface(
                  modifier =
                    Modifier
                      .fillMaxWidth()
                      .testTag("theme_option_${style.name.lowercase()}"),
                  shape = RoundedCornerShape(12.dp),
                  color =
                    if (selected) preview.primaryCyan.copy(alpha = 0.22f)
                    else neon.secondaryBackground.copy(alpha = 0.85f),
                  border =
                    BorderStroke(
                      width = if (selected) 1.8.dp else 1.dp,
                      color = if (selected) preview.primaryCyan else neon.gridLine.copy(alpha = 0.6f),
                    ),
                  onClick = { onThemeSelected(style) },
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                  ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                      Text(
                        text = style.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = neon.textPrimary,
                      )
                      Text(
                        text = style.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = neon.textSecondary,
                      )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                      Box(
                        modifier =
                          Modifier.size(14.dp).clip(CircleShape).background(preview.primaryCyan)
                      )
                      Box(
                        modifier =
                          Modifier.size(14.dp).clip(CircleShape).background(preview.emeraldAccent)
                      )
                      Box(
                        modifier = Modifier.size(14.dp).clip(CircleShape).background(preview.redO)
                      )
                    }
                  }
                }
              }
            }
          }

          // Player Names
          NeonCard(accentColor = neon.blueX) {
            Text(
              text = "PLAYER NAMES",
              style = MaterialTheme.typography.labelLarge,
              color = neon.blueX,
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
              value = p1Draft,
              onValueChange = {
                p1Edited = true
                p1Draft = it.take(16)
                if (p1Draft.isNotBlank()) {
                  onPlayer1NameChange(p1Draft)
                }
              },
              label = { Text("Player 1 Name (${preferences.player1Animal.title})") },
              singleLine = true,
              colors =
                OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = neon.blueX,
                  unfocusedBorderColor = neon.blueX.copy(alpha = 0.5f),
                  focusedTextColor = neon.textPrimary,
                  unfocusedTextColor = neon.textPrimary,
                ),
              modifier = Modifier.fillMaxWidth().testTag("settings_p1_name"),
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
              value = p2Draft,
              onValueChange = {
                p2Edited = true
                p2Draft = it.take(16)
                if (p2Draft.isNotBlank()) {
                  onPlayer2NameChange(p2Draft)
                }
              },
              label = { Text("Player 2 Name (${preferences.player2Animal.title})") },
              singleLine = true,
              colors =
                OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = neon.redO,
                  unfocusedBorderColor = neon.redO.copy(alpha = 0.5f),
                  focusedTextColor = neon.textPrimary,
                  unfocusedTextColor = neon.textPrimary,
                ),
              modifier = Modifier.fillMaxWidth().testTag("settings_p2_name"),
            )
          }

          // Default Difficulty & Board Size
          NeonCard(accentColor = neon.redO) {
            Text(
              text = "DEFAULT AI DIFFICULTY",
              style = MaterialTheme.typography.labelLarge,
              color = neon.redO,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              AIDifficulty.entries.forEach { diff ->
                val selected = preferences.defaultDifficulty == diff
                Surface(
                  modifier =
                    Modifier.weight(1f).testTag("settings_diff_${diff.name.lowercase()}"),
                  shape = RoundedCornerShape(10.dp),
                  color =
                    if (selected) neon.redO.copy(alpha = 0.24f)
                    else neon.secondaryBackground.copy(alpha = 0.8f),
                  border =
                    BorderStroke(
                      width = if (selected) 1.8.dp else 1.dp,
                      color = if (selected) neon.redO else neon.gridLine.copy(alpha = 0.5f),
                    ),
                  onClick = { onDifficultySelected(diff) },
                ) {
                  Box(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
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

            Spacer(modifier = Modifier.height(16.dp))

            Text(
              text = "DEFAULT BOARD SIZE",
              style = MaterialTheme.typography.labelLarge,
              color = neon.primaryCyan,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              BoardSize.entries.forEach { size ->
                val selected = preferences.defaultBoardSize == size
                Surface(
                  modifier =
                    Modifier
                      .weight(1f)
                      .testTag("settings_board_${size.gridSize}x${size.gridSize}"),
                  shape = RoundedCornerShape(10.dp),
                  color =
                    if (selected) neon.primaryCyan.copy(alpha = 0.24f)
                    else neon.secondaryBackground.copy(alpha = 0.8f),
                  border =
                    BorderStroke(
                      width = if (selected) 1.8.dp else 1.dp,
                      color = if (selected) neon.primaryCyan else neon.gridLine.copy(alpha = 0.5f),
                    ),
                  onClick = { onBoardSizeSelected(size) },
                ) {
                  Box(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center,
                  ) {
                    Text(
                      text = "${size.gridSize}×${size.gridSize}",
                      style = MaterialTheme.typography.labelLarge,
                      color = if (selected) neon.textPrimary else neon.textSecondary,
                      maxLines = 1,
                    )
                  }
                }
              }
            }
          }

          // Reset & Legal Info
          NeonButton(
            text = "RESET STATISTICS",
            onClick = { showResetDialog = true },
            icon = Icons.Rounded.DeleteOutline,
            accentColor = neon.redO,
            modifier = Modifier.fillMaxWidth(),
            testTag = "settings_reset_stats_button",
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            NeonButton(
              text = "ABOUT",
              onClick = { showAboutDialog = true },
              icon = Icons.Rounded.Info,
              accentColor = neon.primaryCyan,
              modifier = Modifier.weight(1f),
              testTag = "about_button",
            )
            NeonButton(
              text = "PRIVACY",
              onClick = { showPrivacyDialog = true },
              icon = Icons.Rounded.Policy,
              accentColor = neon.emeraldAccent,
              modifier = Modifier.weight(1f),
              testTag = "privacy_button",
            )
          }

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
          "This will clear all recorded wins, losses, draws, winning streaks, and unlocked achievements.",
          style = MaterialTheme.typography.bodyMedium,
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            showResetDialog = false
            onResetStatistics()
          },
          modifier = Modifier.testTag("settings_confirm_reset_button"),
        ) {
          Text("RESET", color = neon.redO, style = MaterialTheme.typography.labelLarge)
        }
      },
      dismissButton = {
        TextButton(
          onClick = { showResetDialog = false },
          modifier = Modifier.testTag("settings_cancel_reset_button"),
        ) {
          Text("CANCEL", color = neon.textSecondary, style = MaterialTheme.typography.labelLarge)
        }
      },
    )
  }

  if (showAboutDialog) {
    AlertDialog(
      onDismissRequest = { showAboutDialog = false },
      containerColor = neon.secondaryBackground,
      titleContentColor = neon.textPrimary,
      textContentColor = neon.textSecondary,
      title = {
        Text(
          stringResource(R.string.about_title),
          style = MaterialTheme.typography.titleLarge,
        )
      },
      text = {
        Column {
          Text(
            stringResource(R.string.about_body),
            style = MaterialTheme.typography.bodyMedium,
          )
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            "Version 1.0 • Offline Edition",
            style = MaterialTheme.typography.labelSmall,
            color = neon.emeraldAccent,
          )
        }
      },
      confirmButton = {
        TextButton(
          onClick = { showAboutDialog = false },
          modifier = Modifier.testTag("about_close_button"),
        ) {
          Text("CLOSE", color = neon.primaryCyan, style = MaterialTheme.typography.labelLarge)
        }
      },
    )
  }

  if (showPrivacyDialog) {
    AlertDialog(
      onDismissRequest = { showPrivacyDialog = false },
      containerColor = neon.secondaryBackground,
      titleContentColor = neon.textPrimary,
      textContentColor = neon.textSecondary,
      title = {
        Text(
          stringResource(R.string.privacy_policy_title),
          style = MaterialTheme.typography.titleLarge,
        )
      },
      text = {
        Text(
          stringResource(R.string.privacy_policy_body),
          style = MaterialTheme.typography.bodyMedium,
        )
      },
      confirmButton = {
        TextButton(
          onClick = { showPrivacyDialog = false },
          modifier = Modifier.testTag("privacy_ok_button"),
        ) {
          Text("OK", color = neon.primaryCyan, style = MaterialTheme.typography.labelLarge)
        }
      },
    )
  }
}

@Composable
private fun SettingsSwitchRow(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  testTag: String,
) {
  val neon = LocalNeonColors.current
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = neon.textPrimary,
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = neon.textSecondary,
      )
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors =
        SwitchDefaults.colors(
          checkedThumbColor = neon.textPrimary,
          checkedTrackColor = neon.primaryCyan,
          uncheckedThumbColor = neon.textSecondary,
          uncheckedTrackColor = neon.secondaryBackground,
          uncheckedBorderColor = neon.gridLine,
        ),
      modifier = Modifier.testTag(testTag),
    )
  }
}
