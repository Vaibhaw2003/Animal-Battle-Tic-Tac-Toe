package com.example.neontictactoe.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.neontictactoe.domain.AnimalType
import com.example.neontictactoe.domain.NeonThemeStyle

val MidnightJungleBg = Color(0xFF020B18)
val MidnightJungleSecondary = Color(0xFF05162B)
val MidnightJungleSurface = Color(0xFF071F3A)
val ElectricCyan = Color(0xFF00E5FF)
val EmeraldGreen = Color(0xFF00DFA5)
val LionCyanBlue = Color(0xFF00BFFF)
val WolfGoldenAmber = Color(0xFFFFB000)
val JunglePurpleAccent = Color(0xFF8747FF)
val JungleGridTeal = Color(0xFF087C91)
val JungleWhite = Color(0xFFFFFFFF)
val JungleTextMuted = Color(0xFF9AC4D4)
val JungleWinGold = Color(0xFFFFD54F)

@Immutable
data class NeonColorPalette(
  val background: Color,
  val secondaryBackground: Color,
  val cardSurface: Color,
  val primaryCyan: Color,
  val emeraldAccent: Color,
  val purpleNeon: Color,
  val gridLine: Color,
  val floorGrid: Color,
  val blueX: Color,
  val redO: Color,
  val winLine: Color,
  val winLineCore: Color,
  val textPrimary: Color,
  val textSecondary: Color,
  val starTint: Color,
)

fun AnimalType.accentColor(): Color = Color(accentHex)

fun AnimalType.glowColor(): Color = Color(glowHex)

fun AnimalType.secondaryColor(): Color = Color(secondaryHex)

fun paletteForTheme(style: NeonThemeStyle): NeonColorPalette =
  when (style) {
    NeonThemeStyle.MIDNIGHT_JUNGLE ->
      NeonColorPalette(
        background = MidnightJungleBg,
        secondaryBackground = MidnightJungleSecondary,
        cardSurface = MidnightJungleSurface,
        primaryCyan = ElectricCyan,
        emeraldAccent = EmeraldGreen,
        purpleNeon = JunglePurpleAccent,
        gridLine = JungleGridTeal,
        floorGrid = EmeraldGreen,
        blueX = LionCyanBlue,
        redO = WolfGoldenAmber,
        winLine = JungleWinGold,
        winLineCore = JungleWhite,
        textPrimary = JungleWhite,
        textSecondary = JungleTextMuted,
        starTint = ElectricCyan,
      )
    NeonThemeStyle.EMERALD_CANOPY ->
      NeonColorPalette(
        background = Color(0xFF021114),
        secondaryBackground = Color(0xFF041D22),
        cardSurface = Color(0xFF08292F),
        primaryCyan = EmeraldGreen,
        emeraldAccent = ElectricCyan,
        purpleNeon = Color(0xFF7B5CFF),
        gridLine = Color(0xFF0A876F),
        floorGrid = EmeraldGreen,
        blueX = Color(0xFF00E5FF),
        redO = Color(0xFFFFC400),
        winLine = Color(0xFFFFEA70),
        winLineCore = JungleWhite,
        textPrimary = JungleWhite,
        textSecondary = Color(0xFF9CD4CA),
        starTint = EmeraldGreen,
      )
    NeonThemeStyle.ELECTRIC_ABYSS ->
      NeonColorPalette(
        background = Color(0xFF03091B),
        secondaryBackground = Color(0xFF071333),
        cardSurface = Color(0xFF0B1C48),
        primaryCyan = ElectricCyan,
        emeraldAccent = Color(0xFF26F7C2),
        purpleNeon = Color(0xFFA05BFF),
        gridLine = Color(0xFF106F9E),
        floorGrid = ElectricCyan,
        blueX = LionCyanBlue,
        redO = Color(0xFFFFA000),
        winLine = JungleWinGold,
        winLineCore = JungleWhite,
        textPrimary = JungleWhite,
        textSecondary = Color(0xFFA2C2DF),
        starTint = Color(0xFF8747FF),
      )
    NeonThemeStyle.ROYAL_SAVANNA ->
      NeonColorPalette(
        background = Color(0xFF040D1A),
        secondaryBackground = Color(0xFF09182D),
        cardSurface = Color(0xFF0E2340),
        primaryCyan = Color(0xFF1CE8FF),
        emeraldAccent = EmeraldGreen,
        purpleNeon = Color(0xFF9C6BFF),
        gridLine = Color(0xFF12869A),
        floorGrid = WolfGoldenAmber,
        blueX = LionCyanBlue,
        redO = WolfGoldenAmber,
        winLine = Color(0xFFFFE082),
        winLineCore = JungleWhite,
        textPrimary = JungleWhite,
        textSecondary = Color(0xFFA8C6D8),
        starTint = WolfGoldenAmber,
      )
  }
