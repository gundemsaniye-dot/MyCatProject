package com.example.mycatproject.presentation.start

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.mycatproject.domain.usecase.CreateStartCatLayoutUseCase

class StartScreenViewModel(
    createStartCatLayout: CreateStartCatLayoutUseCase = CreateStartCatLayoutUseCase(),
) : ViewModel() {
    var uiState by mutableStateOf(
        StartScreenUiState(cats = createStartCatLayout()),
    )
        private set
}
