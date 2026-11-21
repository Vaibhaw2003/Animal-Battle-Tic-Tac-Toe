package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.neontictactoe.audio.SoundManager
import com.example.neontictactoe.data.GamePreferencesRepository
import com.example.neontictactoe.data.StatisticsRepository
import com.example.neontictactoe.navigation.NeonTicTacToeApp
import com.example.neontictactoe.ui.AppViewModel
import com.example.neontictactoe.ui.game.GameViewModel

class MainActivity : ComponentActivity() {

  private lateinit var appViewModel: AppViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
    )

    val prefsRepo = GamePreferencesRepository(applicationContext)
    val statsRepo = StatisticsRepository(applicationContext)
    val soundManager = SoundManager.getInstance(applicationContext)

    appViewModel =
      ViewModelProvider(
        this,
        AppViewModel.Factory(prefsRepo, statsRepo, soundManager),
      )[AppViewModel::class.java]

    val gameVmFactory = GameViewModel.Factory(statsRepo, appViewModel.soundFeedback)

    setContent {
      NeonTicTacToeApp(
        appViewModel = appViewModel,
        gameViewModelFactory = gameVmFactory,
      )
    }
  }

  override fun onStart() {
    super.onStart()
    if (::appViewModel.isInitialized) {
      appViewModel.onAppForeground()
    }
  }

  override fun onStop() {
    super.onStop()
    if (::appViewModel.isInitialized) {
      appViewModel.onAppBackground()
    }
  }
}
