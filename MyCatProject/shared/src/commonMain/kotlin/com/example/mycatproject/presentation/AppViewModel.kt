package com.example.mycatproject.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.mycatproject.domain.model.AppDestination

class AppViewModel : ViewModel() {
    var currentDestination by mutableStateOf(AppDestination.Start)
        private set

    fun openGamePlay() {
        currentDestination = AppDestination.GamePlay
    }
}
