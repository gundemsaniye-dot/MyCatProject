package com.example.mycatproject.presentation.start

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mycatproject.domain.model.StartCat
import mycatproject.shared.generated.resources.Res
import mycatproject.shared.generated.resources.start_screen_cat_image_1
import mycatproject.shared.generated.resources.start_screen_cat_image_2
import mycatproject.shared.generated.resources.start_screen_cat_image_3
import mycatproject.shared.generated.resources.start_screen_cat_image_4
import mycatproject.shared.generated.resources.start_screen_cat_image_5
import mycatproject.shared.generated.resources.start_screen_cat_image_6
import mycatproject.shared.generated.resources.start_screen_cat_image_7
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun StartScreenRoute(
    onStartClick: () -> Unit,
    viewModel: StartScreenViewModel = viewModel { StartScreenViewModel() },
) {
    StartScreen(
        uiState = viewModel.uiState,
        onStartClick = onStartClick,
    )
}

@Composable
fun StartScreen(
    uiState: StartScreenUiState,
    onStartClick: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF7EA),
                        Color(0xFFFFE7D1),
                        Color(0xFFF7C9B6),
                    ),
                ),
            )
            .safeContentPadding(),
    ) {
        val containerRatio = maxWidth.value / maxHeight.value
        val stageModifier = if (containerRatio > MOBILE_STAGE_RATIO) {
            Modifier
                .fillMaxHeight()
                .aspectRatio(MOBILE_STAGE_RATIO)
        } else {
            Modifier
                .fillMaxWidth()
                .aspectRatio(MOBILE_STAGE_RATIO)
        }

        BoxWithConstraints(
            modifier = stageModifier.align(Alignment.Center),
        ) {
            val shortestSide = minOf(maxWidth, maxHeight)

            uiState.cats.forEach { cat ->
                AnimatedCat(
                    cat = cat,
                    containerWidth = maxWidth,
                    containerHeight = maxHeight,
                    shortestSide = shortestSide,
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10f)
                    .background(Color.Black.copy(alpha = 0.30f)),
            )

            Text(
                text = "You Can Start",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = maxHeight * 0.30f - 24.dp)
                    .zIndex(20f)
                    .clickable(onClick = onStartClick)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = if (maxWidth < 420.dp) 25.sp else 32.sp,
                    fontWeight = FontWeight.Black,
                ),
            )
        }
    }
}

@Composable
private fun AnimatedCat(
    cat: StartCat,
    containerWidth: Dp,
    containerHeight: Dp,
    shortestSide: Dp,
) {
    val size = (shortestSide * cat.sizeFraction).coerceIn(76.dp, 220.dp)
    val x = (containerWidth - size) * cat.horizontalFraction
    val y = (containerHeight - size) * cat.verticalFraction
    val density = LocalDensity.current
    val bounceDistancePx = with(density) { (size * cat.bounceFraction).toPx() }
    val transition = rememberInfiniteTransition(label = "cat-${cat.id}")
    val bounce = transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = cat.animationDurationMillis,
                delayMillis = cat.animationDelayMillis,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "cat-bounce-${cat.id}",
    )

    Image(
        painter = painterResource(cat.drawableResource()),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .offset(x = x, y = y)
            .size(size)
            .zIndex(1f + cat.verticalFraction)
            .graphicsLayer {
                this.alpha = cat.opacity
                translationY = bounce.value * bounceDistancePx
                rotationZ = cat.rotationDegrees + bounce.value * 3.5f
                scaleX = if (cat.mirrorHorizontally) -1f else 1f
                scaleY = 1f + bounce.value * 0.025f
            },
    )
}

private fun StartCat.drawableResource(): DrawableResource = when (imageIndex) {
    0 -> Res.drawable.start_screen_cat_image_1
    1 -> Res.drawable.start_screen_cat_image_2
    2 -> Res.drawable.start_screen_cat_image_3
    3 -> Res.drawable.start_screen_cat_image_4
    4 -> Res.drawable.start_screen_cat_image_5
    5 -> Res.drawable.start_screen_cat_image_6
    else -> Res.drawable.start_screen_cat_image_7
}

private const val MOBILE_STAGE_RATIO = 9f / 16f
