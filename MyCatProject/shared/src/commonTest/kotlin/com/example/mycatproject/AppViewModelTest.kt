package com.example.mycatproject

import com.example.mycatproject.domain.model.AppDestination
import com.example.mycatproject.presentation.AppViewModel
import kotlin.test.Test
import kotlin.test.assertEquals

class AppViewModelTest {
    @Test
    fun startClickOpensGamePlay() {
        val viewModel = AppViewModel()

        assertEquals(AppDestination.Start, viewModel.currentDestination)
        viewModel.openGamePlay()
        assertEquals(AppDestination.GamePlay, viewModel.currentDestination)
    }

    @Test
    fun youDieClickReturnsToStart() {
        val viewModel = AppViewModel()
        viewModel.openGamePlay()

        viewModel.openStart()

        assertEquals(AppDestination.Start, viewModel.currentDestination)
    }
}
