package com.example.neontictactoe.ui.animals

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
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.neontictactoe.domain.AnimalType
import com.example.neontictactoe.domain.PlayerSlot
import com.example.neontictactoe.ui.components.AnimatedAnimalIcon
import com.example.neontictactoe.ui.components.CyberpunkBackground
import com.example.neontictactoe.ui.components.NeonButton
import com.example.neontictactoe.ui.components.NeonCard
import com.example.neontictactoe.ui.components.NeonTopBar
import com.example.neontictactoe.ui.theme.LocalNeonColors
import com.example.neontictactoe.ui.theme.accentColor

@Composable
fun AnimalSelectionScreen(
  player1Animal: AnimalType,
  player2Animal: AnimalType,
  unlockedAnimals: Set<AnimalType>,
  initialSlot: PlayerSlot = PlayerSlot.PLAYER_1,
  onSelectAnimal: (PlayerSlot, AnimalType) -> Boolean,
  onContinueToSetup: () -> Unit,
  onBackClick: () -> Unit,
) {
  val neon = LocalNeonColors.current
  var activeSlot by remember { mutableStateOf(initialSlot) }
  var lockedFeedback by remember { mutableStateOf<String?>(null) }

  CyberpunkBackground(
    modifier = Modifier.testTag("animal_selection_screen"),
    showRetroFloor = false,
  ) {
    Box(
      modifier = Modifier.fillMaxSize().navigationBarsPadding(),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(modifier = Modifier.fillMaxSize().widthIn(max = 600.dp)) {
        NeonTopBar(
          title = "ANIMAL FIGHTERS",
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
          // Player 1 / Player 2 slot switcher
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            SlotSelectorChip(
              label = "PLAYER 1",
              animal = player1Animal,
              selected = activeSlot == PlayerSlot.PLAYER_1,
              onClick = {
                activeSlot = PlayerSlot.PLAYER_1
                lockedFeedback = null
              },
              modifier = Modifier.weight(1f),
              testTag = "slot_player_1",
            )
            SlotSelectorChip(
              label = "PLAYER 2 / AI",
              animal = player2Animal,
              selected = activeSlot == PlayerSlot.PLAYER_2,
              onClick = {
                activeSlot = PlayerSlot.PLAYER_2
                lockedFeedback = null
              },
              modifier = Modifier.weight(1f),
              testTag = "slot_player_2",
            )
          }

          if (lockedFeedback != null) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = neon.redO.copy(alpha = 0.16f),
              border = BorderStroke(1.2.dp, neon.redO.copy(alpha = 0.8f)),
              modifier = Modifier.fillMaxWidth().testTag("animal_locked_banner"),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.Lock,
                  contentDescription = null,
                  tint = neon.redO,
                  modifier = Modifier.size(18.dp),
                )
                Text(
                  text = lockedFeedback.orEmpty(),
                  style = MaterialTheme.typography.labelSmall,
                  color = neon.textPrimary,
                )
              }
            }
          }

          Text(
            text =
              "CHOOSE ${if (activeSlot == PlayerSlot.PLAYER_1) "PLAYER 1" else "PLAYER 2 / AI"} ANIMAL (${unlockedAnimals.size}/${AnimalType.entries.size} UNLOCKED)",
            style = MaterialTheme.typography.labelLarge,
            color = neon.primaryCyan,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )

          AnimalType.entries.chunked(2).forEach { pair ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              pair.forEach { animal ->
                val isUnlocked = animal.unlockedByDefault || animal in unlockedAnimals
                val isP1 = player1Animal == animal
                val isP2 = player2Animal == animal
                val isActiveSelected =
                  (activeSlot == PlayerSlot.PLAYER_1 && isP1) ||
                    (activeSlot == PlayerSlot.PLAYER_2 && isP2)
                val accent = animal.accentColor()

                NeonCard(
                  accentColor = if (isUnlocked) accent else neon.gridLine,
                  isSelected = isActiveSelected,
                  onClick = {
                    if (isUnlocked) {
                      lockedFeedback = null
                      onSelectAnimal(activeSlot, animal)
                    } else {
                      lockedFeedback = "${animal.title} is locked — ${animal.unlockHint}"
                    }
                  },
                  modifier = Modifier.weight(1f),
                  testTag = "animal_card_${animal.name.lowercase()}",
                ) {
                  Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                  ) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      val roleBadge =
                        when {
                          isP1 -> "P1"
                          isP2 -> "P2"
                          else -> ""
                        }
                      if (roleBadge.isNotEmpty()) {
                        Surface(
                          shape = RoundedCornerShape(8.dp),
                          color = accent.copy(alpha = 0.25f),
                          border = BorderStroke(1.dp, accent),
                        ) {
                          Text(
                            text = roleBadge,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = neon.textPrimary,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                          )
                        }
                      } else {
                        Spacer(modifier = Modifier.width(1.dp))
                      }

                      if (!isUnlocked) {
                        Icon(
                          imageVector = Icons.Rounded.Lock,
                          contentDescription = "Locked",
                          tint = neon.textSecondary,
                          modifier = Modifier.size(18.dp),
                        )
                      } else if (isActiveSelected) {
                        Icon(
                          imageVector = Icons.Rounded.CheckCircle,
                          contentDescription = "Selected",
                          tint = accent,
                          modifier = Modifier.size(18.dp),
                        )
                      }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    AnimatedAnimalIcon(
                      animal = animal,
                      modifier = Modifier.size(72.dp),
                      animate = true,
                      isWinningCell = isActiveSelected,
                      isLocked = !isUnlocked,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                      text = animal.title.uppercase(),
                      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                      color = if (isUnlocked) neon.textPrimary else neon.textSecondary,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = if (isUnlocked) animal.tagline else animal.unlockHint,
                      style = MaterialTheme.typography.labelSmall,
                      color = if (isUnlocked) accent else neon.textSecondary,
                      textAlign = TextAlign.Center,
                      maxLines = 2,
                      overflow = TextOverflow.Ellipsis,
                    )
                  }
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
            text = "USE IN BATTLE",
            onClick = onContinueToSetup,
            accentColor = neon.emeraldAccent,
            isPrimary = true,
            modifier = Modifier.fillMaxWidth(),
            testTag = "animal_confirm_button",
          )
        }
      }
    }
  }
}

@Composable
private fun SlotSelectorChip(
  label: String,
  animal: AnimalType,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String,
) {
  val neon = LocalNeonColors.current
  val accent = animal.accentColor()
  Surface(
    modifier = modifier.testTag(testTag),
    shape = RoundedCornerShape(14.dp),
    color = if (selected) accent.copy(alpha = 0.22f) else neon.cardSurface.copy(alpha = 0.85f),
    border = BorderStroke(if (selected) 1.9.dp else 1.1.dp, if (selected) accent else neon.gridLine),
    onClick = onClick,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
    ) {
      AnimatedAnimalIcon(
        animal = animal,
        modifier = Modifier.size(30.dp),
        animate = false,
      )
      Spacer(modifier = Modifier.width(8.dp))
      Column(modifier = Modifier.weight(1f, fill = false)) {
        Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = neon.textSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = animal.title.uppercase(),
          style = MaterialTheme.typography.labelLarge,
          color = if (selected) accent else neon.textPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}
