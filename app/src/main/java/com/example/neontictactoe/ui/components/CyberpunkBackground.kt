package com.example.neontictactoe.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.example.neontictactoe.ui.theme.LocalNeonColors
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private data class CyberParticle(
  val xFraction: Float,
  val yFraction: Float,
  val radiusDp: Float,
  val phase: Float,
  val driftSpeed: Float,
  val tintKind: Int,
)

@Composable
fun CyberpunkBackground(
  modifier: Modifier = Modifier,
  showRetroFloor: Boolean = true,
  content: @Composable BoxScope.() -> Unit,
) {
  val neon = LocalNeonColors.current
  val particles = remember {
    val rng = Random(4209)
    List(42) { index ->
      CyberParticle(
        xFraction = rng.nextFloat(),
        yFraction = rng.nextFloat() * 0.78f,
        radiusDp = 0.9f + rng.nextFloat() * 1.9f,
        phase = rng.nextFloat() * (2f * PI.toFloat()),
        driftSpeed = 0.04f + rng.nextFloat() * 0.08f,
        tintKind = index % 5,
      )
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "cyber_bg")
  val twinkleTime by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = (2f * PI).toFloat(),
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 6000, easing = LinearEasing),
          repeatMode = RepeatMode.Restart,
        ),
      label = "twinkle",
    )

  val floorScroll by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 3600, easing = LinearEasing),
          repeatMode = RepeatMode.Restart,
        ),
      label = "floor_scroll",
    )

  val horizonPaint = remember {
    Paint().apply {
      asFrameworkPaint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
      }
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      // Deep Midnight Jungle backdrop
      drawRect(
        brush =
          Brush.verticalGradient(
            colors =
              listOf(
                neon.background,
                neon.secondaryBackground,
                neon.background,
              )
          )
      )

      // Subtle dual-tone radial cyan & emerald jungle aura
      drawCircle(
        brush =
          Brush.radialGradient(
            colors =
              listOf(
                neon.primaryCyan.copy(alpha = 0.14f),
                neon.emeraldAccent.copy(alpha = 0.06f),
                neon.purpleNeon.copy(alpha = 0.04f),
                Color.Transparent,
              ),
            center = Offset(size.width * 0.5f, size.height * 0.42f),
            radius = size.maxDimension * 0.62f,
          ),
        center = Offset(size.width * 0.5f, size.height * 0.42f),
        radius = size.maxDimension * 0.62f,
      )

      // Glowing stars & firefly jungle motes
      for (p in particles) {
        val tNorm = twinkleTime / (2f * PI.toFloat())
        val yWrapped = ((p.yFraction - tNorm * p.driftSpeed) % 0.78f + 0.78f) % 0.78f
        val alpha = 0.22f + 0.62f * ((sin(twinkleTime + p.phase) + 1f) * 0.5f)
        val baseColor =
          when (p.tintKind) {
            0 -> neon.primaryCyan
            1 -> neon.emeraldAccent
            2 -> neon.redO
            3 -> neon.purpleNeon
            else -> Color.White
          }
        val color = baseColor.copy(alpha = alpha * if (p.tintKind < 4) 0.78f else 0.70f)
        val pos = Offset(p.xFraction * size.width, yWrapped * size.height)
        val radiusPx = p.radiusDp * density
        drawCircle(color = color.copy(alpha = alpha * 0.24f), radius = radiusPx * 2.8f, center = pos)
        drawCircle(color = color, radius = radiusPx, center = pos)
      }

      if (showRetroFloor) {
        val horizonY = size.height * 0.78f
        val floorHeight = size.height - horizonY
        val vanishX = size.width * 0.5f
        val vanishY = horizonY - size.height * 0.13f

        // Emerald-cyan jungle grid mist
        drawRect(
          brush =
            Brush.verticalGradient(
              colors =
                listOf(
                  Color.Transparent,
                  neon.primaryCyan.copy(alpha = 0.12f),
                  neon.floorGrid.copy(alpha = 0.24f),
                ),
              startY = horizonY - 24f * density,
              endY = size.height,
            ),
          topLeft = Offset(0f, horizonY - 24f * density),
          size = Size(size.width, floorHeight + 24f * density),
        )

        drawIntoCanvas { canvas ->
          val fw = horizonPaint.asFrameworkPaint()
          fw.color = neon.primaryCyan.copy(alpha = 0.85f).toArgb()
          fw.strokeWidth = 5f * density
          fw.maskFilter = BlurMaskFilter(10f * density, BlurMaskFilter.Blur.NORMAL)
          canvas.nativeCanvas.drawLine(0f, horizonY, size.width, horizonY, fw)
        }
        drawLine(
          color = Color.White.copy(alpha = 0.68f),
          start = Offset(0f, horizonY),
          end = Offset(size.width, horizonY),
          strokeWidth = 1.4f * density,
        )

        val rayCount = 14
        for (i in -rayCount..rayCount) {
          val bottomX = vanishX + i * (size.width * 0.14f)
          val t = (horizonY - vanishY) / (size.height - vanishY)
          val startX = vanishX + (bottomX - vanishX) * t
          drawLine(
            color = neon.gridLine.copy(alpha = 0.42f),
            start = Offset(startX, horizonY),
            end = Offset(bottomX, size.height),
            strokeWidth = 1.15f * density,
          )
        }

        val rowCount = 9
        for (r in 0 until rowCount) {
          val raw = ((r + floorScroll) / rowCount).coerceIn(0f, 1f)
          val depth = raw * raw
          val y = horizonY + floorHeight * depth
          val alpha = (0.16f + 0.52f * raw).coerceIn(0f, 0.72f)
          drawLine(
            color = neon.floorGrid.copy(alpha = alpha),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = (0.85f + raw * 1.1f) * density,
          )
        }
      }
    }

    content()
  }
}
