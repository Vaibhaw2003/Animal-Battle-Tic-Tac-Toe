package com.example.neontictactoe.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.neontictactoe.domain.AIDifficulty
import com.example.neontictactoe.domain.AnimalType
import com.example.neontictactoe.domain.BoardSize
import com.example.neontictactoe.domain.MatchFormat
import com.example.neontictactoe.domain.NeonThemeStyle
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.gamePrefsDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "animal_battle_preferences")

data class UserPreferences(
  val soundEnabled: Boolean = true,
  val musicEnabled: Boolean = true,
  val sfxEnabled: Boolean = true,
  val masterVolume: Float = 1.0f,
  val musicVolume: Float = 0.55f,
  val sfxVolume: Float = 1.0f,
  val vibrationEnabled: Boolean = true,
  val themeStyle: NeonThemeStyle = NeonThemeStyle.MIDNIGHT_JUNGLE,
  val player1Name: String = "Player 1",
  val player2Name: String = "Player 2",
  val player1Animal: AnimalType = AnimalType.LION,
  val player2Animal: AnimalType = AnimalType.WOLF,
  val defaultDifficulty: AIDifficulty = AIDifficulty.MEDIUM,
  val defaultBoardSize: BoardSize = BoardSize.CLASSIC_3X3,
  val defaultMatchFormat: MatchFormat = MatchFormat.SINGLE_ROUND,
)

interface PreferencesRepository {
  val preferencesFlow: Flow<UserPreferences>

  suspend fun setSoundEnabled(enabled: Boolean)

  suspend fun setMusicEnabled(enabled: Boolean)

  suspend fun setSfxEnabled(enabled: Boolean)

  suspend fun setMasterVolume(volume: Float)

  suspend fun setMusicVolume(volume: Float)

  suspend fun setSfxVolume(volume: Float)

  suspend fun setVibrationEnabled(enabled: Boolean)

  suspend fun setThemeStyle(style: NeonThemeStyle)

  suspend fun setPlayer1Name(name: String)

  suspend fun setPlayer2Name(name: String)

  suspend fun setPlayer1Animal(animal: AnimalType)

  suspend fun setPlayer2Animal(animal: AnimalType)

  suspend fun setDefaultDifficulty(difficulty: AIDifficulty)

  suspend fun setDefaultBoardSize(boardSize: BoardSize)

  suspend fun setDefaultMatchFormat(format: MatchFormat)
}

