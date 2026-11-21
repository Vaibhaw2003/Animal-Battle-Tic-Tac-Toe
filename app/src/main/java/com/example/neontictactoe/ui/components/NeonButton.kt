package com.example.neontictactoe.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.neontictactoe.ui.theme.LocalNeonColors

@Composable
fun NeonButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  icon: ImageVector? = null,
  accentColor: Color = LocalNeonColors.current.purpleNeon,
  isPrimary: Boolean = false,
  enabled: Boolean = true,
  testTag: String? = null,
) {
  val neon = LocalNeonColors.current
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by
    animateFloatAsState(
      targetValue = if (isPressed) 0.96f else 1f,
      animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
      label = "btn_scale",
    )

  val glowPaint = remember {
    Paint().apply {
      asFrameworkPaint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
      }
    }
  }

  val shape = RoundedCornerShape(16.dp)
  val containerBrush =
    if (isPrimary) {
      Brush.linearGradient(
        colors =
          listOf(
            accentColor.copy(alpha = 0.44f),
            neon.secondaryBackground.copy(alpha = 0.94f),
            accentColor.copy(alpha = 0.22f),
          )
      )
    } else {
      Brush.linearGradient(
        colors =
          listOf(
            neon.cardSurface.copy(alpha = 0.95f),
            neon.secondaryBackground.copy(alpha = 0.90f),
          )
      )
    }

  val baseModifier =
    modifier
      .scale(scale)
      .heightIn(min = 52.dp)
      .minimumInteractiveComponentSize()
      .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
      .drawBehind {
        if (enabled) {
          val radiusPx = 16.dp.toPx()
          val glowAlpha = if (isPrimary) 0.72f else if (isPressed) 0.65f else 0.40f
          drawIntoCanvas { canvas ->
            val fw = glowPaint.asFrameworkPaint()
            fw.color = accentColor.copy(alpha = glowAlpha).toArgb()
            fw.strokeWidth = if (isPrimary) 5f * density else 3.2f * density
            fw.maskFilter =
              BlurMaskFilter(
                (if (isPrimary) 12f else 8f) * density,
                BlurMaskFilter.Blur.NORMAL,
              )
            canvas.nativeCanvas.drawRoundRect(
              0f,
              0f,
              size.width,
              size.height,
              radiusPx,
              radiusPx,
              fw,
            )
          }
        }
      }
      .clip(shape)
      .background(brush = containerBrush, shape = shape)
      .border(
        width = if (isPrimary) 1.8.dp else 1.2.dp,
        brush =
          Brush.linearGradient(
            colors =
              listOf(
                if (enabled) accentColor else accentColor.copy(alpha = 0.35f),
                if (enabled) Color.White.copy(alpha = if (isPrimary) 0.55f else 0.22f)
                else accentColor.copy(alpha = 0.25f),
                if (enabled) accentColor.copy(alpha = 0.85f) else accentColor.copy(alpha = 0.35f),
              )
          ),
        shape = shape,
      )
      .clickable(
        enabled = enabled,
        interactionSource = interactionSource,
        indication = ripple(color = accentColor),
        onClick = onClick,
      )
      .padding(horizontal = 16.dp, vertical = 11.dp)

  Row(
    modifier = baseModifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center,
  ) {
    if (icon != null) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (enabled) accentColor else accentColor.copy(alpha = 0.5f),
        modifier = Modifier.size(22.dp),
      )
      Spacer(modifier = Modifier.width(10.dp))
    }
    Column(
      modifier = Modifier.weight(1f, fill = false),
      horizontalAlignment = if (icon == null) Alignment.CenterHorizontally else Alignment.Start,
    ) {
      Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = if (enabled) neon.textPrimary else neon.textSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      if (!subtitle.isNullOrBlank()) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.labelSmall,
          color = neon.textSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
fun NeonIconButton(
  icon: ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  accentColor: Color = LocalNeonColors.current.purpleNeon,
  size: Dp = 48.dp,
  testTag: String? = null,
) {
  val neon = LocalNeonColors.current
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by
    animateFloatAsState(
      targetValue = if (isPressed) 0.90f else 1f,
      animationSpec = spring(stiffness = Spring.StiffnessMedium),
      label = "icon_btn_scale",
    )

  val glowPaint = remember {
    Paint().apply {
      asFrameworkPaint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
      }
    }
  }

  Surface(
    modifier =
      modifier
        .size(size)
        .scale(scale)
        .minimumInteractiveComponentSize()
        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
        .drawBehind {
          val radiusPx = 14.dp.toPx()
          drawIntoCanvas { canvas ->
            val fw = glowPaint.asFrameworkPaint()
            fw.color = accentColor.copy(alpha = 0.45f).toArgb()
            fw.strokeWidth = 2.6f * density
            fw.maskFilter = BlurMaskFilter(7f * density, BlurMaskFilter.Blur.NORMAL)
            canvas.nativeCanvas.drawRoundRect(
              0f,
              0f,
              this.size.width,
              this.size.height,
              radiusPx,
              radiusPx,
              fw,
            )
          }
        },
    shape = RoundedCornerShape(14.dp),
    color = neon.cardSurface.copy(alpha = 0.90f),
    border = BorderStroke(1.3.dp, accentColor.copy(alpha = 0.85f)),
    onClick = onClick,
    interactionSource = interactionSource,
  ) {
    Box(contentAlignment = Alignment.Center) {
      Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = neon.textPrimary,
        modifier = Modifier.size(22.dp),
      )
    }
  }
}

@Composable
fun NeonCard(
  modifier: Modifier = Modifier,
  accentColor: Color = LocalNeonColors.current.purpleNeon,
  isSelected: Boolean = false,
  onClick: (() -> Unit)? = null,
  testTag: String? = null,
  content: @Composable ColumnScope.() -> Unit,
) {
  val neon = LocalNeonColors.current
  val shape = RoundedCornerShape(18.dp)
  val borderWidth = if (isSelected) 1.9.dp else 1.2.dp
  val bgColor =
    if (isSelected) accentColor.copy(alpha = 0.18f)
    else neon.cardSurface.copy(alpha = 0.88f)

  val glowPaint = remember {
    Paint().apply {
      asFrameworkPaint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
      }
    }
  }

  val cardMod =
    modifier
      .fillMaxWidth()
      .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
      .drawBehind {
        if (isSelected) {
          val radiusPx = 18.dp.toPx()
          drawIntoCanvas { canvas ->
            val fw = glowPaint.asFrameworkPaint()
            fw.color = accentColor.copy(alpha = 0.48f).toArgb()
            fw.strokeWidth = 3.5f * density
            fw.maskFilter = BlurMaskFilter(10f * density, BlurMaskFilter.Blur.NORMAL)
            canvas.nativeCanvas.drawRoundRect(
              0f,
              0f,
              size.width,
              size.height,
              radiusPx,
              radiusPx,
              fw,
            )
          }
        }
      }

  if (onClick != null) {
    Surface(
      modifier = cardMod,
      shape = shape,
      color = bgColor,
      border = BorderStroke(borderWidth, accentColor.copy(alpha = if (isSelected) 1f else 0.65f)),
      onClick = onClick,
    ) {
      Column(modifier = Modifier.padding(16.dp), content = content)
    }
  } else {
    Surface(
      modifier = cardMod,
      shape = shape,
      color = bgColor,
      border = BorderStroke(borderWidth, accentColor.copy(alpha = if (isSelected) 1f else 0.65f)),
    ) {
      Column(modifier = Modifier.padding(16.dp), content = content)
    }
  }
}
