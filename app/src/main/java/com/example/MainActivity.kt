package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.neontictactoe.audio.SoundFeedbackManager
import com.example.neontictactoe.data.GamePreferencesRepository
import com.example.neontictactoe.data.StatisticsRepository
import com.example.neontictactoe.navigation.NeonTicTacToeApp
import com.example.neontictactoe.ui.AppViewModel
import com.example.neontictactoe.ui.game.GameViewModel

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
    )

    val prefsRepo = GamePreferencesRepository(applicationContext)
    val statsRepo = StatisticsRepository(applicationContext)
    val soundManager = SoundFeedbackManager(applicationContext)

    val appViewModel =
      ViewModelProvider(
        this,
        AppViewModel.Factory(prefsRepo, statsRepo, soundManager),
      )[AppViewModel::class.java]

    val gameVmFactory = GameViewModel.Factory(statsRepo, soundManager)

    setContent {
      NeonTicTacToeApp(
        appViewModel = appViewModel,
        gameViewModelFactory = gameVmFactory,
      )
    }
  }
}
