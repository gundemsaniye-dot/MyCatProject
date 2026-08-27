package com.example.mycatproject

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mycatproject.domain.model.AppDestination
import com.example.mycatproject.presentation.AppViewModel
import com.example.mycatproject.presentation.gameplay.GamePlayScreen
import com.example.mycatproject.presentation.start.StartScreenRoute

@Composable
@Preview
fun App() {
    val appViewModel = viewModel { AppViewModel() }

    MaterialTheme {
        when (appViewModel.currentDestination) {
            AppDestination.Start -> StartScreenRoute(
                onStartClick = appViewModel::openGamePlay,
            )

            AppDestination.GamePlay -> GamePlayScreen()
        }
    }
}
