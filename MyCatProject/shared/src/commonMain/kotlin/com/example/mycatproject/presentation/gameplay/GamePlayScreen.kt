package com.example.mycatproject.presentation.gameplay

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import mycatproject.shared.generated.resources.Res
import mycatproject.shared.generated.resources.gameplay_cat_back
import mycatproject.shared.generated.resources.gameplay_cat_front_1
import mycatproject.shared.generated.resources.gameplay_cat_front_2
import mycatproject.shared.generated.resources.gameplay_cat_front_3
import mycatproject.shared.generated.resources.gameplay_cat_front_4
import mycatproject.shared.generated.resources.gameplay_cat_front_5
import mycatproject.shared.generated.resources.gameplay_cat_front_6
import mycatproject.shared.generated.resources.gameplay_cat_mouth_1
import mycatproject.shared.generated.resources.gameplay_cat_mouth_2
import mycatproject.shared.generated.resources.gameplay_cat_mouth_3
import mycatproject.shared.generated.resources.gameplay_comb
import mycatproject.shared.generated.resources.you_die_fullscreen
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

internal const val MAX_FUR_PARTICLES = 800
internal const val MIN_CAT_TURN_DELAY_MS = 3_000L
internal const val MAX_CAT_TURN_DELAY_MS = 10_000L
internal const val CAT_TURN_GRACE_MS = 200L
internal const val MIN_CAT_TURNED_HOLD_MS = 2_000L
internal const val MAX_CAT_TURNED_HOLD_MS = 5_000L
internal const val CAUGHT_BEFORE_DIE_MS = 5_000L
internal const val CAUGHT_MOUTH_INITIAL_SCALE = 2f
internal const val CAUGHT_MOUTH_TARGET_SCALE = 4f

internal enum class GamePlayPhase {
    Playing,
    CatTurnGrace,
    CatTurnedSafe,
    Caught,
    YouDied,
}

