package com.example.mycatproject.presentation.start

import com.example.mycatproject.domain.model.StartCat

data class StartScreenUiState(
    val cats: List<StartCat> = emptyList(),
)
