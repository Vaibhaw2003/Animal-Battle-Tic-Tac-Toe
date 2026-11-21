package com.example.neontictactoe.domain

enum class PlayerSlot {
  PLAYER_1,
  PLAYER_2;

  fun opponent(): PlayerSlot = if (this == PLAYER_1) PLAYER_2 else PLAYER_1
}

enum class AnimalType(
  val title: String,
  val tagline: String,
  val accentHex: Long,
  val glowHex: Long,
  val secondaryHex: Long,
  val unlockedByDefault: Boolean,
  val unlockHint: String,
) {
  LION(
    title = "Lion",
    tagline = "Electric Cyan Sovereign",
    accentHex = 0xFF00BFFF,
    glowHex = 0xFF00E5FF,
    secondaryHex = 0xFF8BE9FF,
    unlockedByDefault = true,
    unlockHint = "Starter champion",
  ),
  WOLF(
    title = "Wolf",
    tagline = "Golden Amber Alpha",
    accentHex = 0xFFFFB000,
    glowHex = 0xFFFFD54F,
    secondaryHex = 0xFFFFF3B0,
    unlockedByDefault = true,
    unlockHint = "Starter champion",
  ),
  TIGER(
    title = "Tiger",
    tagline = "Solar Flame Striker",
    accentHex = 0xFFFF7A00,
    glowHex = 0xFFFFAB40,
    secondaryHex = 0xFFFFE082,
    unlockedByDefault = false,
    unlockHint = "Win 3 matches to unlock",
  ),
  EAGLE(
    title = "Eagle",
    tagline = "Sky Talon Tactician",
    accentHex = 0xFF00E5FF,
    glowHex = 0xFF80F5FF,
    secondaryHex = 0xFFFFFFFF,
    unlockedByDefault = false,
    unlockHint = "Defeat Medium AI or higher",
  ),
  FOX(
    title = "Fox",
    tagline = "Crimson Mirage Trickster",
    accentHex = 0xFFFF4D6D,
    glowHex = 0xFFFF8FA3,
    secondaryHex = 0xFFFFD6E0,
    unlockedByDefault = false,
    unlockHint = "Complete 5 matches to unlock",
  ),
  PANDA(
    title = "Panda",
    tagline = "Emerald Zen Guardian",
    accentHex = 0xFF00DFA5,
    glowHex = 0xFF69F0AE,
    secondaryHex = 0xFFFFFFFF,
    unlockedByDefault = false,
    unlockHint = "Win on a 5×5, 7×7, or 9×9 board",
  ),
  BEAR(
    title = "Bear",
    tagline = "Violet Titan Vanguard",
    accentHex = 0xFF8747FF,
    glowHex = 0xFFB388FF,
    secondaryHex = 0xFFE6D4FF,
    unlockedByDefault = false,
    unlockHint = "Reach a 3-match win streak",
  ),
  DRAGON(
    title = "Dragon",
    tagline = "Plasma Storm Legend",
    accentHex = 0xFFE040FB,
    glowHex = 0xFFFF80AB,
    secondaryHex = 0xFF00E5FF,
    unlockedByDefault = false,
    unlockHint = "Defeat Hard or Expert AI",
  );

  companion object {
    val defaultUnlocked: Set<AnimalType> =
      entries.filter { it.unlockedByDefault }.toSet()
  }
}

enum class AchievementId(
  val title: String,
  val description: String,
  val rewardAnimal: AnimalType?,
) {
  FIRST_VICTORY(
    title = "First Roar",
    description = "Win your very first Animal Battle match.",
    rewardAnimal = null,
  ),
  JUNGLE_CHALLENGER(
    title = "Jungle Challenger",
    description = "Win 3 matches across any mode or board size.",
    rewardAnimal = AnimalType.TIGER,
  ),
  SKY_STRIKER(
    title = "Sky Striker",
    description = "Defeat Medium AI or higher in single-player.",
    rewardAnimal = AnimalType.EAGLE,
  ),
  PACK_VETERAN(
    title = "Arena Veteran",
    description = "Complete 5 total battles in the arena.",
    rewardAnimal = AnimalType.FOX,
  ),
  GIANT_CONQUEROR(
    title = "Territory Master",
    description = "Win a match on a 5×5, 7×7, or 9×9 board.",
    rewardAnimal = AnimalType.PANDA,
  ),
  ALPHA_STREAK(
    title = "Alpha Streak",
    description = "Achieve a 3-match winning streak.",
    rewardAnimal = AnimalType.BEAR,
  ),
  DRAGON_ASCENDANT(
    title = "Dragon Ascendant",
    description = "Defeat Hard or Expert AI in battle.",
    rewardAnimal = AnimalType.DRAGON,
  ),
  APEX_LEGEND(
    title = "Apex Legend",
    description = "Win 10 total matches or conquer Expert AI.",
    rewardAnimal = null,
  ),
}

