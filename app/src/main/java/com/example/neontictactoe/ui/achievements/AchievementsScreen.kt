package com.example.neontictactoe.ui.achievements

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
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.neontictactoe.data.GameStatistics
import com.example.neontictactoe.domain.AchievementId
import com.example.neontictactoe.domain.AnimalType
import com.example.neontictactoe.ui.components.AnimatedAnimalIcon
import com.example.neontictactoe.ui.components.CyberpunkBackground
import com.example.neontictactoe.ui.components.NeonCard
import com.example.neontictactoe.ui.components.NeonTopBar
import com.example.neontictactoe.ui.theme.LocalNeonColors
import com.example.neontictactoe.ui.theme.accentColor

@Composable
fun AchievementsScreen(
  statistics: GameStatistics,
  onSelectAnimalsClick: () -> Unit,
  onBackClick: () -> Unit,
) {
  val neon = LocalNeonColors.current
  val unlockedCount = statistics.unlockedAchievements.size
  val totalCount = AchievementId.entries.size
  val progress = if (totalCount > 0) unlockedCount.toFloat() / totalCount.toFloat() else 0f

  CyberpunkBackground(
    modifier = Modifier.testTag("achievements_screen"),
    showRetroFloor = false,
  ) {
    Box(
      modifier = Modifier.fillMaxSize().navigationBarsPadding(),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(modifier = Modifier.fillMaxSize().widthIn(max = 600.dp)) {
        NeonTopBar(
          title = "ACHIEVEMENTS",
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
          // Summary progress hero card
          NeonCard(
            accentColor = neon.emeraldAccent,
            isSelected = true,
            testTag = "achievements_summary_card",
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                Text(
                  text = "ARENA MILESTONES",
                  style = MaterialTheme.typography.labelLarge,
                  color = neon.emeraldAccent,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "$unlockedCount / $totalCount UNLOCKED",
                  style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                  color = neon.textPrimary,
                  maxLines = 1,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text =
                    "${statistics.unlockedAnimals.size} / ${AnimalType.entries.size} Animal Fighters Unlocked",
                  style = MaterialTheme.typography.bodyMedium,
                  color = neon.textSecondary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
              Icon(
                imageVector = Icons.Rounded.EmojiEvents,
                contentDescription = null,
                tint = neon.winLine,
                modifier = Modifier.size(44.dp),
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
              progress = { progress },
              color = neon.emeraldAccent,
              trackColor = neon.gridLine.copy(alpha = 0.35f),
              modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            )
          }

          AchievementId.entries.forEach { achievement ->
            val unlocked = achievement in statistics.unlockedAchievements
            val reward = achievement.rewardAnimal
            val accent =
              when {
                unlocked && reward != null -> reward.accentColor()
                unlocked -> neon.emeraldAccent
                else -> neon.gridLine
              }

            NeonCard(
              accentColor = accent,
              isSelected = unlocked,
              onClick = if (unlocked && reward != null) onSelectAnimalsClick else null,
              testTag = "achievement_card_${achievement.name.lowercase()}",
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                if (reward != null) {
                  AnimatedAnimalIcon(
                    animal = reward,
                    modifier = Modifier.size(54.dp),
                    animate = false,
                    isLocked = !unlocked,
                  )
                } else {
                  Surface(
                    modifier = Modifier.size(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = accent.copy(alpha = if (unlocked) 0.22f else 0.12f),
                    border = BorderStroke(1.2.dp, accent),
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        tint = if (unlocked) neon.winLine else neon.textSecondary,
                        modifier = Modifier.size(28.dp),
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = achievement.title.uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (unlocked) neon.textPrimary else neon.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = neon.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                  )
                  if (reward != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text =
                        if (unlocked) "UNLOCKED: ${reward.title.uppercase()}"
                        else "REWARD: ${reward.title.uppercase()}",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = if (unlocked) reward.accentColor() else neon.primaryCyan,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                  }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                  imageVector =
                    if (unlocked) Icons.Rounded.CheckCircle else Icons.Rounded.Lock,
                  contentDescription = if (unlocked) "Unlocked" else "Locked",
                  tint = if (unlocked) neon.emeraldAccent else neon.textSecondary,
                  modifier = Modifier.size(22.dp),
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))
        }
      }
    }
  }
}
