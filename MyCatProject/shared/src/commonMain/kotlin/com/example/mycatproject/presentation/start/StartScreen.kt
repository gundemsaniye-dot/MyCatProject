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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mycatproject.domain.model.StartCat
import mycatproject.shared.generated.resources.Res
import mycatproject.shared.generated.resources.chewy_regular
import mycatproject.shared.generated.resources.start_screen_cat_image_1
import mycatproject.shared.generated.resources.start_screen_cat_image_2
import mycatproject.shared.generated.resources.start_screen_cat_image_3
import mycatproject.shared.generated.resources.start_screen_cat_image_4
import mycatproject.shared.generated.resources.start_screen_cat_image_5
import mycatproject.shared.generated.resources.start_screen_cat_image_6
import mycatproject.shared.generated.resources.start_screen_cat_image_7
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.Font
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
            ),
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
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .zIndex(1f),
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

        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(10f)
                .background(Color.Black.copy(alpha = 0.20f)),
        )

        BoxWithConstraints(
            modifier = stageModifier
                .align(Alignment.Center)
                .zIndex(20f),
        ) {
            val chewyFont = FontFamily(
                Font(
                    resource = Res.font.chewy_regular,
                    weight = FontWeight.Normal,
                ),
            )
            val isCompactStage = maxWidth < 420.dp
            val youCanStyle = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = chewyFont,
                fontSize = if (isCompactStage) 40.sp else 48.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = if (isCompactStage) 42.sp else 50.sp,
                letterSpacing = 0.5.sp,
                shadow = CTA_TEXT_SHADOW,
            )
            val startStyle = MaterialTheme.typography.displayMedium.copy(
                fontFamily = chewyFont,
                fontSize = if (isCompactStage) 68.sp else 82.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = if (isCompactStage) 70.sp else 84.sp,
                letterSpacing = 1.5.sp,
                shadow = CTA_TEXT_SHADOW,
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = maxHeight * 0.23f)
                    .zIndex(20f)
                    .clickable(onClick = onStartClick)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "You Can",
                        modifier = Modifier.offset(x = 2.dp, y = 4.dp),
                        color = Color.Black.copy(alpha = 0.70f),
                        style = youCanStyle,
                    )
                    Text(
                        text = "You Can",
                        style = youCanStyle.copy(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFFF3B0),
                                Color(0xFFFFB347),
                                Color(0xFFFF6B6B),
                            ),
                        ),
                    ),
                    )
                }
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "START",
                        modifier = Modifier.offset(x = 3.dp, y = 5.dp),
                        color = Color.Black.copy(alpha = 0.70f),
                        style = startStyle,
                    )
                    Text(
                        text = "START",
                        style = startStyle.copy(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFFD166),
                                Color(0xFFFF5C8A),
                                Color(0xFFB84CFF),
                            ),
                        ),
                    ),
                    )
                }
            }
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
    val size = shortestSide * cat.sizeFraction
    val x = containerWidth * cat.horizontalFraction - size / 2
    val y = containerHeight * cat.verticalFraction - size / 2
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
private val CTA_TEXT_SHADOW = Shadow(
    color = Color(0x99000000),
    offset = Offset(0f, 5f),
    blurRadius = 10f,
)
