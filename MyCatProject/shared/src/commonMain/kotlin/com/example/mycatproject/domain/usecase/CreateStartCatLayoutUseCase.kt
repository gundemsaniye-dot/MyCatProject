package com.example.mycatproject.domain.usecase

import com.example.mycatproject.domain.model.StartCat
import kotlin.math.roundToInt
import kotlin.random.Random

class CreateStartCatLayoutUseCase(
    private val random: Random = Random.Default,
) {
    operator fun invoke(): List<StartCat> {
        val catCount = random.nextInt(MIN_CAT_COUNT, MAX_CAT_COUNT + 1)
        val straightCatCount = (catCount * STRAIGHT_CAT_RATIO).roundToInt()
        val negativeRotationCatCount = (catCount * NEGATIVE_ROTATION_CAT_RATIO).roundToInt()
        val positiveRotationCatCount = catCount - straightCatCount - negativeRotationCatCount
        val placements = createNonTouchingRandomPlacements(catCount)
        val imageOrder = List(catCount) { it % AVAILABLE_IMAGE_COUNT }.shuffled(random)
        val rotationOrder = buildList {
            repeat(straightCatCount) { add(0f) }
            repeat(negativeRotationCatCount) { add(random.nextFloat(-25f, -10f)) }
            repeat(positiveRotationCatCount) { add(random.nextFloat(10f, 25f)) }
        }.shuffled(random)

        return List(catCount) { index ->
            val placement = placements[index]
            StartCat(
                id = index,
                imageIndex = imageOrder[index],
                horizontalFraction = placement.horizontalFraction,
                verticalFraction = placement.verticalFraction,
                sizeFraction = placement.sizeFraction,
                opacity = random.nextFloat(0.40f, 0.90f),
                rotationDegrees = rotationOrder[index],
                bounceFraction = random.nextFloat(0.01f, 0.03f),
                animationDurationMillis = random.nextInt(1_150, 2_050),
                animationDelayMillis = random.nextInt(0, 650),
                mirrorHorizontally = random.nextBoolean(),
            )
        }
    }

    private fun createNonTouchingRandomPlacements(catCount: Int): List<CatPlacement> {
        repeat(MAX_LAYOUT_ATTEMPTS) { attempt ->
            val maximumSize = maximumSizeForAttempt(attempt)
            val sizes = List(catCount) {
                random.nextFloat(MIN_CAT_SIZE, maximumSize)
            }.sortedDescending()
            val placements = mutableListOf<CatPlacement>()

            for (size in sizes) {
                val horizontalInset = size * COLLISION_BOUNDS_FACTOR / 2f
                val verticalInset = horizontalInset / MOBILE_STAGE_HEIGHT_RATIO
                val candidates = List(POSITION_CANDIDATE_COUNT) {
                    CatPlacement(
                        horizontalFraction = random.nextFloat(
                            horizontalInset,
                            1f - horizontalInset,
                        ),
                        verticalFraction = random.nextFloat(
                            verticalInset,
                            1f - verticalInset,
                        ),
                        sizeFraction = size,
                    )
                }.filter { candidate ->
                    placements.all { existing -> !candidate.touches(existing) }
                }

                val selectedPlacement = candidates.maxByOrNull { candidate ->
                    placements.minOfOrNull { existing ->
                        candidate.centerDistanceSquared(existing)
                    } ?: Float.MAX_VALUE
                }
                if (selectedPlacement == null) {
                    break
                }
                placements += selectedPlacement
            }

            if (placements.size == catCount) {
                return placements.shuffled(random)
            }
        }

        error("Unable to create a non-touching cat layout")
    }

    private fun maximumSizeForAttempt(attempt: Int): Float {
        val reductionProgress =
            (attempt.toFloat() / (MAX_LAYOUT_ATTEMPTS - 1)).coerceIn(0f, 1f)
        return MAX_CAT_SIZE - (MAX_CAT_SIZE - FALLBACK_MAX_CAT_SIZE) * reductionProgress
    }

    private fun CatPlacement.touches(other: CatPlacement): Boolean {
        val requiredSeparation =
            (sizeFraction + other.sizeFraction) * COLLISION_BOUNDS_FACTOR / 2f + CONTACT_GAP
        val horizontalDistance = kotlin.math.abs(horizontalFraction - other.horizontalFraction)
        val verticalDistance =
            kotlin.math.abs(verticalFraction - other.verticalFraction) * MOBILE_STAGE_HEIGHT_RATIO
        return horizontalDistance < requiredSeparation && verticalDistance < requiredSeparation
    }

    private fun CatPlacement.centerDistanceSquared(other: CatPlacement): Float {
        val horizontalDistance = horizontalFraction - other.horizontalFraction
        val verticalDistance =
            (verticalFraction - other.verticalFraction) * MOBILE_STAGE_HEIGHT_RATIO
        return horizontalDistance * horizontalDistance + verticalDistance * verticalDistance
    }

    private data class CatPlacement(
        val horizontalFraction: Float,
        val verticalFraction: Float,
        val sizeFraction: Float,
    )

    private fun Random.nextFloat(from: Float, until: Float): Float =
        from + nextFloat() * (until - from)

    private companion object {
        const val MIN_CAT_COUNT = 18
        const val MAX_CAT_COUNT = 25
        const val AVAILABLE_IMAGE_COUNT = 7
        const val POSITION_CANDIDATE_COUNT = 600
        const val MAX_LAYOUT_ATTEMPTS = 80
        const val MOBILE_STAGE_HEIGHT_RATIO = 16f / 9f
        const val MIN_CAT_SIZE = 0.125f
        const val MAX_CAT_SIZE = 0.375f
        const val FALLBACK_MAX_CAT_SIZE = 0.225f
        const val COLLISION_BOUNDS_FACTOR = 1.08f
        const val CONTACT_GAP = 0.01f
        const val STRAIGHT_CAT_RATIO = 0.4f
        const val NEGATIVE_ROTATION_CAT_RATIO = 0.3f
    }
}
