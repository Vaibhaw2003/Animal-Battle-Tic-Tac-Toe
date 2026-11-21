package com.example.neontictactoe.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.neontictactoe.audio.AudioSettings
import com.example.neontictactoe.audio.SoundFeedbackController
import com.example.neontictactoe.data.GameStatistics
import com.example.neontictactoe.data.PreferencesRepository
import com.example.neontictactoe.data.StatisticsDataSource
import com.example.neontictactoe.data.UserPreferences
import com.example.neontictactoe.domain.AIDifficulty
import com.example.neontictactoe.domain.AnimalType
import com.example.neontictactoe.domain.BoardSize
import com.example.neontictactoe.domain.MatchFormat
import com.example.neontictactoe.domain.NeonThemeStyle
import com.example.neontictactoe.domain.PlayMode
import com.example.neontictactoe.domain.PlayerSlot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MatchSetupConfig(
  val playMode: PlayMode = PlayMode.LION_VS_AI,
  val boardSize: BoardSize = BoardSize.CLASSIC_3X3,
  val difficulty: AIDifficulty = AIDifficulty.MEDIUM,
  val matchFormat: MatchFormat = MatchFormat.SINGLE_ROUND,
  val player1Name: String = "Player 1",
  val player2Name: String = "Player 2",
  val player1Animal: AnimalType = AnimalType.LION,
  val player2Animal: AnimalType = AnimalType.WOLF,
  val isAiOpponent: Boolean = true,
)

