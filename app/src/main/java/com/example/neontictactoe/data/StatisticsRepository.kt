package com.example.neontictactoe.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.neontictactoe.domain.AIDifficulty
import com.example.neontictactoe.domain.AchievementId
import com.example.neontictactoe.domain.AnimalType
import com.example.neontictactoe.domain.BoardSize
import com.example.neontictactoe.domain.PlayerSlot
import java.io.IOException
import kotlin.math.max
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.statisticsDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "animal_battle_statistics")

data class GameStatistics(
  val totalGamesPlayed: Int = 0,
  val totalWins: Int = 0,
  val totalLosses: Int = 0,
  val totalDraws: Int = 0,
  val player1Wins: Int = 0,
  val player2Wins: Int = 0,
  val aiWins: Int = 0,
  val currentWinningStreak: Int = 0,
  val bestWinningStreak: Int = 0,
  val mediumPlusAiWins: Int = 0,
  val hardOrExpertAiWins: Int = 0,
  val expertAiWins: Int = 0,
  val largeBoardWins: Int = 0,
  val unlockedAchievements: Set<AchievementId> = emptySet(),
  val unlockedAnimals: Set<AnimalType> = AnimalType.defaultUnlocked,
) {
  val winPercentage: Float
    get() =
      if (totalGamesPlayed <= 0) 0f
      else (totalWins.toFloat() / totalGamesPlayed.toFloat()) * 100f
}

interface StatisticsDataSource {
  val statisticsFlow: Flow<GameStatistics>

  suspend fun recordMatchResult(
    isAiMatch: Boolean,
    winner: PlayerSlot?,
    boardSize: BoardSize = BoardSize.CLASSIC_3X3,
    aiDifficulty: AIDifficulty = AIDifficulty.MEDIUM,
  ): List<AchievementId>

  suspend fun resetStatistics()
}