@Composable
fun GamePlayScreen(
    onBrushHaptic: () -> Unit = {},
    onCatTurnSound: () -> Unit = {},
    onCaughtSound: () -> Unit = {},
    onYouDieClick: () -> Unit = {},
) {
    var catBounds by remember { mutableStateOf(Rect.Zero) }
    var combPosition by remember { mutableStateOf(Offset.Unspecified) }
    var isBrushing by remember { mutableStateOf(false) }
    var combJitter by remember { mutableStateOf(CombJitter.None) }
    var furParticles by remember { mutableStateOf(emptyList<FurParticle>()) }
    var sceneHeight by remember { mutableStateOf(0f) }
    var distanceSinceLastFur by remember { mutableStateOf(0f) }
    var gamePhase by remember { mutableStateOf(GamePlayPhase.Playing) }
    var frontCatResource by remember { mutableStateOf(FRONT_CAT_RESOURCES.first()) }
    var mouthResource by remember { mutableStateOf(MOUTH_RESOURCES.first()) }
    val currentOnBrushHaptic by rememberUpdatedState(onBrushHaptic)
    val currentOnCatTurnSound by rememberUpdatedState(onCatTurnSound)
    val currentOnCaughtSound by rememberUpdatedState(onCaughtSound)
    val currentGamePhase by rememberUpdatedState(gamePhase)
    val caughtMouthScale by animateFloatAsState(
        targetValue = if (gamePhase == GamePlayPhase.Caught) {
            CAUGHT_MOUTH_TARGET_SCALE
        } else {
            CAUGHT_MOUTH_INITIAL_SCALE
        },
        animationSpec = tween(
            durationMillis = CAUGHT_BEFORE_DIE_MS.toInt(),
            easing = FastOutSlowInEasing,
        ),
        label = "caught-mouth-approach",
    )

    LaunchedEffect(gamePhase) {
        when (gamePhase) {
            GamePlayPhase.Playing -> {
                delay(nextCatTurnDelayMillis())
                frontCatResource = FRONT_CAT_RESOURCES.random()
                gamePhase = GamePlayPhase.CatTurnGrace
            }

            GamePlayPhase.CatTurnGrace -> {
                currentOnCatTurnSound()
                delay(CAT_TURN_GRACE_MS)
                val phaseAfterGrace = phaseAfterTurnGrace(isBrushing)
                if (phaseAfterGrace == GamePlayPhase.Caught) {
                    mouthResource = MOUTH_RESOURCES.random()
                }
                gamePhase = phaseAfterGrace
                combPosition = Offset.Unspecified
                isBrushing = false
            }

            GamePlayPhase.CatTurnedSafe -> {
                delay(nextCatTurnedHoldDelayMillis())
                gamePhase = GamePlayPhase.Playing
            }

            GamePlayPhase.Caught -> {
                currentOnCaughtSound()
                delay(CAUGHT_BEFORE_DIE_MS)
                gamePhase = GamePlayPhase.YouDied
            }

            GamePlayPhase.YouDied -> Unit
        }
    }

    if (gamePhase == GamePlayPhase.YouDied) {
        Image(
            painter = painterResource(Res.drawable.you_die_fullscreen),
            contentDescription = "You Die",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onYouDieClick),
        )
        return
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(16)
            if (furParticles.isNotEmpty()) {
                furParticles = furParticles.mapNotNull { it.advance(sceneHeight) }
            }
        }
    }

    LaunchedEffect(isBrushing) {
        if (!isBrushing) {
            combJitter = CombJitter.None
            return@LaunchedEffect
        }

        while (isBrushing) {
            val jitterSequence = List(8) {
                CombJitter(
                    translation = Offset(
                        x = Random.nextFloat() * 18f - 9f,
                        y = Random.nextFloat() * 18f - 9f,
                    ),
                    rotation = Random.nextFloat() * 8f - 4f,
                )
            }
            jitterSequence.forEach { jitter ->
                if (!isBrushing) return@forEach
                combJitter = jitter
                delay(42)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFF6EC), Color(0xFFFFDEC2)),
                ),
            )
            .safeContentPadding()
            .clipToBounds()
            .onSizeChanged { sceneHeight = it.height.toFloat() }
            .pointerInput(catBounds) {
                detectDragGestures(
                    onDragStart = onDragStart@{ position ->
                        when (currentGamePhase) {
                            GamePlayPhase.CatTurnedSafe -> {
                                mouthResource = MOUTH_RESOURCES.random()
                                gamePhase = GamePlayPhase.Caught
                                combPosition = Offset.Unspecified
                                isBrushing = false
                                return@onDragStart
                            }

                            GamePlayPhase.Caught,
                            GamePlayPhase.YouDied,
                            -> return@onDragStart

                            GamePlayPhase.Playing,
                            GamePlayPhase.CatTurnGrace,
                            -> Unit
                        }

                        isBrushing = true
                        combPosition = position
                        distanceSinceLastFur = 0f
                    },
                    onDragEnd = { isBrushing = false },
                    onDragCancel = { isBrushing = false },
                    onDrag = onDrag@{ change, dragAmount ->
                        change.consume()
                        when (currentGamePhase) {
                            GamePlayPhase.CatTurnedSafe -> {
                                mouthResource = MOUTH_RESOURCES.random()
                                gamePhase = GamePlayPhase.Caught
                                combPosition = Offset.Unspecified
                                isBrushing = false
                                return@onDrag
                            }

                            GamePlayPhase.CatTurnGrace -> {
                                combPosition = change.position
                                return@onDrag
                            }

                            GamePlayPhase.Caught,
                            GamePlayPhase.YouDied,
                            -> return@onDrag

                            GamePlayPhase.Playing -> Unit
                        }

                        val position = change.position
                        combPosition = position
                        distanceSinceLastFur += dragAmount.getDistance()

                        if (catBounds.contains(position) && distanceSinceLastFur >= 18f) {
                            furParticles = appendFurParticlesPreservingExisting(
                                current = furParticles,
                                spawned = createFurParticles(position, dragAmount),
                            )
                            currentOnBrushHaptic()
                            distanceSinceLastFur = 0f
                        }
                    },
                )
            },
    ) {
        Image(
            painter = painterResource(
                if (
                    gamePhase == GamePlayPhase.CatTurnGrace ||
                    gamePhase == GamePlayPhase.CatTurnedSafe
                ) {
                    frontCatResource
                } else {
                    Res.drawable.gameplay_cat_back
                },
            ),
            contentDescription = "Taranmayı bekleyen kedi",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.86f)
                .aspectRatio(0.88f)
                .onGloballyPositioned { coordinates ->
                    catBounds = coordinates.boundsInParent()
                }
                .zIndex(1f),
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(2f),
        ) {
            furParticles.forEach { particle ->
                val shake = sin(particle.age * 0.70f + particle.jitterPhase) * 3.4f
                val start = Offset(particle.position.x + shake, particle.position.y)
                val path = Path().apply {
                    moveTo(start.x, start.y)
                    quadraticTo(
                        start.x + particle.curve + shake,
                        start.y - particle.length * 0.42f,
                        start.x + particle.drift + shake,
                        start.y - particle.length,
                    )
                }
                drawPath(
                    path = path,
                    color = Color(0xFF713609).copy(alpha = particle.alpha * 0.72f),
                    style = Stroke(
                        width = particle.thickness + 2.4f,
                        cap = StrokeCap.Round,
                    ),
                )
                drawPath(
                    path = path,
                    color = Color(0xFFF0A126).copy(alpha = particle.alpha),
                    style = Stroke(
                        width = particle.thickness,
                        cap = StrokeCap.Round,
                    ),
                )
            }
        }

        if (gamePhase == GamePlayPhase.Caught) {
            Image(
                painter = painterResource(mouthResource),
                contentDescription = "Yaklaşan açık ağızlı kedi",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.86f)
                    .aspectRatio(0.88f)
                    .graphicsLayer {
                        val approachProgress = (
                            (caughtMouthScale - CAUGHT_MOUTH_INITIAL_SCALE) /
                                (CAUGHT_MOUTH_TARGET_SCALE - CAUGHT_MOUTH_INITIAL_SCALE)
                            ).coerceIn(0f, 1f)
                        scaleX = caughtMouthScale
                        scaleY = caughtMouthScale
                        translationX = -size.width * 0.08f * approachProgress
                        translationY = size.height * 0.34f * approachProgress
                        transformOrigin = TransformOrigin(0.58f, 0.20f)
                    }
                    .zIndex(4f),
            )
        }

        if (combPosition != Offset.Unspecified) {
            Image(
                painter = painterResource(Res.drawable.gameplay_comb),
                contentDescription = "Tarama tarağı",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(176.dp)
                    .graphicsLayer {
                        translationX = combPosition.x - size.width * 0.30f + combJitter.translation.x
                        translationY = combPosition.y - size.height * 0.24f + combJitter.translation.y
                        rotationZ = -18f + combJitter.rotation
                    }
                    .zIndex(3f),
            )
        }
    }
}