class GamePreferencesRepository(
  private val dataStore: DataStore<Preferences>,
) : PreferencesRepository {

  constructor(context: Context) : this(context.applicationContext.gamePrefsDataStore)

  private object Keys {
    val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
    val SFX_ENABLED = booleanPreferencesKey("sfx_enabled")
    val MASTER_VOLUME = floatPreferencesKey("master_volume")
    val MUSIC_VOLUME = floatPreferencesKey("music_volume")
    val SFX_VOLUME = floatPreferencesKey("sfx_volume")
    val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
    val THEME_STYLE = stringPreferencesKey("theme_style")
    val PLAYER_1_NAME = stringPreferencesKey("player_1_name")
    val PLAYER_2_NAME = stringPreferencesKey("player_2_name")
    val PLAYER_1_ANIMAL = stringPreferencesKey("player_1_animal")
    val PLAYER_2_ANIMAL = stringPreferencesKey("player_2_animal")
    val DEFAULT_DIFFICULTY = stringPreferencesKey("default_difficulty")
    val DEFAULT_BOARD_SIZE = stringPreferencesKey("default_board_size")
    val DEFAULT_MATCH_FORMAT = stringPreferencesKey("default_match_format")
  }

  override val preferencesFlow: Flow<UserPreferences> =
    dataStore.data
      .catch { exception ->
        if (exception is IOException) {
          emit(emptyPreferences())
        } else {
          throw exception
        }
      }
      .map { prefs ->
        UserPreferences(
          soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
          musicEnabled = prefs[Keys.MUSIC_ENABLED] ?: true,
          sfxEnabled = prefs[Keys.SFX_ENABLED] ?: true,
          masterVolume = (prefs[Keys.MASTER_VOLUME] ?: 1.0f).coerceIn(0f, 1f),
          musicVolume = (prefs[Keys.MUSIC_VOLUME] ?: 0.55f).coerceIn(0f, 1f),
          sfxVolume = (prefs[Keys.SFX_VOLUME] ?: 1.0f).coerceIn(0f, 1f),
          vibrationEnabled = prefs[Keys.VIBRATION_ENABLED] ?: true,
          themeStyle =
            prefs[Keys.THEME_STYLE]?.let { raw ->
              NeonThemeStyle.entries.firstOrNull { it.name == raw }
            } ?: NeonThemeStyle.MIDNIGHT_JUNGLE,
          player1Name = prefs[Keys.PLAYER_1_NAME]?.ifBlank { "Player 1" } ?: "Player 1",
          player2Name = prefs[Keys.PLAYER_2_NAME]?.ifBlank { "Player 2" } ?: "Player 2",
          player1Animal =
            prefs[Keys.PLAYER_1_ANIMAL]?.let { raw ->
              AnimalType.entries.firstOrNull { it.name == raw }
            } ?: AnimalType.LION,
          player2Animal =
            prefs[Keys.PLAYER_2_ANIMAL]?.let { raw ->
              AnimalType.entries.firstOrNull { it.name == raw }
            } ?: AnimalType.WOLF,
          defaultDifficulty =
            prefs[Keys.DEFAULT_DIFFICULTY]?.let { raw ->
              AIDifficulty.entries.firstOrNull { it.name == raw }
            } ?: AIDifficulty.MEDIUM,
          defaultBoardSize =
            prefs[Keys.DEFAULT_BOARD_SIZE]?.let { raw ->
              BoardSize.entries.firstOrNull { it.name == raw }
            } ?: BoardSize.CLASSIC_3X3,
          defaultMatchFormat =
            prefs[Keys.DEFAULT_MATCH_FORMAT]?.let { raw ->
              MatchFormat.entries.firstOrNull { it.name == raw }
            } ?: MatchFormat.SINGLE_ROUND,
        )
      }

  override suspend fun setSoundEnabled(enabled: Boolean) {
    dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
  }

  override suspend fun setMusicEnabled(enabled: Boolean) {
    dataStore.edit { it[Keys.MUSIC_ENABLED] = enabled }
  }

  override suspend fun setSfxEnabled(enabled: Boolean) {
    dataStore.edit { it[Keys.SFX_ENABLED] = enabled }
  }

  override suspend fun setMasterVolume(volume: Float) {
    dataStore.edit { it[Keys.MASTER_VOLUME] = volume.coerceIn(0f, 1f) }
  }

  override suspend fun setMusicVolume(volume: Float) {
    dataStore.edit { it[Keys.MUSIC_VOLUME] = volume.coerceIn(0f, 1f) }
  }

  override suspend fun setSfxVolume(volume: Float) {
    dataStore.edit { it[Keys.SFX_VOLUME] = volume.coerceIn(0f, 1f) }
  }

  override suspend fun setVibrationEnabled(enabled: Boolean) {
    dataStore.edit { it[Keys.VIBRATION_ENABLED] = enabled }
  }

  override suspend fun setThemeStyle(style: NeonThemeStyle) {
    dataStore.edit { it[Keys.THEME_STYLE] = style.name }
  }

  override suspend fun setPlayer1Name(name: String) {
    val sanitized = name.trim().take(16).ifBlank { "Player 1" }
    dataStore.edit { it[Keys.PLAYER_1_NAME] = sanitized }
  }

  override suspend fun setPlayer2Name(name: String) {
    val sanitized = name.trim().take(16).ifBlank { "Player 2" }
    dataStore.edit { it[Keys.PLAYER_2_NAME] = sanitized }
  }

  override suspend fun setPlayer1Animal(animal: AnimalType) {
    dataStore.edit { it[Keys.PLAYER_1_ANIMAL] = animal.name }
  }

  override suspend fun setPlayer2Animal(animal: AnimalType) {
    dataStore.edit { it[Keys.PLAYER_2_ANIMAL] = animal.name }
  }

  override suspend fun setDefaultDifficulty(difficulty: AIDifficulty) {
    dataStore.edit { it[Keys.DEFAULT_DIFFICULTY] = difficulty.name }
  }

  override suspend fun setDefaultBoardSize(boardSize: BoardSize) {
    dataStore.edit { it[Keys.DEFAULT_BOARD_SIZE] = boardSize.name }
  }

  override suspend fun setDefaultMatchFormat(format: MatchFormat) {
    dataStore.edit { it[Keys.DEFAULT_MATCH_FORMAT] = format.name }
  }
}
