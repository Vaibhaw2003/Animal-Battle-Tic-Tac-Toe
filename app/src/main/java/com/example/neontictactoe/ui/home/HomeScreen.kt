package com.example.neontictactoe.ui.home

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.neontictactoe.domain.AnimalType
import com.example.neontictactoe.ui.components.AnimalBattleLogoBadge
import com.example.neontictactoe.ui.components.AnimatedAnimalIcon
import com.example.neontictactoe.ui.components.CyberpunkBackground
import com.example.neontictactoe.ui.components.NeonButton
import com.example.neontictactoe.ui.components.NeonIconButton
import com.example.neontictactoe.ui.theme.LocalNeonColors
import com.example.neontictactoe.ui.theme.accentColor
import com.example.neontictactoe.ui.theme.withNeonGlow

@Composable
fun HomeScreen(
  soundEnabled: Boolean,
  player1Animal: AnimalType,
  player2Animal: AnimalType,
  onToggleSound: () -> Unit,
  onPlayGameClick: () -> Unit,
  onPlayVsAiClick: () -> Unit,
  onTwoPlayerClick: () -> Unit,
  onGameModesClick: () -> Unit,
  onAnimalSelectionClick: () -> Unit,
  onStatisticsClick: () -> Unit,
  onAchievementsClick: () -> Unit,
  onSettingsClick: () -> Unit,
) {
  val neon = LocalNeonColors.current

  CyberpunkBackground(modifier = Modifier.testTag("home_screen")) {
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
            .widthIn(max = 560.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          NeonIconButton(
            icon =
              if (soundEnabled) Icons.AutoMirrored.Rounded.VolumeUp
              else Icons.AutoMirrored.Rounded.VolumeOff,
            contentDescription = stringResource(R.string.cd_sound_toggle),
            onClick = onToggleSound,
            accentColor = if (soundEnabled) neon.primaryCyan else neon.textSecondary,
            testTag = "home_sound_toggle",
          )

          Text(
            text = "MIDNIGHT JUNGLE",
            style =
              MaterialTheme.typography.labelSmall
                .copy(letterSpacing = 2.2.sp)
                .withNeonGlow(neon.emeraldAccent, 10f),
            color = neon.emeraldAccent,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
          )

          NeonIconButton(
            icon = Icons.Rounded.Settings,
            contentDescription = stringResource(R.string.cd_settings),
            onClick = onSettingsClick,
            accentColor = neon.primaryCyan,
            testTag = "home_settings_icon",
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        AnimalBattleLogoBadge(
          badgeSize = 102.dp,
          player1Animal = player1Animal,
          player2Animal = player2Animal,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "ANIMAL BATTLE",
          style =
            MaterialTheme.typography.displayLarge
              .copy(fontSize = 30.sp, letterSpacing = 3.5.sp)
              .withNeonGlow(neon.primaryCyan, 20f),
          color = neon.textPrimary,
          textAlign = TextAlign.Center,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = "TIC TAC TOE",
          style =
            MaterialTheme.typography.headlineMedium
              .copy(fontSize = 19.sp, letterSpacing = 2.8.sp)
              .withNeonGlow(neon.redO, 14f),
          color = neon.textPrimary,
          textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Active Matchup Pill (tappable -> Animal Selection)
        Surface(
          modifier = Modifier.fillMaxWidth().testTag("home_fighter_matchup"),
          shape = RoundedCornerShape(14.dp),
          color = neon.cardSurface.copy(alpha = 0.86f),
          border = BorderStroke(1.1.dp, neon.gridLine.copy(alpha = 0.85f)),
          onClick = onAnimalSelectionClick,
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
          ) {
            AnimatedAnimalIcon(animal = player1Animal, modifier = Modifier.size(26.dp), animate = false)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = player1Animal.title.uppercase(),
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = player1Animal.accentColor(),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = "  VS  ",
              style = MaterialTheme.typography.labelSmall,
              color = neon.emeraldAccent,
            )
            Text(
              text = player2Animal.title.uppercase(),
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = player2Animal.accentColor(),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(6.dp))
            AnimatedAnimalIcon(animal = player2Animal, modifier = Modifier.size(26.dp), animate = false)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        NeonButton(
          text = stringResource(R.string.play_game),
          subtitle = "Instant ${player1Animal.title} vs ${player2Animal.title} duel",
          onClick = onPlayGameClick,
          icon = Icons.Rounded.PlayArrow,
          accentColor = neon.primaryCyan,
          isPrimary = true,
          modifier = Modifier.fillMaxWidth(),
          testTag = "play_game_button",
        )

        Spacer(modifier = Modifier.height(8.dp))

        NeonButton(
          text = stringResource(R.string.play_vs_ai),
          subtitle = "Battle Easy to Expert Jungle AI",
          onClick = onPlayVsAiClick,
          icon = Icons.Rounded.SmartToy,
          accentColor = neon.emeraldAccent,
          modifier = Modifier.fillMaxWidth(),
          testTag = "play_vs_ai_button",
        )

        Spacer(modifier = Modifier.height(8.dp))

        NeonButton(
          text = stringResource(R.string.two_player),
          subtitle = "Local pass & play animal showdown",
          onClick = onTwoPlayerClick,
          icon = Icons.Rounded.Groups,
          accentColor = neon.redO,
          modifier = Modifier.fillMaxWidth(),
          testTag = "two_player_button",
        )

        Spacer(modifier = Modifier.height(8.dp))

        NeonButton(
          text = stringResource(R.string.game_modes),
          subtitle = "Lion vs Wolf, 3×3 Classic up to 9×9 Expert",
          onClick = onGameModesClick,
          icon = Icons.Rounded.GridView,
          accentColor = neon.purpleNeon,
          modifier = Modifier.fillMaxWidth(),
          testTag = "game_modes_button",
        )

        Spacer(modifier = Modifier.height(8.dp))

        NeonButton(
          text = stringResource(R.string.animal_selection),
          subtitle = "Choose from 8 unlockable animal champions",
          onClick = onAnimalSelectionClick,
          icon = Icons.Rounded.Pets,
          accentColor = neon.emeraldAccent,
          modifier = Modifier.fillMaxWidth(),
          testTag = "animal_selection_button",
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          NeonButton(
            text = stringResource(R.string.statistics),
            onClick = onStatisticsClick,
            icon = Icons.Rounded.BarChart,
            accentColor = neon.primaryCyan,
            modifier = Modifier.weight(1f),
            testTag = "statistics_button",
          )
          NeonButton(
            text = stringResource(R.string.achievements),
            onClick = onAchievementsClick,
            icon = Icons.Rounded.EmojiEvents,
            accentColor = neon.redO,
            modifier = Modifier.weight(1f),
            testTag = "achievements_button",
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        NeonButton(
          text = stringResource(R.string.settings),
          onClick = onSettingsClick,
          icon = Icons.Rounded.Settings,
          accentColor = neon.purpleNeon,
          modifier = Modifier.fillMaxWidth(),
          testTag = "settings_button",
        )

        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}
