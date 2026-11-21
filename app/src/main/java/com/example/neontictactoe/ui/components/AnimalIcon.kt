package com.example.neontictactoe.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.neontictactoe.domain.AnimalType
import com.example.neontictactoe.ui.theme.LocalNeonColors
import com.example.neontictactoe.ui.theme.accentColor
import com.example.neontictactoe.ui.theme.glowColor
import com.example.neontictactoe.ui.theme.secondaryColor
import kotlinx.coroutines.launch

@Composable
fun AnimatedAnimalIcon(
  animal: AnimalType,
  modifier: Modifier = Modifier,
  animate: Boolean = true,
  isWinningCell: Boolean = false,
  isLocked: Boolean = false,
  strokeScale: Float = 1f,
  overrideColor: Color? = null,
) {
  val accent = overrideColor ?: animal.accentColor()
  val glow = overrideColor ?: animal.glowColor()
  val secondary = animal.secondaryColor()

  val scaleAnim = remember(animal, animate) { Animatable(if (animate) 0.48f else 1f) }
  val alphaAnim = remember(animal, animate) { Animatable(if (animate) 0f else 1f) }
  val rotAnim = remember(animal, animate) { Animatable(if (animate && animal == AnimalType.FOX) -14f else 0f) }
  val offsetYAnim = remember(animal, animate) { Animatable(if (animate && animal == AnimalType.EAGLE) -16f else 0f) }
  val offsetXAnim = remember(animal, animate) { Animatable(if (animate && animal == AnimalType.TIGER) -14f else 0f) }

  LaunchedEffect(animal, animate) {
    if (animate) {
      alphaAnim.snapTo(0f)
      launch { alphaAnim.animateTo(1f, tween(180, easing = FastOutSlowInEasing)) }

      when (animal) {
        AnimalType.LION -> {
          scaleAnim.snapTo(0.55f)
          scaleAnim.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
          )
        }
        AnimalType.WOLF -> {
          scaleAnim.snapTo(0.62f)
          scaleAnim.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
          )
        }
        AnimalType.TIGER -> {
          scaleAnim.snapTo(0.70f)
          offsetXAnim.snapTo(-14f)
          launch { offsetXAnim.animateTo(0f, tween(220, easing = FastOutSlowInEasing)) }
          scaleAnim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
        AnimalType.EAGLE -> {
          scaleAnim.snapTo(0.75f)
          offsetYAnim.snapTo(-16f)
          launch { offsetYAnim.animateTo(0f, tween(230, easing = FastOutSlowInEasing)) }
          scaleAnim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
        AnimalType.FOX -> {
          scaleAnim.snapTo(0.60f)
          rotAnim.snapTo(-14f)
          launch { rotAnim.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
          scaleAnim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
        AnimalType.PANDA -> {
          scaleAnim.snapTo(0.50f)
          scaleAnim.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
          )
        }
        AnimalType.BEAR -> {
          scaleAnim.snapTo(1.22f)
          scaleAnim.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
          )
        }
        AnimalType.DRAGON -> {
          scaleAnim.snapTo(0.52f)
          offsetYAnim.snapTo(12f)
          launch { offsetYAnim.animateTo(0f, tween(260, easing = FastOutSlowInEasing)) }
          scaleAnim.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
          )
        }
      }
    } else {
      alphaAnim.snapTo(1f)
      scaleAnim.snapTo(1f)
      rotAnim.snapTo(0f)
      offsetXAnim.snapTo(0f)
      offsetYAnim.snapTo(0f)
    }
  }

  val pulseTransition = rememberInfiniteTransition(label = "animal_pulse")
  val winGlowFactor by
    pulseTransition.animateFloat(
      initialValue = 0.80f,
      targetValue = 1.28f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 680, easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "animal_win_glow",
    )

  val glowPaint = remember {
    Paint().apply {
      asFrameworkPaint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeJoin = android.graphics.Paint.Join.ROUND
      }
    }
  }

  Canvas(
    modifier =
      modifier
        .alpha(if (isLocked) 0.42f else alphaAnim.value)
        .graphicsLayer {
          scaleX = scaleAnim.value
          scaleY = scaleAnim.value
          rotationZ = rotAnim.value
          translationX = offsetXAnim.value
          translationY = offsetYAnim.value
        }
  ) {
    val glowMult = if (isWinningCell) winGlowFactor else 1f
    val minDim = size.minDimension
    val pad = minDim * 0.08f
    val drawSize = minDim - pad * 2f
    val ox = (size.width - drawSize) / 2f
    val oy = (size.height - drawSize) / 2f
    val strokeW = (drawSize * 0.052f * strokeScale).coerceIn(1.8f * density, 6.5f * density)

    drawAnimalHead(
      animal = animal,
      ox = ox,
      oy = oy,
      s = drawSize,
      accent = if (isLocked) Color(0xFF567788) else accent,
      glow = if (isLocked) Color(0xFF567788) else glow,
      secondary = if (isLocked) Color(0xFF8AA8B8) else secondary,
      strokeW = strokeW,
      glowMult = if (isLocked) 0.35f else glowMult,
      glowPaint = glowPaint,
    )
  }
}