internal data class FurParticle(
    val position: Offset,
    val velocity: Offset,
    val drift: Float,
    val curve: Float,
    val length: Float,
    val thickness: Float,
    val jitterPhase: Float,
    val horizontalSway: Float,
    val age: Int = 0,
    val maxAge: Int,
) {
    val alpha: Float
        get() {
            val fadeStart = maxAge * 0.82f
            if (age <= fadeStart) return 1f
            return (1f - (age - fadeStart) / (maxAge - fadeStart)).coerceIn(0f, 1f)
        }

    fun advance(sceneHeight: Float): FurParticle? {
        if (age >= maxAge || (sceneHeight > 0f && position.y - length > sceneHeight)) return null
        val sideToSide = sin(age * 0.16f + jitterPhase) * horizontalSway
        return copy(
            position = position + velocity + Offset(sideToSide, 0f),
            velocity = velocity.copy(y = velocity.y + 0.12f),
            age = age + 1,
        )
    }
}

private data class CombJitter(
    val translation: Offset,
    val rotation: Float,
) {
    companion object {
        val None = CombJitter(translation = Offset.Zero, rotation = 0f)
    }
}

internal fun createFurParticles(position: Offset, dragAmount: Offset): List<FurParticle> =
    List(4) {
        FurParticle(
            position = position + Offset(
                x = Random.nextFloat() * 20f - 10f,
                y = Random.nextFloat() * 16f - 8f,
            ),
            velocity = Offset(
                x = dragAmount.x * 0.08f + Random.nextFloat() * 5.6f - 2.8f,
                y = Random.nextFloat() * 2.2f + 1.2f,
            ),
            drift = Random.nextFloat() * 22f - 11f,
            curve = Random.nextFloat() * 18f - 9f,
            length = Random.nextFloat() * 26f + 24f,
            thickness = Random.nextFloat() * 2.2f + 2.6f,
            jitterPhase = Random.nextFloat() * 6.28f,
            horizontalSway = Random.nextFloat() * 1.5f + 0.5f,
            maxAge = Random.nextInt(360, 480),
        )
    }

internal fun appendFurParticlesPreservingExisting(
    current: List<FurParticle>,
    spawned: List<FurParticle>,
): List<FurParticle> {
    val availableSlots = (MAX_FUR_PARTICLES - current.size).coerceAtLeast(0)
    if (availableSlots == 0) return current
    return current + spawned.take(availableSlots)
}

internal fun nextCatTurnDelayMillis(random: Random = Random.Default): Long =
    random.nextLong(MIN_CAT_TURN_DELAY_MS, MAX_CAT_TURN_DELAY_MS + 1L)

internal fun nextCatTurnedHoldDelayMillis(random: Random = Random.Default): Long =
    random.nextLong(MIN_CAT_TURNED_HOLD_MS, MAX_CAT_TURNED_HOLD_MS + 1L)

internal fun phaseAfterTurnGrace(isBrushing: Boolean): GamePlayPhase =
    if (isBrushing) GamePlayPhase.Caught else GamePlayPhase.CatTurnedSafe

private val FRONT_CAT_RESOURCES: List<DrawableResource> = listOf(
    Res.drawable.gameplay_cat_front_1,
    Res.drawable.gameplay_cat_front_2,
    Res.drawable.gameplay_cat_front_3,
    Res.drawable.gameplay_cat_front_4,
    Res.drawable.gameplay_cat_front_5,
    Res.drawable.gameplay_cat_front_6,
)

private val MOUTH_RESOURCES: List<DrawableResource> = listOf(
    Res.drawable.gameplay_cat_mouth_1,
    Res.drawable.gameplay_cat_mouth_2,
    Res.drawable.gameplay_cat_mouth_3,
)
