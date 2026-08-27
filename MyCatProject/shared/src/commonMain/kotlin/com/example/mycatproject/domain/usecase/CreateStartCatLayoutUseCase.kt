package com.example.mycatproject.domain.usecase

import com.example.mycatproject.domain.model.StartCat
import kotlin.random.Random

class CreateStartCatLayoutUseCase(
    private val random: Random = Random.Default,
) {
    operator fun invoke(): List<StartCat> {
        val imageOrder = List(CAT_COUNT) { it % AVAILABLE_IMAGE_COUNT }.shuffled(random)
        val opacityOrder = buildList {
            repeat(4) { add(1f) }
            repeat(3) { add(0.5f) }
            repeat(3) { add(0.15f) }
        }.shuffled(random)
        val rotationOrder = buildList {
            repeat(4) { add(0f) }
            repeat(3) { add(random.nextFloat(-25f, -10f)) }
            repeat(3) { add(random.nextFloat(10f, 25f)) }
        }.shuffled(random)

        return List(CAT_COUNT) { index ->
            StartCat(
                id = index,
                imageIndex = imageOrder[index],
                horizontalFraction = random.nextFloat(0.01f, 0.99f),
                verticalFraction = random.nextFloat(0.01f, 0.78f),
                sizeFraction = random.nextFloat(0.17f, 0.27f),
                opacity = opacityOrder[index],
                rotationDegrees = rotationOrder[index],
                bounceFraction = random.nextFloat(0.07f, 0.17f),
                animationDurationMillis = random.nextInt(1_150, 2_050),
                animationDelayMillis = random.nextInt(0, 650),
                mirrorHorizontally = random.nextBoolean(),
            )
        }
    }

    private fun Random.nextFloat(from: Float, until: Float): Float =
        from + nextFloat() * (until - from)

    private companion object {
        const val CAT_COUNT = 10
        const val AVAILABLE_IMAGE_COUNT = 7
    }
}
