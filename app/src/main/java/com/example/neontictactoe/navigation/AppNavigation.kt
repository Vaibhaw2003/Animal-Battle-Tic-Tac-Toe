package com.example.neontictactoe.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.neontictactoe.domain.PlayMode
import com.example.neontictactoe.domain.PlayerSlot
import com.example.neontictactoe.ui.AppViewModel
import com.example.neontictactoe.ui.achievements.AchievementsScreen
import com.example.neontictactoe.ui.animals.AnimalSelectionScreen
import com.example.neontictactoe.ui.game.GameScreen
import com.example.neontictactoe.ui.game.GameViewModel
import com.example.neontictactoe.ui.home.HomeScreen
import com.example.neontictactoe.ui.mode.GameModeScreen
import com.example.neontictactoe.ui.player.PlayerSetupScreen
import com.example.neontictactoe.ui.settings.SettingsScreen
import com.example.neontictactoe.ui.splash.SplashScreen
import com.example.neontictactoe.ui.statistics.StatisticsScreen
import com.example.neontictactoe.ui.theme.NeonTicTacToeTheme

object AppRoutes {
  const val ROUTE_SPLASH = "splash"
  const val ROUTE_HOME = "home"
  const val ROUTE_GAME_MODES = "game_modes"
  const val ROUTE_ANIMAL_SELECTION = "animal_selection"
  const val ROUTE_PLAYER_SETUP = "player_setup"
  const val ROUTE_GAME = "game"
  const val ROUTE_STATISTICS = "statistics"
  const val ROUTE_ACHIEVEMENTS = "achievements"
  const val ROUTE_SETTINGS = "settings"
}