class AppViewModel(
  private val preferencesRepository: PreferencesRepository,
  private val statisticsRepository: StatisticsDataSource,
  val soundFeedback: SoundFeedbackController,
) : ViewModel() {

  private val _preferences = MutableStateFlow(UserPreferences())
  val preferences: StateFlow<UserPreferences> = _preferences.asStateFlow()

  val statistics: StateFlow<GameStatistics> =
    statisticsRepository.statisticsFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = GameStatistics(),
    )

  private val _matchSetup = MutableStateFlow(MatchSetupConfig())
  val matchSetup: StateFlow<MatchSetupConfig> = _matchSetup.asStateFlow()

  private var soundOverridden = false
  private var musicOverridden = false
  private var sfxOverridden = false
  private var masterVolumeOverridden = false
  private var musicVolumeOverridden = false
  private var sfxVolumeOverridden = false
  private var vibrationOverridden = false
  private var themeOverridden = false
  private var p1Overridden = false
  private var p2Overridden = false
  private var p1AnimalOverridden = false
  private var p2AnimalOverridden = false
  private var difficultyOverridden = false
  private var boardSizeOverridden = false
  private var matchFormatOverridden = false

  init {
    viewModelScope.launch {
      val loaded = preferencesRepository.preferencesFlow.first()
      _preferences.update { current ->
        UserPreferences(
          soundEnabled = if (soundOverridden) current.soundEnabled else loaded.soundEnabled,
          musicEnabled = if (musicOverridden) current.musicEnabled else loaded.musicEnabled,
          sfxEnabled = if (sfxOverridden) current.sfxEnabled else loaded.sfxEnabled,
          masterVolume = if (masterVolumeOverridden) current.masterVolume else loaded.masterVolume,
          musicVolume = if (musicVolumeOverridden) current.musicVolume else loaded.musicVolume,
          sfxVolume = if (sfxVolumeOverridden) current.sfxVolume else loaded.sfxVolume,
          vibrationEnabled =
            if (vibrationOverridden) current.vibrationEnabled else loaded.vibrationEnabled,
          themeStyle = if (themeOverridden) current.themeStyle else loaded.themeStyle,
          player1Name = if (p1Overridden) current.player1Name else loaded.player1Name,
          player2Name = if (p2Overridden) current.player2Name else loaded.player2Name,
          player1Animal =
            if (p1AnimalOverridden) current.player1Animal else loaded.player1Animal,
          player2Animal =
            if (p2AnimalOverridden) current.player2Animal else loaded.player2Animal,
          defaultDifficulty =
            if (difficultyOverridden) current.defaultDifficulty else loaded.defaultDifficulty,
          defaultBoardSize =
            if (boardSizeOverridden) current.defaultBoardSize else loaded.defaultBoardSize,
          defaultMatchFormat =
            if (matchFormatOverridden) current.defaultMatchFormat else loaded.defaultMatchFormat,
        )
      }
      val resolved = _preferences.value
      syncSoundSettings(resolved)
      soundFeedback.setVibrationEnabled(resolved.vibrationEnabled)
      if (resolved.soundEnabled && resolved.musicEnabled) {
        soundFeedback.startBackgroundMusic()
      }
      _matchSetup.update { current ->
        current.copy(
          boardSize = if (boardSizeOverridden) current.boardSize else resolved.defaultBoardSize,
          difficulty =
            if (difficultyOverridden) current.difficulty else resolved.defaultDifficulty,
          matchFormat =
            if (matchFormatOverridden) current.matchFormat else resolved.defaultMatchFormat,
          player1Name = if (p1Overridden) current.player1Name else resolved.player1Name,
          player2Name = if (p2Overridden) current.player2Name else resolved.player2Name,
          player1Animal = if (p1AnimalOverridden) current.player1Animal else resolved.player1Animal,
          player2Animal =
            if (p2AnimalOverridden) {
              current.player2Animal
            } else if (resolved.player2Animal == resolved.player1Animal) {
              if (resolved.player1Animal == AnimalType.WOLF) AnimalType.LION else AnimalType.WOLF
            } else {
              resolved.player2Animal
            },
        )
      }
    }
  }

  fun selectPlayMode(mode: PlayMode) {
    val prefs = _preferences.value
    val targetBoardSize = mode.fixedBoardSize ?: _matchSetup.value.boardSize
    val targetFormat =
      when (mode) {
        PlayMode.ANIMAL_BATTLE_MULTIPLAYER ->
          if (_matchSetup.value.matchFormat == MatchFormat.SINGLE_ROUND) MatchFormat.BEST_OF_3
          else _matchSetup.value.matchFormat
        else -> _matchSetup.value.matchFormat
      }
    val p1Animal =
      if (mode == PlayMode.LION_VS_WOLF_TWO_PLAYER) AnimalType.LION
      else _matchSetup.value.player1Animal
    val p2Animal =
      if (mode == PlayMode.LION_VS_WOLF_TWO_PLAYER) AnimalType.WOLF
      else _matchSetup.value.player2Animal

    _matchSetup.update {
      it.copy(
        playMode = mode,
        isAiOpponent = mode.isAiMode,
        boardSize = targetBoardSize,
        matchFormat = targetFormat,
        player1Name = it.player1Name.ifBlank { prefs.player1Name },
        player2Name = it.player2Name.ifBlank { prefs.player2Name },
        player1Animal = p1Animal,
        player2Animal =
          if (p2Animal == p1Animal) {
            if (p1Animal == AnimalType.WOLF) AnimalType.LION else AnimalType.WOLF
          } else p2Animal,
      )
    }
  }

  fun setQuickPlayConfig() {
    val prefs = _preferences.value
    val p1 = prefs.player1Animal
    val p2 =
      if (prefs.player2Animal == p1) {
        if (p1 == AnimalType.WOLF) AnimalType.LION else AnimalType.WOLF
      } else {
        prefs.player2Animal
      }
    _matchSetup.update {
      it.copy(
        playMode = PlayMode.LION_VS_AI,
        isAiOpponent = true,
        boardSize = prefs.defaultBoardSize,
        difficulty = prefs.defaultDifficulty,
        matchFormat = MatchFormat.SINGLE_ROUND,
        player1Name = prefs.player1Name,
        player2Name = prefs.player2Name,
        player1Animal = p1,
        player2Animal = p2,
      )
    }
  }

  fun selectPlayerAnimal(slot: PlayerSlot, animal: AnimalType): Boolean {
    val unlocked = statistics.value.unlockedAnimals
    if (!animal.unlockedByDefault && animal !in unlocked) {
      return false
    }

    p1AnimalOverridden = true
    p2AnimalOverridden = true

    val current1 = _matchSetup.value.player1Animal
    val current2 = _matchSetup.value.player2Animal
    val (next1, next2) =
      when (slot) {
        PlayerSlot.PLAYER_1 -> {
          val swapped2 = if (animal == current2) current1 else current2
          animal to swapped2
        }
        PlayerSlot.PLAYER_2 -> {
          val swapped1 = if (animal == current1) current2 else current1
          swapped1 to animal
        }
      }

    _matchSetup.update { it.copy(player1Animal = next1, player2Animal = next2) }
    _preferences.update { it.copy(player1Animal = next1, player2Animal = next2) }
    viewModelScope.launch {
      preferencesRepository.setPlayer1Animal(next1)
      preferencesRepository.setPlayer2Animal(next2)
    }
    return true
  }

  fun updateSetupBoardSize(size: BoardSize) {
    boardSizeOverridden = true
    _matchSetup.update { it.copy(boardSize = size) }
  }

  fun updateSetupDifficulty(difficulty: AIDifficulty) {
    difficultyOverridden = true
    _matchSetup.update { it.copy(difficulty = difficulty) }
  }

  fun updateSetupMatchFormat(format: MatchFormat) {
    matchFormatOverridden = true
    _matchSetup.update { it.copy(matchFormat = format) }
  }

  fun updateSetupAiOpponent(isAi: Boolean) {
    _matchSetup.update { it.copy(isAiOpponent = isAi) }
  }

  fun updateSetupPlayer1Name(name: String) {
    p1Overridden = true
    _matchSetup.update { it.copy(player1Name = name.take(16)) }
  }

  fun updateSetupPlayer2Name(name: String) {
    p2Overridden = true
    _matchSetup.update { it.copy(player2Name = name.take(16)) }
  }

  fun persistSetupNamesAndDefaults() {
    val current = _matchSetup.value
    val p1Clean = current.player1Name.trim().ifBlank { "Player 1" }
    val p2Clean = current.player2Name.trim().ifBlank { "Player 2" }
    p1Overridden = true
    if (!current.isAiOpponent) p2Overridden = true
    p1AnimalOverridden = true
    p2AnimalOverridden = true
    difficultyOverridden = true
    boardSizeOverridden = true
    matchFormatOverridden = true
    _preferences.update {
      it.copy(
        player1Name = p1Clean,
        player2Name = if (!current.isAiOpponent) p2Clean else it.player2Name,
        player1Animal = current.player1Animal,
        player2Animal = current.player2Animal,
        defaultDifficulty = current.difficulty,
        defaultBoardSize = current.boardSize,
        defaultMatchFormat = current.matchFormat,
      )
    }
    viewModelScope.launch {
      preferencesRepository.setPlayer1Name(p1Clean)
      if (!current.isAiOpponent) {
        preferencesRepository.setPlayer2Name(p2Clean)
      }
      preferencesRepository.setPlayer1Animal(current.player1Animal)
      preferencesRepository.setPlayer2Animal(current.player2Animal)
      preferencesRepository.setDefaultDifficulty(current.difficulty)
      preferencesRepository.setDefaultBoardSize(current.boardSize)
      preferencesRepository.setDefaultMatchFormat(current.matchFormat)
    }
  }

  private fun syncSoundSettings(prefs: UserPreferences) {
    soundFeedback.updateAudioSettings(
      AudioSettings(
        soundEnabled = prefs.soundEnabled,
        musicEnabled = prefs.musicEnabled,
        sfxEnabled = prefs.sfxEnabled,
        masterVolume = prefs.masterVolume,
        musicVolume = prefs.musicVolume,
        sfxVolume = prefs.sfxVolume,
      )
    )
  }

  fun onAppForeground() {
    soundFeedback.onAppForeground()
  }

  fun onAppBackground() {
    soundFeedback.onAppBackground()
  }

  fun toggleSound() {
    setSoundEnabled(!_preferences.value.soundEnabled)
  }

  fun setSoundEnabled(enabled: Boolean) {
    soundOverridden = true
    _preferences.update { it.copy(soundEnabled = enabled) }
    soundFeedback.setSoundEnabled(enabled)
    if (enabled && _preferences.value.sfxEnabled) {
      soundFeedback.playButtonClick()
    }
    viewModelScope.launch { preferencesRepository.setSoundEnabled(enabled) }
  }

  fun setMusicEnabled(enabled: Boolean) {
    musicOverridden = true
    _preferences.update { it.copy(musicEnabled = enabled) }
    soundFeedback.setMusicEnabled(enabled)
    viewModelScope.launch { preferencesRepository.setMusicEnabled(enabled) }
  }

  fun setSfxEnabled(enabled: Boolean) {
    sfxOverridden = true
    _preferences.update { it.copy(sfxEnabled = enabled) }
    soundFeedback.setSfxEnabled(enabled)
    if (enabled && _preferences.value.soundEnabled) {
      soundFeedback.playButtonClick()
    }
    viewModelScope.launch { preferencesRepository.setSfxEnabled(enabled) }
  }

  fun setMasterVolume(volume: Float) {
    val clamped = volume.coerceIn(0f, 1f)
    masterVolumeOverridden = true
    _preferences.update { it.copy(masterVolume = clamped) }
    soundFeedback.setMasterVolume(clamped)
    viewModelScope.launch { preferencesRepository.setMasterVolume(clamped) }
  }

  fun setMusicVolume(volume: Float) {
    val clamped = volume.coerceIn(0f, 1f)
    musicVolumeOverridden = true
    _preferences.update { it.copy(musicVolume = clamped) }
    soundFeedback.setMusicVolume(clamped)
    viewModelScope.launch { preferencesRepository.setMusicVolume(clamped) }
  }

  fun setSfxVolume(volume: Float) {
    val clamped = volume.coerceIn(0f, 1f)
    sfxVolumeOverridden = true
    _preferences.update { it.copy(sfxVolume = clamped) }
    soundFeedback.setSfxVolume(clamped)
    viewModelScope.launch { preferencesRepository.setSfxVolume(clamped) }
  }

  fun setVibrationEnabled(enabled: Boolean) {
    vibrationOverridden = true
    _preferences.update { it.copy(vibrationEnabled = enabled) }
    soundFeedback.setVibrationEnabled(enabled)
    viewModelScope.launch { preferencesRepository.setVibrationEnabled(enabled) }
  }

  fun setThemeStyle(style: NeonThemeStyle) {
    themeOverridden = true
    _preferences.update { it.copy(themeStyle = style) }
    viewModelScope.launch { preferencesRepository.setThemeStyle(style) }
  }

  fun setDefaultPlayer1Name(name: String) {
    val clean = name.trim().ifBlank { "Player 1" }.take(16)
    p1Overridden = true
    _preferences.update { it.copy(player1Name = clean) }
    _matchSetup.update { it.copy(player1Name = clean) }
    viewModelScope.launch { preferencesRepository.setPlayer1Name(clean) }
  }

  fun setDefaultPlayer2Name(name: String) {
    val clean = name.trim().ifBlank { "Player 2" }.take(16)
    p2Overridden = true
    _preferences.update { it.copy(player2Name = clean) }
    _matchSetup.update { it.copy(player2Name = clean) }
    viewModelScope.launch { preferencesRepository.setPlayer2Name(clean) }
  }

  fun setDefaultDifficulty(difficulty: AIDifficulty) {
    difficultyOverridden = true
    _preferences.update { it.copy(defaultDifficulty = difficulty) }
    _matchSetup.update { it.copy(difficulty = difficulty) }
    viewModelScope.launch { preferencesRepository.setDefaultDifficulty(difficulty) }
  }

  fun setDefaultBoardSize(boardSize: BoardSize) {
    boardSizeOverridden = true
    _preferences.update { it.copy(defaultBoardSize = boardSize) }
    _matchSetup.update { it.copy(boardSize = boardSize) }
    viewModelScope.launch { preferencesRepository.setDefaultBoardSize(boardSize) }
  }

  fun resetStatistics() {
    p1AnimalOverridden = true
    p2AnimalOverridden = true
    _preferences.update {
      it.copy(player1Animal = AnimalType.LION, player2Animal = AnimalType.WOLF)
    }
    _matchSetup.update {
      it.copy(player1Animal = AnimalType.LION, player2Animal = AnimalType.WOLF)
    }
    viewModelScope.launch {
      statisticsRepository.resetStatistics()
      preferencesRepository.setPlayer1Animal(AnimalType.LION)
      preferencesRepository.setPlayer2Animal(AnimalType.WOLF)
    }
  }

  override fun onCleared() {
    super.onCleared()
    soundFeedback.release()
  }

  class Factory(
    private val preferencesRepository: PreferencesRepository,
    private val statisticsRepository: StatisticsDataSource,
    private val soundFeedback: SoundFeedbackController,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return AppViewModel(preferencesRepository, statisticsRepository, soundFeedback) as T
    }
  }
}