private fun DrawScope.drawAnimalHead(
  animal: AnimalType,
  ox: Float,
  oy: Float,
  s: Float,
  accent: Color,
  glow: Color,
  secondary: Color,
  strokeW: Float,
  glowMult: Float,
  glowPaint: Paint,
) {
  fun pt(nx: Float, ny: Float) = Offset(ox + nx * s, oy + ny * s)

  fun neonPath(path: Path, fillAlpha: Float = 0.24f, color: Color = accent, width: Float = strokeW) {
    drawPath(
      path = path,
      brush =
        Brush.verticalGradient(
          colors = listOf(color.copy(alpha = fillAlpha + 0.08f), Color(0xFF031122).copy(alpha = 0.90f)),
          startY = oy,
          endY = oy + s,
        ),
    )
    drawIntoCanvas { canvas ->
      val fw = glowPaint.asFrameworkPaint()
      fw.color = glow.copy(alpha = (0.72f * glowMult).coerceAtMost(1f)).toArgb()
      fw.strokeWidth = width * 2.2f
      fw.maskFilter = BlurMaskFilter((width * 1.4f * glowMult).coerceAtLeast(1f), BlurMaskFilter.Blur.NORMAL)
      canvas.nativeCanvas.drawPath(path.asAndroidPath(), fw)
    }
    drawPath(
      path = path,
      color = color,
      style = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
    drawPath(
      path = path,
      color = secondary.copy(alpha = 0.65f),
      style = Stroke(width = width * 0.32f, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
  }

  fun featureLine(a: Offset, b: Offset, color: Color = accent, width: Float = strokeW * 0.68f) {
    drawLine(color = color.copy(alpha = 0.95f), start = a, end = b, strokeWidth = width, cap = StrokeCap.Round)
  }

  fun eyePair(lx: Float, rx: Float, y: Float, tilt: Float = 0.04f, eyeColor: Color = secondary) {
    val leftEye = Path().apply {
      moveTo(pt(lx - 0.07f, y - tilt).x, pt(lx - 0.07f, y - tilt).y)
      lineTo(pt(lx + 0.05f, y + tilt * 0.7f).x, pt(lx + 0.05f, y + tilt * 0.7f).y)
      lineTo(pt(lx - 0.02f, y + 0.05f).x, pt(lx - 0.02f, y + 0.05f).y)
      close()
    }
    val rightEye = Path().apply {
      moveTo(pt(rx + 0.07f, y - tilt).x, pt(rx + 0.07f, y - tilt).y)
      lineTo(pt(rx - 0.05f, y + tilt * 0.7f).x, pt(rx - 0.05f, y + tilt * 0.7f).y)
      lineTo(pt(rx + 0.02f, y + 0.05f).x, pt(rx + 0.02f, y + 0.05f).y)
      close()
    }
    drawPath(leftEye, color = eyeColor)
    drawPath(rightEye, color = eyeColor)
  }

  when (animal) {
    AnimalType.LION -> {
      // Spiky royal mane silhouette
      val mane = Path().apply {
        moveTo(pt(0.50f, 0.04f).x, pt(0.50f, 0.04f).y)
        lineTo(pt(0.63f, 0.14f).x, pt(0.63f, 0.14f).y)
        lineTo(pt(0.78f, 0.09f).x, pt(0.78f, 0.09f).y)
        lineTo(pt(0.82f, 0.25f).x, pt(0.82f, 0.25f).y)
        lineTo(pt(0.96f, 0.31f).x, pt(0.96f, 0.31f).y)
        lineTo(pt(0.88f, 0.46f).x, pt(0.88f, 0.46f).y)
        lineTo(pt(0.95f, 0.62f).x, pt(0.95f, 0.62f).y)
        lineTo(pt(0.82f, 0.72f).x, pt(0.82f, 0.72f).y)
        lineTo(pt(0.78f, 0.88f).x, pt(0.78f, 0.88f).y)
        lineTo(pt(0.62f, 0.86f).x, pt(0.62f, 0.86f).y)
        lineTo(pt(0.50f, 0.96f).x, pt(0.50f, 0.96f).y)
        lineTo(pt(0.38f, 0.86f).x, pt(0.38f, 0.86f).y)
        lineTo(pt(0.22f, 0.88f).x, pt(0.22f, 0.88f).y)
        lineTo(pt(0.18f, 0.72f).x, pt(0.18f, 0.72f).y)
        lineTo(pt(0.05f, 0.62f).x, pt(0.05f, 0.62f).y)
        lineTo(pt(0.12f, 0.46f).x, pt(0.12f, 0.46f).y)
        lineTo(pt(0.04f, 0.31f).x, pt(0.04f, 0.31f).y)
        lineTo(pt(0.18f, 0.25f).x, pt(0.18f, 0.25f).y)
        lineTo(pt(0.22f, 0.09f).x, pt(0.22f, 0.09f).y)
        lineTo(pt(0.37f, 0.14f).x, pt(0.37f, 0.14f).y)
        close()
      }
      neonPath(mane, fillAlpha = 0.20f)

      // Inner sculpted lion face + rounded ears
      val face = Path().apply {
        moveTo(pt(0.30f, 0.28f).x, pt(0.30f, 0.28f).y)
        lineTo(pt(0.36f, 0.22f).x, pt(0.36f, 0.22f).y)
        lineTo(pt(0.50f, 0.25f).x, pt(0.50f, 0.25f).y)
        lineTo(pt(0.64f, 0.22f).x, pt(0.64f, 0.22f).y)
        lineTo(pt(0.70f, 0.28f).x, pt(0.70f, 0.28f).y)
        lineTo(pt(0.73f, 0.50f).x, pt(0.73f, 0.50f).y)
        lineTo(pt(0.64f, 0.72f).x, pt(0.64f, 0.72f).y)
        lineTo(pt(0.50f, 0.79f).x, pt(0.50f, 0.79f).y)
        lineTo(pt(0.36f, 0.72f).x, pt(0.36f, 0.72f).y)
        lineTo(pt(0.27f, 0.50f).x, pt(0.27f, 0.50f).y)
        close()
      }
      drawPath(
        face,
        color = accent.copy(alpha = 0.18f),
      )
      drawPath(
        face,
        color = secondary.copy(alpha = 0.85f),
        style = Stroke(width = strokeW * 0.65f, cap = StrokeCap.Round, join = StrokeJoin.Round),
      )

      // Brow ridge & fierce eyes
      featureLine(pt(0.33f, 0.40f), pt(0.45f, 0.45f), secondary)
      featureLine(pt(0.67f, 0.40f), pt(0.55f, 0.45f), secondary)
      eyePair(0.39f, 0.61f, 0.46f, tilt = 0.03f, eyeColor = Color.White)

      // Nose bridge + triangular lion nose + jaw muzzle
      featureLine(pt(0.50f, 0.43f), pt(0.50f, 0.57f), accent, strokeW * 0.55f)
      val nose = Path().apply {
        moveTo(pt(0.43f, 0.57f).x, pt(0.43f, 0.57f).y)
        lineTo(pt(0.57f, 0.57f).x, pt(0.57f, 0.57f).y)
        lineTo(pt(0.50f, 0.64f).x, pt(0.50f, 0.64f).y)
        close()
      }
      drawPath(nose, color = secondary)
      featureLine(pt(0.50f, 0.64f), pt(0.50f, 0.71f), secondary, strokeW * 0.6f)
      featureLine(pt(0.50f, 0.70f), pt(0.41f, 0.73f), secondary, strokeW * 0.55f)
      featureLine(pt(0.50f, 0.70f), pt(0.59f, 0.73f), secondary, strokeW * 0.55f)
    }

    AnimalType.WOLF -> {
      // Angular wolf head with tall pointed ears & jagged cheek fur
      val wolfOutline = Path().apply {
        moveTo(pt(0.24f, 0.06f).x, pt(0.24f, 0.06f).y) // Left ear tip
        lineTo(pt(0.41f, 0.24f).x, pt(0.41f, 0.24f).y)
        lineTo(pt(0.50f, 0.22f).x, pt(0.50f, 0.22f).y)
        lineTo(pt(0.59f, 0.24f).x, pt(0.59f, 0.24f).y)
        lineTo(pt(0.76f, 0.06f).x, pt(0.76f, 0.06f).y) // Right ear tip
        lineTo(pt(0.81f, 0.34f).x, pt(0.81f, 0.34f).y)
        lineTo(pt(0.93f, 0.48f).x, pt(0.93f, 0.48f).y) // Upper cheek spike
        lineTo(pt(0.82f, 0.55f).x, pt(0.82f, 0.55f).y)
        lineTo(pt(0.88f, 0.65f).x, pt(0.88f, 0.65f).y) // Lower cheek spike
        lineTo(pt(0.68f, 0.75f).x, pt(0.68f, 0.75f).y)
        lineTo(pt(0.50f, 0.94f).x, pt(0.50f, 0.94f).y) // Tapered chin/snout
        lineTo(pt(0.32f, 0.75f).x, pt(0.32f, 0.75f).y)
        lineTo(pt(0.12f, 0.65f).x, pt(0.12f, 0.65f).y)
        lineTo(pt(0.18f, 0.55f).x, pt(0.18f, 0.55f).y)
        lineTo(pt(0.07f, 0.48f).x, pt(0.07f, 0.48f).y)
        lineTo(pt(0.19f, 0.34f).x, pt(0.19f, 0.34f).y)
        close()
      }
      neonPath(wolfOutline, fillAlpha = 0.24f)

      // Inner ear lines + forehead crest
      featureLine(pt(0.26f, 0.14f), pt(0.33f, 0.31f), secondary, strokeW * 0.55f)
      featureLine(pt(0.74f, 0.14f), pt(0.67f, 0.31f), secondary, strokeW * 0.55f)
      featureLine(pt(0.50f, 0.24f), pt(0.50f, 0.44f), secondary, strokeW * 0.6f)

      // Slanted fierce golden eyes & snout bridge
      eyePair(0.37f, 0.63f, 0.48f, tilt = 0.055f, eyeColor = Color.White)
      featureLine(pt(0.41f, 0.56f), pt(0.46f, 0.72f), accent, strokeW * 0.55f)
      featureLine(pt(0.59f, 0.56f), pt(0.54f, 0.72f), accent, strokeW * 0.55f)

      // Wolf nose diamond & fang line
      val nose = Path().apply {
        moveTo(pt(0.44f, 0.71f).x, pt(0.44f, 0.71f).y)
        lineTo(pt(0.56f, 0.71f).x, pt(0.56f, 0.71f).y)
        lineTo(pt(0.50f, 0.80f).x, pt(0.50f, 0.80f).y)
        close()
      }
      drawPath(nose, color = secondary)
      featureLine(pt(0.42f, 0.83f), pt(0.58f, 0.83f), secondary, strokeW * 0.5f)
    }

    AnimalType.TIGER -> {
      val tigerHead = Path().apply {
        moveTo(pt(0.20f, 0.26f).x, pt(0.20f, 0.26f).y)
        lineTo(pt(0.24f, 0.12f).x, pt(0.24f, 0.12f).y) // Left rounded-edge ear
        lineTo(pt(0.37f, 0.20f).x, pt(0.37f, 0.20f).y)
        lineTo(pt(0.63f, 0.20f).x, pt(0.63f, 0.20f).y)
        lineTo(pt(0.76f, 0.12f).x, pt(0.76f, 0.12f).y) // Right ear
        lineTo(pt(0.80f, 0.26f).x, pt(0.80f, 0.26f).y)
        lineTo(pt(0.92f, 0.45f).x, pt(0.92f, 0.45f).y) // Wide cheek fur
        lineTo(pt(0.84f, 0.65f).x, pt(0.84f, 0.65f).y)
        lineTo(pt(0.68f, 0.85f).x, pt(0.68f, 0.85f).y)
        lineTo(pt(0.50f, 0.90f).x, pt(0.50f, 0.90f).y)
        lineTo(pt(0.32f, 0.85f).x, pt(0.32f, 0.85f).y)
        lineTo(pt(0.16f, 0.65f).x, pt(0.16f, 0.65f).y)
        lineTo(pt(0.08f, 0.45f).x, pt(0.08f, 0.45f).y)
        close()
      }
      neonPath(tigerHead)

      // Forehead Tiger "王" stripes + side cheek stripes
      featureLine(pt(0.41f, 0.27f), pt(0.59f, 0.27f), secondary, strokeW * 0.65f)
      featureLine(pt(0.43f, 0.34f), pt(0.57f, 0.34f), secondary, strokeW * 0.65f)
      featureLine(pt(0.50f, 0.24f), pt(0.50f, 0.42f), secondary, strokeW * 0.65f)
      featureLine(pt(0.14f, 0.48f), pt(0.28f, 0.51f), secondary, strokeW * 0.6f)
      featureLine(pt(0.17f, 0.59f), pt(0.29f, 0.60f), secondary, strokeW * 0.6f)
      featureLine(pt(0.86f, 0.48f), pt(0.72f, 0.51f), secondary, strokeW * 0.6f)
      featureLine(pt(0.83f, 0.59f), pt(0.71f, 0.60f), secondary, strokeW * 0.6f)

      eyePair(0.38f, 0.62f, 0.48f, tilt = 0.045f, eyeColor = Color.White)
      val nose = Path().apply {
        moveTo(pt(0.43f, 0.63f).x, pt(0.43f, 0.63f).y)
        lineTo(pt(0.57f, 0.63f).x, pt(0.57f, 0.63f).y)
        lineTo(pt(0.50f, 0.71f).x, pt(0.50f, 0.71f).y)
        close()
      }
      drawPath(nose, color = secondary)
      featureLine(pt(0.50f, 0.71f), pt(0.41f, 0.78f), secondary, strokeW * 0.55f)
      featureLine(pt(0.50f, 0.71f), pt(0.59f, 0.78f), secondary, strokeW * 0.55f)
    }

    AnimalType.EAGLE -> {
      // Swept-back fierce eagle crest & feathered collar
      val eagleHead = Path().apply {
        moveTo(pt(0.50f, 0.10f).x, pt(0.50f, 0.10f).y)
        lineTo(pt(0.76f, 0.18f).x, pt(0.76f, 0.18f).y)
        lineTo(pt(0.92f, 0.34f).x, pt(0.92f, 0.34f).y) // Swept wing-brow crest
        lineTo(pt(0.80f, 0.44f).x, pt(0.80f, 0.44f).y)
        lineTo(pt(0.86f, 0.62f).x, pt(0.86f, 0.62f).y)
        lineTo(pt(0.68f, 0.84f).x, pt(0.68f, 0.84f).y)
        lineTo(pt(0.50f, 0.92f).x, pt(0.50f, 0.92f).y)
        lineTo(pt(0.32f, 0.84f).x, pt(0.32f, 0.84f).y)
        lineTo(pt(0.14f, 0.62f).x, pt(0.14f, 0.62f).y)
        lineTo(pt(0.20f, 0.44f).x, pt(0.20f, 0.44f).y)
        lineTo(pt(0.08f, 0.34f).x, pt(0.08f, 0.34f).y)
        lineTo(pt(0.24f, 0.18f).x, pt(0.24f, 0.18f).y)
        close()
      }
      neonPath(eagleHead)

      // Intense V-brow
      featureLine(pt(0.21f, 0.34f), pt(0.50f, 0.44f), secondary, strokeW * 0.75f)
      featureLine(pt(0.79f, 0.34f), pt(0.50f, 0.44f), secondary, strokeW * 0.75f)
      eyePair(0.36f, 0.64f, 0.44f, tilt = 0.05f, eyeColor = Color.White)

      // Signature sharp hooked golden-white raptorial beak
      val beak = Path().apply {
        moveTo(pt(0.39f, 0.52f).x, pt(0.39f, 0.52f).y)
        lineTo(pt(0.61f, 0.52f).x, pt(0.61f, 0.52f).y)
        lineTo(pt(0.58f, 0.70f).x, pt(0.58f, 0.70f).y)
        lineTo(pt(0.50f, 0.84f).x, pt(0.50f, 0.84f).y) // Hooked tip
        lineTo(pt(0.42f, 0.70f).x, pt(0.42f, 0.70f).y)
        close()
      }
      drawPath(beak, color = Color(0xFFFFD54F).copy(alpha = 0.88f))
      drawPath(
        beak,
        color = Color.White,
        style = Stroke(width = strokeW * 0.6f, cap = StrokeCap.Round, join = StrokeJoin.Round),
      )
    }

    AnimalType.FOX -> {
      // Extra-tall pointed ears, wide cheekbones, razor-sharp snout
      val foxHead = Path().apply {
        moveTo(pt(0.18f, 0.06f).x, pt(0.18f, 0.06f).y) // Left ear tip
        lineTo(pt(0.40f, 0.28f).x, pt(0.40f, 0.28f).y)
        lineTo(pt(0.60f, 0.28f).x, pt(0.60f, 0.28f).y)
        lineTo(pt(0.82f, 0.06f).x, pt(0.82f, 0.06f).y) // Right ear tip
        lineTo(pt(0.80f, 0.39f).x, pt(0.80f, 0.39f).y)
        lineTo(pt(0.94f, 0.56f).x, pt(0.94f, 0.56f).y) // Wide cheek
        lineTo(pt(0.70f, 0.72f).x, pt(0.70f, 0.72f).y)
        lineTo(pt(0.50f, 0.93f).x, pt(0.50f, 0.93f).y) // Sharp snout
        lineTo(pt(0.30f, 0.72f).x, pt(0.30f, 0.72f).y)
        lineTo(pt(0.06f, 0.56f).x, pt(0.06f, 0.56f).y)
        lineTo(pt(0.20f, 0.39f).x, pt(0.20f, 0.39f).y)
        close()
      }
      neonPath(foxHead)

      // Cheek mask markings (white inner lower cheeks)
      featureLine(pt(0.12f, 0.57f), pt(0.43f, 0.67f), secondary, strokeW * 0.6f)
      featureLine(pt(0.88f, 0.57f), pt(0.57f, 0.67f), secondary, strokeW * 0.6f)
      featureLine(pt(0.23f, 0.16f), pt(0.31f, 0.33f), secondary, strokeW * 0.55f)
      featureLine(pt(0.77f, 0.16f), pt(0.69f, 0.33f), secondary, strokeW * 0.55f)

      eyePair(0.37f, 0.63f, 0.51f, tilt = 0.045f, eyeColor = Color.White)
      drawCircle(color = secondary, radius = s * 0.055f, center = pt(0.50f, 0.83f))
    }

    AnimalType.PANDA -> {
      // Iconic round ears
      drawCircle(
        color = accent.copy(alpha = 0.25f),
        radius = s * 0.14f,
        center = pt(0.24f, 0.22f),
      )
      drawCircle(
        color = accent,
        radius = s * 0.14f,
        center = pt(0.24f, 0.22f),
        style = Stroke(width = strokeW),
      )
      drawCircle(
        color = accent.copy(alpha = 0.25f),
        radius = s * 0.14f,
        center = pt(0.76f, 0.22f),
      )
      drawCircle(
        color = accent,
        radius = s * 0.14f,
        center = pt(0.76f, 0.22f),
        style = Stroke(width = strokeW),
      )

      // Broad rounded panda head contour
      val pandaHead = Path().apply {
        moveTo(pt(0.50f, 0.18f).x, pt(0.50f, 0.18f).y)
        cubicTo(
          pt(0.82f, 0.18f).x, pt(0.82f, 0.18f).y,
          pt(0.90f, 0.42f).x, pt(0.90f, 0.42f).y,
          pt(0.88f, 0.64f).x, pt(0.88f, 0.64f).y,
        )
        cubicTo(
          pt(0.86f, 0.84f).x, pt(0.86f, 0.84f).y,
          pt(0.68f, 0.91f).x, pt(0.68f, 0.91f).y,
          pt(0.50f, 0.91f).x, pt(0.50f, 0.91f).y,
        )
        cubicTo(
          pt(0.32f, 0.91f).x, pt(0.32f, 0.91f).y,
          pt(0.14f, 0.84f).x, pt(0.14f, 0.84f).y,
          pt(0.12f, 0.64f).x, pt(0.12f, 0.64f).y,
        )
        cubicTo(
          pt(0.10f, 0.42f).x, pt(0.10f, 0.42f).y,
          pt(0.18f, 0.18f).x, pt(0.18f, 0.18f).y,
          pt(0.50f, 0.18f).x, pt(0.50f, 0.18f).y,
        )
        close()
      }
      neonPath(pandaHead, fillAlpha = 0.20f)

      // Signature panda eye patches
      drawOval(
        color = accent.copy(alpha = 0.55f),
        topLeft = pt(0.24f, 0.39f),
        size = Size(s * 0.19f, s * 0.22f),
      )
      drawOval(
        color = accent.copy(alpha = 0.55f),
        topLeft = pt(0.57f, 0.39f),
        size = Size(s * 0.19f, s * 0.22f),
      )
      drawCircle(color = Color.White, radius = s * 0.042f, center = pt(0.35f, 0.49f))
      drawCircle(color = Color.White, radius = s * 0.042f, center = pt(0.65f, 0.49f))

      // Nose & cheerful curved mouth
      drawOval(
        color = secondary,
        topLeft = pt(0.44f, 0.62f),
        size = Size(s * 0.12f, s * 0.08f),
      )
      featureLine(pt(0.50f, 0.70f), pt(0.50f, 0.76f), secondary, strokeW * 0.55f)
      featureLine(pt(0.50f, 0.76f), pt(0.41f, 0.79f), secondary, strokeW * 0.55f)
      featureLine(pt(0.50f, 0.76f), pt(0.59f, 0.79f), secondary, strokeW * 0.55f)
    }

    AnimalType.BEAR -> {
      // Heavy bear ears
      drawCircle(color = accent.copy(alpha = 0.22f), radius = s * 0.13f, center = pt(0.22f, 0.22f))
      drawCircle(color = accent, radius = s * 0.13f, center = pt(0.22f, 0.22f), style = Stroke(width = strokeW))
      drawCircle(color = accent.copy(alpha = 0.22f), radius = s * 0.13f, center = pt(0.78f, 0.22f))
      drawCircle(color = accent, radius = s * 0.13f, center = pt(0.78f, 0.22f), style = Stroke(width = strokeW))

      // Broad hexagonal-domed bear skull
      val bearHead = Path().apply {
        moveTo(pt(0.50f, 0.16f).x, pt(0.50f, 0.16f).y)
        lineTo(pt(0.76f, 0.24f).x, pt(0.76f, 0.24f).y)
        lineTo(pt(0.90f, 0.48f).x, pt(0.90f, 0.48f).y)
        lineTo(pt(0.84f, 0.74f).x, pt(0.84f, 0.74f).y)
        lineTo(pt(0.66f, 0.90f).x, pt(0.66f, 0.90f).y)
        lineTo(pt(0.34f, 0.90f).x, pt(0.34f, 0.90f).y)
        lineTo(pt(0.16f, 0.74f).x, pt(0.16f, 0.74f).y)
        lineTo(pt(0.10f, 0.48f).x, pt(0.10f, 0.48f).y)
        lineTo(pt(0.24f, 0.24f).x, pt(0.24f, 0.24f).y)
        close()
      }
      neonPath(bearHead)

      // Prominent snout pad
      drawRoundRect(
        color = accent.copy(alpha = 0.22f),
        topLeft = pt(0.31f, 0.53f),
        size = Size(s * 0.38f, s * 0.28f),
        cornerRadius = CornerRadius(s * 0.14f, s * 0.14f),
      )
      drawRoundRect(
        color = secondary.copy(alpha = 0.8f),
        topLeft = pt(0.31f, 0.53f),
        size = Size(s * 0.38f, s * 0.28f),
        cornerRadius = CornerRadius(s * 0.14f, s * 0.14f),
        style = Stroke(width = strokeW * 0.6f),
      )
      eyePair(0.36f, 0.64f, 0.43f, tilt = 0.025f, eyeColor = Color.White)
      drawOval(color = secondary, topLeft = pt(0.43f, 0.57f), size = Size(s * 0.14f, s * 0.10f))
      featureLine(pt(0.50f, 0.67f), pt(0.50f, 0.75f), secondary, strokeW * 0.6f)
    }

    AnimalType.DRAGON -> {
      // Swept-back twin dragon horns, cheek spikes & armored jaw
      val dragonSilhouette = Path().apply {
        moveTo(pt(0.14f, 0.06f).x, pt(0.14f, 0.06f).y) // Left horn tip
        lineTo(pt(0.38f, 0.23f).x, pt(0.38f, 0.23f).y)
        lineTo(pt(0.50f, 0.16f).x, pt(0.50f, 0.16f).y) // Center crest
        lineTo(pt(0.62f, 0.23f).x, pt(0.62f, 0.23f).y)
        lineTo(pt(0.86f, 0.06f).x, pt(0.86f, 0.06f).y) // Right horn tip
        lineTo(pt(0.76f, 0.34f).x, pt(0.76f, 0.34f).y)
        lineTo(pt(0.95f, 0.38f).x, pt(0.95f, 0.38f).y) // Upper fin spike
        lineTo(pt(0.82f, 0.52f).x, pt(0.82f, 0.52f).y)
        lineTo(pt(0.90f, 0.66f).x, pt(0.90f, 0.66f).y) // Lower spike
        lineTo(pt(0.66f, 0.78f).x, pt(0.66f, 0.78f).y)
        lineTo(pt(0.50f, 0.94f).x, pt(0.50f, 0.94f).y) // Armored chin
        lineTo(pt(0.34f, 0.78f).x, pt(0.34f, 0.78f).y)
        lineTo(pt(0.10f, 0.66f).x, pt(0.10f, 0.66f).y)
        lineTo(pt(0.18f, 0.52f).x, pt(0.18f, 0.52f).y)
        lineTo(pt(0.05f, 0.38f).x, pt(0.05f, 0.38f).y)
        lineTo(pt(0.24f, 0.34f).x, pt(0.24f, 0.34f).y)
        close()
      }
      neonPath(dragonSilhouette)

      // Armored snout ridge & horn ribs
      featureLine(pt(0.50f, 0.18f), pt(0.50f, 0.72f), secondary, strokeW * 0.6f)
      featureLine(pt(0.20f, 0.14f), pt(0.35f, 0.31f), secondary, strokeW * 0.55f)
      featureLine(pt(0.80f, 0.14f), pt(0.65f, 0.31f), secondary, strokeW * 0.55f)

      // Glowing cyan plasma eyes + nostrils
      eyePair(0.37f, 0.63f, 0.47f, tilt = 0.06f, eyeColor = Color(0xFF00E5FF))
      drawCircle(color = secondary, radius = s * 0.028f, center = pt(0.43f, 0.73f))
      drawCircle(color = secondary, radius = s * 0.028f, center = pt(0.57f, 0.73f))
      featureLine(pt(0.39f, 0.82f), pt(0.50f, 0.87f), secondary, strokeW * 0.55f)
      featureLine(pt(0.61f, 0.82f), pt(0.50f, 0.87f), secondary, strokeW * 0.55f)
    }
  }
}

@Composable
fun AnimalBattleLogoBadge(
  modifier: Modifier = Modifier,
  badgeSize: Dp = 112.dp,
  player1Animal: AnimalType = AnimalType.LION,
  player2Animal: AnimalType = AnimalType.WOLF,
) {
  val neon = LocalNeonColors.current
  val pulseTransition = rememberInfiniteTransition(label = "logo_pulse")
  val glowAlpha by
    pulseTransition.animateFloat(
      initialValue = 0.58f,
      targetValue = 1.0f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "logo_glow",
    )

  val glowPaint = remember {
    Paint().apply {
      asFrameworkPaint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeJoin = android.graphics.Paint.Join.ROUND
      }
    }
  }

  Canvas(modifier = modifier.size(badgeSize)) {
    val inset = size.minDimension * 0.08f
    val boxSize = Size(size.width - inset * 2f, size.height - inset * 2f)
    val radiusPx = size.minDimension * 0.22f
    val corner = CornerRadius(radiusPx, radiusPx)

    // Outer cyan-emerald neon aura
    drawIntoCanvas { canvas ->
      val fw = glowPaint.asFrameworkPaint()
      fw.color = neon.primaryCyan.copy(alpha = glowAlpha * 0.82f).toArgb()
      fw.strokeWidth = 5.5f * density
      fw.maskFilter = BlurMaskFilter(13f * density, BlurMaskFilter.Blur.NORMAL)
      canvas.nativeCanvas.drawRoundRect(
        inset,
        inset,
        inset + boxSize.width,
        inset + boxSize.height,
        radiusPx,
        radiusPx,
        fw,
      )
    }

    drawRoundRect(
      brush =
        Brush.linearGradient(
          colors = listOf(neon.secondaryBackground, neon.background),
        ),
      topLeft = Offset(inset, inset),
      size = boxSize,
      cornerRadius = corner,
    )
    drawRoundRect(
      brush =
        Brush.linearGradient(
          colors = listOf(neon.primaryCyan, neon.emeraldAccent, neon.redO),
        ),
      topLeft = Offset(inset, inset),
      size = boxSize,
      cornerRadius = corner,
      style = Stroke(width = 2.6f * density),
    )

    // Subtle divider lightning line
    val midX = size.width * 0.5f
    drawLine(
      color = neon.gridLine.copy(alpha = 0.70f),
      start = Offset(midX, inset + boxSize.height * 0.16f),
      end = Offset(midX, size.height - inset - boxSize.height * 0.16f),
      strokeWidth = 1.6f * density,
    )

    val animalSize = boxSize.width * 0.39f
    val animalY = size.height * 0.5f - animalSize * 0.5f
    // Left fighter (Lion / Player 1)
    drawAnimalHead(
      animal = player1Animal,
      ox = inset + boxSize.width * 0.06f,
      oy = animalY,
      s = animalSize,
      accent = player1Animal.accentColor(),
      glow = player1Animal.glowColor(),
      secondary = player1Animal.secondaryColor(),
      strokeW = 2.2f * density,
      glowMult = glowAlpha,
      glowPaint = glowPaint,
    )
    // Right fighter (Wolf / Player 2)
    drawAnimalHead(
      animal = player2Animal,
      ox = size.width - inset - boxSize.width * 0.06f - animalSize,
      oy = animalY,
      s = animalSize,
      accent = player2Animal.accentColor(),
      glow = player2Animal.glowColor(),
      secondary = player2Animal.secondaryColor(),
      strokeW = 2.2f * density,
      glowMult = glowAlpha,
      glowPaint = glowPaint,
    )
  }
}