@Composable
fun NeonTicTacToeApp(
  appViewModel: AppViewModel,
  gameViewModelFactory: GameViewModel.Factory,
  navController: NavHostController = rememberNavController(),
  startDestination: String = AppRoutes.ROUTE_SPLASH,
) {
  val preferences by appViewModel.preferences.collectAsStateWithLifecycle()
  val statistics by appViewModel.statistics.collectAsStateWithLifecycle()
  val matchSetup by appViewModel.matchSetup.collectAsStateWithLifecycle()
  val gameViewModel: GameViewModel = viewModel(factory = gameViewModelFactory)
  val gameUiState by gameViewModel.uiState.collectAsStateWithLifecycle()

  var initialAnimalSlot by remember { mutableStateOf(PlayerSlot.PLAYER_1) }

  val safeNavigate: (String) -> Unit = { route ->
    navController.navigate(route) { launchSingleTop = true }
  }
  val safePopBack: () -> Unit = {
    if (navController.previousBackStackEntry != null) {
      navController.popBackStack()
    }
  }

  NeonTicTacToeTheme(themeStyle = preferences.themeStyle) {
    NavHost(
      navController = navController,
      startDestination = startDestination,
      enterTransition = {
        fadeIn(animationSpec = tween(240, easing = FastOutSlowInEasing)) +
          scaleIn(initialScale = 0.96f, animationSpec = tween(240, easing = FastOutSlowInEasing))
      },
      exitTransition = {
        fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
      },
      popEnterTransition = {
        fadeIn(animationSpec = tween(240, easing = FastOutSlowInEasing))
      },
      popExitTransition = {
        fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
          scaleOut(targetScale = 0.96f, animationSpec = tween(180, easing = FastOutSlowInEasing))
      },
    ) {
      composable(AppRoutes.ROUTE_SPLASH) {
        SplashScreen(
          onSplashFinished = {
            navController.navigate(AppRoutes.ROUTE_HOME) {
              popUpTo(AppRoutes.ROUTE_SPLASH) { inclusive = true }
              launchSingleTop = true
            }
          }
        )
      }

      composable(AppRoutes.ROUTE_HOME) {
        HomeScreen(
          soundEnabled = preferences.soundEnabled,
          player1Animal = matchSetup.player1Animal,
          player2Animal = matchSetup.player2Animal,
          onToggleSound = { appViewModel.toggleSound() },
          onPlayGameClick = {
            appViewModel.setQuickPlayConfig()
            gameViewModel.startNewMatch(appViewModel.matchSetup.value)
            safeNavigate(AppRoutes.ROUTE_GAME)
          },
          onPlayVsAiClick = {
            appViewModel.soundFeedback.playButtonClick()
            appViewModel.selectPlayMode(PlayMode.LION_VS_AI)
            safeNavigate(AppRoutes.ROUTE_PLAYER_SETUP)
          },
          onTwoPlayerClick = {
            appViewModel.soundFeedback.playButtonClick()
            appViewModel.selectPlayMode(PlayMode.LION_VS_WOLF_TWO_PLAYER)
            safeNavigate(AppRoutes.ROUTE_PLAYER_SETUP)
          },
          onGameModesClick = {
            appViewModel.soundFeedback.playButtonClick()
            safeNavigate(AppRoutes.ROUTE_GAME_MODES)
          },
          onAnimalSelectionClick = {
            appViewModel.soundFeedback.playButtonClick()
            initialAnimalSlot = PlayerSlot.PLAYER_1
            safeNavigate(AppRoutes.ROUTE_ANIMAL_SELECTION)
          },
          onStatisticsClick = {
            appViewModel.soundFeedback.playButtonClick()
            safeNavigate(AppRoutes.ROUTE_STATISTICS)
          },
          onAchievementsClick = {
            appViewModel.soundFeedback.playButtonClick()
            safeNavigate(AppRoutes.ROUTE_ACHIEVEMENTS)
          },
          onSettingsClick = {
            appViewModel.soundFeedback.playButtonClick()
            safeNavigate(AppRoutes.ROUTE_SETTINGS)
          },
        )
      }

      composable(AppRoutes.ROUTE_GAME_MODES) {
        GameModeScreen(
          selectedMode = matchSetup.playMode,
          selectedBoardSize = matchSetup.boardSize,
          onSelectMode = { mode ->
            appViewModel.soundFeedback.playButtonClick()
            appViewModel.selectPlayMode(mode)
          },
          onSelectBoardSize = { size ->
            appViewModel.soundFeedback.playButtonClick()
            appViewModel.updateSetupBoardSize(size)
          },
          onContinueClick = {
            appViewModel.soundFeedback.playButtonClick()
            safeNavigate(AppRoutes.ROUTE_PLAYER_SETUP)
          },
          onBackClick = {
            appViewModel.soundFeedback.playButtonClick()
            safePopBack()
          },
        )
      }

      composable(AppRoutes.ROUTE_ANIMAL_SELECTION) {
        AnimalSelectionScreen(
          player1Animal = matchSetup.player1Animal,
          player2Animal = matchSetup.player2Animal,
          unlockedAnimals = statistics.unlockedAnimals,
          initialSlot = initialAnimalSlot,
          onSelectAnimal = { slot, animal ->
            val selected = appViewModel.selectPlayerAnimal(slot, animal)
            if (selected) {
              appViewModel.soundFeedback.playAnimalSelect()
            } else {
              appViewModel.soundFeedback.playButtonClick()
            }
            selected
          },
          onContinueToSetup = {
            appViewModel.soundFeedback.playButtonClick()
            safeNavigate(AppRoutes.ROUTE_PLAYER_SETUP)
          },
          onBackClick = {
            appViewModel.soundFeedback.playButtonClick()
            safePopBack()
          },
        )
      }

      composable(AppRoutes.ROUTE_PLAYER_SETUP) {
        PlayerSetupScreen(
          config = matchSetup,
          onPlayer1NameChange = { appViewModel.updateSetupPlayer1Name(it) },
          onPlayer2NameChange = { appViewModel.updateSetupPlayer2Name(it) },
          onToggleAiOpponent = { isAi ->
            appViewModel.soundFeedback.playButtonClick()
            appViewModel.updateSetupAiOpponent(isAi)
          },
          onSelectAnimalForSlot = { slot ->
            appViewModel.soundFeedback.playButtonClick()
            initialAnimalSlot = slot
            safeNavigate(AppRoutes.ROUTE_ANIMAL_SELECTION)
          },
          onDifficultySelected = { diff ->
            appViewModel.soundFeedback.playButtonClick()
            appViewModel.updateSetupDifficulty(diff)
          },
          onBoardSizeSelected = { size ->
            appViewModel.soundFeedback.playButtonClick()
            appViewModel.updateSetupBoardSize(size)
          },
          onMatchFormatSelected = { format ->
            appViewModel.soundFeedback.playButtonClick()
            appViewModel.updateSetupMatchFormat(format)
          },
          onStartMatchClick = {
            appViewModel.persistSetupNamesAndDefaults()
            gameViewModel.startNewMatch(matchSetup)
            safeNavigate(AppRoutes.ROUTE_GAME)
          },
          onBackClick = {
            appViewModel.soundFeedback.playButtonClick()
            safePopBack()
          },
        )
      }

      composable(AppRoutes.ROUTE_GAME) {
        GameScreen(
          uiState = gameUiState,
          soundEnabled = preferences.soundEnabled,
          onToggleSound = { appViewModel.toggleSound() },
          onCellClick = { index -> gameViewModel.onCellClicked(index) },
          onRestartClick = {
            gameViewModel.resetGame()
          },
          onNextRoundClick = {
            gameViewModel.startNextRound()
          },
          onSettingsClick = {
            appViewModel.soundFeedback.playButtonClick()
            safeNavigate(AppRoutes.ROUTE_SETTINGS)
          },
          onHomeClick = {
            appViewModel.soundFeedback.playButtonClick()
            navController.popBackStack(AppRoutes.ROUTE_HOME, inclusive = false)
          },
          onBackClick = {
            appViewModel.soundFeedback.playButtonClick()
            safePopBack()
          },
        )
      }

      composable(AppRoutes.ROUTE_STATISTICS) {
        StatisticsScreen(
          stats = statistics,
          onResetStatistics = {
            appViewModel.soundFeedback.playButtonClick()
            appViewModel.resetStatistics()
          },
          onBackClick = {
            appViewModel.soundFeedback.playButtonClick()
            safePopBack()
          },
        )
      }

      composable(AppRoutes.ROUTE_ACHIEVEMENTS) {
        AchievementsScreen(
          statistics = statistics,
          onSelectAnimalsClick = {
            appViewModel.soundFeedback.playButtonClick()
            initialAnimalSlot = PlayerSlot.PLAYER_1
            safeNavigate(AppRoutes.ROUTE_ANIMAL_SELECTION)
          },
          onBackClick = {
            appViewModel.soundFeedback.playButtonClick()
            safePopBack()
          },
        )
      }

      composable(AppRoutes.ROUTE_SETTINGS) {
        SettingsScreen(
          preferences = preferences,
          onSoundToggle = { appViewModel.setSoundEnabled(it) },
          onMusicToggle = {
            appViewModel.soundFeedback.playButtonClick()
            appViewModel.setMusicEnabled(it)
          },
          onSfxToggle = { appViewModel.setSfxEnabled(it) },
          onMasterVolumeChange = { appViewModel.setMasterVolume(it) },
          onMusicVolumeChange = { appViewModel.setMusicVolume(it) },
          onSfxVolumeChange = { appViewModel.setSfxVolume(it) },
          onVibrationToggle = {
            appViewModel.soundFeedback.playButtonClick()
            appViewModel.setVibrationEnabled(it)
          },
          onThemeSelected = {
            appViewModel.soundFeedback.playButtonClick()
            appViewModel.setThemeStyle(it)
          },
          onPlayer1NameChange = { appViewModel.setDefaultPlayer1Name(it) },
          onPlayer2NameChange = { appViewModel.setDefaultPlayer2Name(it) },
          onDifficultySelected = {
            appViewModel.soundFeedback.playButtonClick()
            appViewModel.setDefaultDifficulty(it)
          },
          onBoardSizeSelected = {
            appViewModel.soundFeedback.playButtonClick()
            appViewModel.setDefaultBoardSize(it)
          },
          onResetStatistics = {
            appViewModel.soundFeedback.playButtonClick()
            appViewModel.resetStatistics()
          },
          onBackClick = {
            appViewModel.soundFeedback.playButtonClick()
            safePopBack()
          },
        )
      }
    }
  }
}