class StatisticsRepository(
  private val dataStore: DataStore<Preferences>,
) : StatisticsDataSource {

  constructor(context: Context) : this(context.applicationContext.statisticsDataStore)

  private object Keys {
    val TOTAL_GAMES = intPreferencesKey("total_games_played")
    val TOTAL_WINS = intPreferencesKey("total_wins")
    val TOTAL_LOSSES = intPreferencesKey("total_losses")
    val TOTAL_DRAWS = intPreferencesKey("total_draws")
    val PLAYER_1_WINS = intPreferencesKey("player_1_wins")
    val PLAYER_2_WINS = intPreferencesKey("player_2_wins")
    val AI_WINS = intPreferencesKey("ai_wins")
    val CURRENT_STREAK = intPreferencesKey("current_winning_streak")
    val BEST_STREAK = intPreferencesKey("best_winning_streak")
    val MEDIUM_PLUS_AI_WINS = intPreferencesKey("medium_plus_ai_wins")
    val HARD_OR_EXPERT_AI_WINS = intPreferencesKey("hard_or_expert_ai_wins")
    val EXPERT_AI_WINS = intPreferencesKey("expert_ai_wins")
    val LARGE_BOARD_WINS = intPreferencesKey("large_board_wins")
    val UNLOCKED_ACHIEVEMENTS = stringSetPreferencesKey("unlocked_achievements")
    val UNLOCKED_ANIMALS = stringSetPreferencesKey("unlocked_animals")
  }

  override val statisticsFlow: Flow<GameStatistics> =
    dataStore.data
      .catch { exception ->
        if (exception is IOException) {
          emit(emptyPreferences())
        } else {
          throw exception
        }
      }
      .map { prefs ->
        val rawAchievements = prefs[Keys.UNLOCKED_ACHIEVEMENTS] ?: emptySet()
        val parsedAchievements =
          rawAchievements.mapNotNull { name ->
            AchievementId.entries.firstOrNull { it.name == name }
          }.toSet()

        val rawAnimals = prefs[Keys.UNLOCKED_ANIMALS] ?: emptySet()
        val parsedAnimals =
          AnimalType.defaultUnlocked +
            rawAnimals.mapNotNull { name ->
              AnimalType.entries.firstOrNull { it.name == name }
            }

        GameStatistics(
          totalGamesPlayed = prefs[Keys.TOTAL_GAMES] ?: 0,
          totalWins = prefs[Keys.TOTAL_WINS] ?: 0,
          totalLosses = prefs[Keys.TOTAL_LOSSES] ?: 0,
          totalDraws = prefs[Keys.TOTAL_DRAWS] ?: 0,
          player1Wins = prefs[Keys.PLAYER_1_WINS] ?: 0,
          player2Wins = prefs[Keys.PLAYER_2_WINS] ?: 0,
          aiWins = prefs[Keys.AI_WINS] ?: 0,
          currentWinningStreak = prefs[Keys.CURRENT_STREAK] ?: 0,
          bestWinningStreak = prefs[Keys.BEST_STREAK] ?: 0,
          mediumPlusAiWins = prefs[Keys.MEDIUM_PLUS_AI_WINS] ?: 0,
          hardOrExpertAiWins = prefs[Keys.HARD_OR_EXPERT_AI_WINS] ?: 0,
          expertAiWins = prefs[Keys.EXPERT_AI_WINS] ?: 0,
          largeBoardWins = prefs[Keys.LARGE_BOARD_WINS] ?: 0,
          unlockedAchievements = parsedAchievements,
          unlockedAnimals = parsedAnimals,
        )
      }

  override suspend fun recordMatchResult(
    isAiMatch: Boolean,
    winner: PlayerSlot?,
    boardSize: BoardSize,
    aiDifficulty: AIDifficulty,
  ): List<AchievementId> {
    val newlyUnlocked = mutableListOf<AchievementId>()
    dataStore.edit { prefs ->
      val games = (prefs[Keys.TOTAL_GAMES] ?: 0) + 1
      var wins = prefs[Keys.TOTAL_WINS] ?: 0
      var losses = prefs[Keys.TOTAL_LOSSES] ?: 0
      var draws = prefs[Keys.TOTAL_DRAWS] ?: 0
      var p1Wins = prefs[Keys.PLAYER_1_WINS] ?: 0
      var p2Wins = prefs[Keys.PLAYER_2_WINS] ?: 0
      var aiWins = prefs[Keys.AI_WINS] ?: 0
      var streak = prefs[Keys.CURRENT_STREAK] ?: 0
      var bestStreak = prefs[Keys.BEST_STREAK] ?: 0
      var mediumPlusWins = prefs[Keys.MEDIUM_PLUS_AI_WINS] ?: 0
      var hardPlusWins = prefs[Keys.HARD_OR_EXPERT_AI_WINS] ?: 0
      var expertWins = prefs[Keys.EXPERT_AI_WINS] ?: 0
      var largeWins = prefs[Keys.LARGE_BOARD_WINS] ?: 0

      when (winner) {
        PlayerSlot.PLAYER_1 -> {
          wins += 1
          p1Wins += 1
          streak += 1
          bestStreak = max(bestStreak, streak)
          if (boardSize != BoardSize.CLASSIC_3X3) {
            largeWins += 1
          }
          if (isAiMatch) {
            if (aiDifficulty != AIDifficulty.EASY) {
              mediumPlusWins += 1
            }
            if (aiDifficulty == AIDifficulty.HARD || aiDifficulty == AIDifficulty.EXPERT) {
              hardPlusWins += 1
            }
            if (aiDifficulty == AIDifficulty.EXPERT) {
              expertWins += 1
            }
          }
        }
        PlayerSlot.PLAYER_2 -> {
          if (isAiMatch) {
            losses += 1
            aiWins += 1
            streak = 0
          } else {
            wins += 1
            p2Wins += 1
            streak = 0
            if (boardSize != BoardSize.CLASSIC_3X3) {
              largeWins += 1
            }
          }
        }
        null -> {
          draws += 1
          streak = 0
        }
      }

      val existingAchievements =
        (prefs[Keys.UNLOCKED_ACHIEVEMENTS] ?: emptySet())
          .mapNotNull { name -> AchievementId.entries.firstOrNull { it.name == name } }
          .toMutableSet()
      val existingAnimals =
        (AnimalType.defaultUnlocked +
          (prefs[Keys.UNLOCKED_ANIMALS] ?: emptySet()).mapNotNull { name ->
            AnimalType.entries.firstOrNull { it.name == name }
          }).toMutableSet()

      fun checkUnlock(id: AchievementId, condition: Boolean) {
        if (condition && existingAchievements.add(id)) {
          newlyUnlocked.add(id)
          id.rewardAnimal?.let { existingAnimals.add(it) }
        }
      }

      checkUnlock(AchievementId.FIRST_VICTORY, wins >= 1)
      checkUnlock(AchievementId.JUNGLE_CHALLENGER, wins >= 3)
      checkUnlock(AchievementId.SKY_STRIKER, mediumPlusWins >= 1)
      checkUnlock(AchievementId.PACK_VETERAN, games >= 5)
      checkUnlock(AchievementId.GIANT_CONQUEROR, largeWins >= 1)
      checkUnlock(AchievementId.ALPHA_STREAK, bestStreak >= 3)
      checkUnlock(AchievementId.DRAGON_ASCENDANT, hardPlusWins >= 1)
      checkUnlock(AchievementId.APEX_LEGEND, wins >= 10 || expertWins >= 1)

      prefs[Keys.TOTAL_GAMES] = games
      prefs[Keys.TOTAL_WINS] = wins
      prefs[Keys.TOTAL_LOSSES] = losses
      prefs[Keys.TOTAL_DRAWS] = draws
      prefs[Keys.PLAYER_1_WINS] = p1Wins
      prefs[Keys.PLAYER_2_WINS] = p2Wins
      prefs[Keys.AI_WINS] = aiWins
      prefs[Keys.CURRENT_STREAK] = streak
      prefs[Keys.BEST_STREAK] = bestStreak
      prefs[Keys.MEDIUM_PLUS_AI_WINS] = mediumPlusWins
      prefs[Keys.HARD_OR_EXPERT_AI_WINS] = hardPlusWins
      prefs[Keys.EXPERT_AI_WINS] = expertWins
      prefs[Keys.LARGE_BOARD_WINS] = largeWins
      prefs[Keys.UNLOCKED_ACHIEVEMENTS] = existingAchievements.map { it.name }.toSet()
      prefs[Keys.UNLOCKED_ANIMALS] = existingAnimals.map { it.name }.toSet()
    }
    return newlyUnlocked
  }

  override suspend fun resetStatistics() {
    dataStore.edit { it.clear() }
  }
}
