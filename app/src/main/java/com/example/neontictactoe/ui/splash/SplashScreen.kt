package com.example.neontictactoe.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neontictactoe.domain.AnimalType
import com.example.neontictactoe.ui.components.AnimatedAnimalIcon
import com.example.neontictactoe.ui.components.CyberpunkBackground
import com.example.neontictactoe.ui.theme.LocalNeonColors
import com.example.neontictactoe.ui.theme.withNeonGlow
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onSplashFinished: () -> Unit,
) {
  val neon = LocalNeonColors.current
  val loadProgress = remember { Animatable(0f) }
  val titleAlpha = remember { Animatable(0f) }
  var hasFinished by remember { mutableStateOf(false) }

  fun completeOnce() {
    if (!hasFinished) {
      hasFinished = true
      onSplashFinished()
    }
  }

  LaunchedEffect(Unit) {
    titleAlpha.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))
    loadProgress.animateTo(1f, animationSpec = tween(1400, easing = FastOutSlowInEasing))
    delay(120)
    completeOnce()
  }

  CyberpunkBackground(
    modifier =
      Modifier
        .testTag("splash_screen")
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = ::completeOnce,
        )
  ) {
    Column(
      modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier =
          Modifier
            .border(1.8.dp, neon.primaryCyan.copy(alpha = 0.90f), RoundedCornerShape(28.dp))
            .background(neon.cardSurface.copy(alpha = 0.88f), RoundedCornerShape(28.dp))
            .padding(horizontal = 26.dp, vertical = 20.dp),
      ) {
        AnimatedAnimalIcon(
          animal = AnimalType.LION,
          modifier = Modifier.size(72.dp),
          animate = true,
        )
        Text(
          text = "VS",
          style = MaterialTheme.typography.titleLarge.withNeonGlow(neon.emeraldAccent, 14f),
          color = neon.emeraldAccent,
        )
        AnimatedAnimalIcon(
          animal = AnimalType.WOLF,
          modifier = Modifier.size(72.dp),
          animate = true,
        )
      }

      Spacer(modifier = Modifier.height(26.dp))

      Text(
        text = "ANIMAL BATTLE",
        style =
          MaterialTheme.typography.displayLarge
            .copy(fontSize = 32.sp, letterSpacing = 4.sp)
            .withNeonGlow(neon.primaryCyan, 22f),
        color = neon.textPrimary,
        textAlign = TextAlign.Center,
        modifier = Modifier.alpha(titleAlpha.value),
      )
      Text(
        text = "TIC TAC TOE",
        style =
          MaterialTheme.typography.headlineMedium
            .copy(letterSpacing = 3.2.sp)
            .withNeonGlow(neon.redO, 16f),
        color = neon.textPrimary,
        textAlign = TextAlign.Center,
        modifier = Modifier.alpha(titleAlpha.value),
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "MIDNIGHT JUNGLE ARENA",
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.6.sp),
        color = neon.emeraldAccent,
        textAlign = TextAlign.Center,
        modifier = Modifier.alpha(titleAlpha.value),
      )

      Spacer(modifier = Modifier.height(40.dp))

      Box(
        modifier =
          Modifier
            .width(230.dp)
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(neon.cardSurface)
            .border(1.2.dp, neon.primaryCyan.copy(alpha = 0.75f), RoundedCornerShape(5.dp))
      ) {
        Box(
          modifier =
            Modifier
              .fillMaxWidth(loadProgress.value)
              .fillMaxHeight()
              .background(
                brush = Brush.horizontalGradient(listOf(neon.blueX, neon.emeraldAccent, neon.redO)),
                shape = RoundedCornerShape(5.dp),
              )
        )
      }

      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = "SUMMONING CHAMPIONS...",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp),
        color = neon.textSecondary,
      )
    }
  }
}
