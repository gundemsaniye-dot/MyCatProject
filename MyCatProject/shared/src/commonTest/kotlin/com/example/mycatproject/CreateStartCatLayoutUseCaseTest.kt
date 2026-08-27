package com.example.mycatproject

import com.example.mycatproject.domain.usecase.CreateStartCatLayoutUseCase
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateStartCatLayoutUseCaseTest {
    @Test
    fun createsEighteenToTwentyFiveNonTouchingRandomCats() {
        val cats = CreateStartCatLayoutUseCase(Random(42))()
        val expectedStraightCount = (cats.size * 0.4f).roundToInt()
        val expectedNegativeCount = (cats.size * 0.3f).roundToInt()
        val expectedPositiveCount = cats.size - expectedStraightCount - expectedNegativeCount

        assertTrue(cats.size in 18..25)
        assertEquals(cats.size, cats.map { it.id }.distinct().size)
        assertEquals((0..6).toSet(), cats.map { it.imageIndex }.toSet())
        assertTrue(cats.all { it.horizontalFraction >= 0f && it.horizontalFraction < 1f })
        assertTrue(cats.all { it.verticalFraction >= 0f && it.verticalFraction < 1f })
        assertEquals(
            cats.size,
            cats.map { it.horizontalFraction to it.verticalFraction }.distinct().size,
        )
        assertTrue(cats.all { it.sizeFraction >= 0.125f && it.sizeFraction < 0.375f })
        assertTrue(cats.all { it.opacity >= 0.40f && it.opacity < 0.90f })
        assertTrue(cats.all { it.bounceFraction in 0.01f..0.03f })
        assertEquals(expectedStraightCount, cats.count { it.rotationDegrees == 0f })
        assertEquals(expectedNegativeCount, cats.count { it.rotationDegrees in -25f..-10f })
        assertEquals(expectedPositiveCount, cats.count { it.rotationDegrees in 10f..25f })
        assertTrue(cats.noneTouch())
    }

    @Test
    fun catCountAndPositionsChangeBetweenLaunches() {
        val layouts = (0 until 30).map { seed ->
            CreateStartCatLayoutUseCase(Random(seed))()
        }
        val allCats = layouts.flatten()

        assertTrue(layouts.map { it.size }.distinct().size > 1)
        assertEquals(
            layouts.size,
            layouts.map { cats -> cats.map { it.horizontalFraction to it.verticalFraction } }
                .distinct()
                .size,
        )
        assertTrue(allCats.minOf { it.horizontalFraction } < 0.10f)
        assertTrue(allCats.maxOf { it.horizontalFraction } > 0.90f)
        assertTrue(allCats.minOf { it.verticalFraction } < 0.06f)
        assertTrue(allCats.maxOf { it.verticalFraction } > 0.94f)
        assertTrue(allCats.minOf { it.sizeFraction } < 0.15f)
        assertTrue(allCats.maxOf { it.sizeFraction } > 0.26f)
        assertTrue(layouts.all { it.noneTouch() })
    }

    private fun List<com.example.mycatproject.domain.model.StartCat>.noneTouch(): Boolean =
        indices.all { firstIndex ->
            ((firstIndex + 1) until size).all { secondIndex ->
                val first = this[firstIndex]
                val second = this[secondIndex]
                val requiredSeparation =
                    (first.sizeFraction + second.sizeFraction) * 1.08f / 2f + 0.01f
                val horizontalDistance =
                    kotlin.math.abs(first.horizontalFraction - second.horizontalFraction)
                val verticalDistance =
                    kotlin.math.abs(first.verticalFraction - second.verticalFraction) * (16f / 9f)
                horizontalDistance >= requiredSeparation || verticalDistance >= requiredSeparation
            }
        }
}