enum class BoardSize(
  val gridSize: Int,
  val winLength: Int,
  val title: String,
  val ruleLabel: String,
) {
  CLASSIC_3X3(3, 3, "Classic 3×3", "Connect 3"),
  MEDIUM_5X5(5, 4, "Medium 5×5", "Connect 4"),
  LARGE_7X7(7, 4, "Large 7×7", "Connect 4"),
  EXPERT_9X9(9, 5, "Expert 9×9", "Connect 5");

  val totalCells: Int
    get() = gridSize * gridSize

  companion object {
    fun fromGridSize(size: Int): BoardSize =
      entries.firstOrNull { it.gridSize == size } ?: CLASSIC_3X3
  }
}

enum class PlayMode(
  val title: String,
  val subtitle: String,
  val isAiMode: Boolean,
  val fixedBoardSize: BoardSize? = null,
) {
  LION_VS_WOLF_TWO_PLAYER(
    title = "Lion vs Wolf – Two Player",
    subtitle = "Classic local pass & play animal showdown",
    isAiMode = false,
  ),
  LION_VS_AI(
    title = "Lion vs AI – Single Player",
    subtitle = "Challenge adaptive jungle AI opponents",
    isAiMode = true,
  ),
  ANIMAL_BATTLE_MULTIPLAYER(
    title = "Animal Battle – Multiplayer",
    subtitle = "Custom animals in Best-of-3 & Best-of-5 series",
    isAiMode = false,
  ),
  CLASSIC_3X3(
    title = "Classic 3×3",
    subtitle = "Fast Connect-3 tactical showdown",
    isAiMode = true,
    fixedBoardSize = BoardSize.CLASSIC_3X3,
  ),
  MEDIUM_5X5(
    title = "Medium 5×5",
    subtitle = "Connect-4 arena with strategic forks",
    isAiMode = true,
    fixedBoardSize = BoardSize.MEDIUM_5X5,
  ),
  LARGE_7X7(
    title = "Large 7×7",
    subtitle = "Connect-4 deep territory battle",
    isAiMode = true,
    fixedBoardSize = BoardSize.LARGE_7X7,
  ),
  EXPERT_9X9(
    title = "Expert 9×9",
    subtitle = "Connect-5 grandmaster jungle grid",
    isAiMode = true,
    fixedBoardSize = BoardSize.EXPERT_9X9,
  ),
}

enum class AIDifficulty(
  val title: String,
  val description: String,
) {
  EASY("Easy", "Playful openings with occasional slips"),
  MEDIUM("Medium", "Blocks threats & seizes clear wins"),
  HARD("Hard", "Alpha-beta search & sharp defense"),
  EXPERT("Expert", "Optimal play & multi-ply threat control"),
}

enum class MatchFormat(
  val roundsToWin: Int,
  val maxRounds: Int,
  val title: String,
) {
  SINGLE_ROUND(1, 1, "Single Match"),
  BEST_OF_3(2, 3, "Best of 3"),
  BEST_OF_5(3, 5, "Best of 5"),
}

enum class NeonThemeStyle(
  val title: String,
  val subtitle: String,
) {
  MIDNIGHT_JUNGLE(
    "Midnight Jungle",
    "Deep Midnight #020B18, Electric Cyan & Golden Amber",
  ),
  EMERALD_CANOPY(
    "Emerald Canopy",
    "Jungle Abyss, Emerald Green & Solar Gold",
  ),
  ELECTRIC_ABYSS(
    "Electric Abyss",
    "Deep Trench, Cyber Cyan & Violet Pulse",
  ),
  ROYAL_SAVANNA(
    "Royal Savanna",
    "Obsidian Night, Golden Amber & Teal Grid",
  ),
}
