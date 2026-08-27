package com.example.mycatproject.domain.model

data class StartCat(
    val id: Int,
    val imageIndex: Int,
    val horizontalFraction: Float,
    val verticalFraction: Float,
    val sizeFraction: Float,
    val opacity: Float,
    val rotationDegrees: Float,
    val bounceFraction: Float,
    val animationDurationMillis: Int,
    val animationDelayMillis: Int,
    val mirrorHorizontally: Boolean,
)
