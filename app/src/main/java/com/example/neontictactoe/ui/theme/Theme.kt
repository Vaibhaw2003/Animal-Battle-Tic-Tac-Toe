package com.example.neontictactoe.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import com.example.neontictactoe.domain.NeonThemeStyle

val LocalNeonColors = staticCompositionLocalOf { paletteForTheme(NeonThemeStyle.MIDNIGHT_JUNGLE) }

private val NeonShapes =
  Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
  )

@Composable
fun NeonTicTacToeTheme(
  themeStyle: NeonThemeStyle = NeonThemeStyle.MIDNIGHT_JUNGLE,
  content: @Composable () -> Unit,
) {
  val palette = paletteForTheme(themeStyle)
  val colorScheme =
    darkColorScheme(
      primary = palette.primaryCyan,
      onPrimary = palette.background,
      secondary = palette.emeraldAccent,
      onSecondary = palette.background,
      tertiary = palette.redO,
      onTertiary = palette.background,
      background = palette.background,
      onBackground = palette.textPrimary,
      surface = palette.secondaryBackground,
      onSurface = palette.textPrimary,
      surfaceVariant = palette.cardSurface,
      onSurfaceVariant = palette.textSecondary,
      outline = palette.gridLine,
    )

  CompositionLocalProvider(LocalNeonColors provides palette) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = NeonTypography,
      shapes = NeonShapes,
      content = content,
    )
  }
}
