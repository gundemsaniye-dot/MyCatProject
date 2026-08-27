package com.example.mycatproject

import com.example.mycatproject.domain.usecase.CreateStartCatLayoutUseCase
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateStartCatLayoutUseCaseTest {
    @Test
    fun createsTenResponsiveCatsAtRandomScreenPositions() {
        val cats = CreateStartCatLayoutUseCase(Random(42))()

        assertEquals(10, cats.size)
        assertEquals(10, cats.map { it.id }.distinct().size)
        assertTrue(cats.all { it.horizontalFraction in 0f..1f })
        assertTrue(cats.all { it.verticalFraction in 0f..0.78f })
        assertTrue(cats.map { it.horizontalFraction to it.verticalFraction }.distinct().size > 1)
        assertTrue(cats.all { it.sizeFraction in 0.17f..0.27f })
        assertEquals(4, cats.count { it.opacity == 1f })
        assertEquals(3, cats.count { it.opacity == 0.5f })
        assertEquals(3, cats.count { it.opacity == 0.15f })
        assertEquals(4, cats.count { it.rotationDegrees == 0f })
        assertEquals(3, cats.count { it.rotationDegrees in -25f..-10f })
        assertEquals(3, cats.count { it.rotationDegrees in 10f..25f })
    }
}
