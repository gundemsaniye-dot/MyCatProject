package com.example.mycatproject

import androidx.compose.ui.geometry.Offset
import com.example.mycatproject.presentation.gameplay.MAX_FUR_PARTICLES
import com.example.mycatproject.presentation.gameplay.MAX_CAT_TURN_DELAY_MS
import com.example.mycatproject.presentation.gameplay.MAX_CAT_TURNED_HOLD_MS
import com.example.mycatproject.presentation.gameplay.MIN_CAT_TURN_DELAY_MS
import com.example.mycatproject.presentation.gameplay.MIN_CAT_TURNED_HOLD_MS
import com.example.mycatproject.presentation.gameplay.CAT_TURN_GRACE_MS
import com.example.mycatproject.presentation.gameplay.CAUGHT_BEFORE_DIE_MS
import com.example.mycatproject.presentation.gameplay.CAUGHT_MOUTH_INITIAL_SCALE
import com.example.mycatproject.presentation.gameplay.CAUGHT_MOUTH_TARGET_SCALE
import com.example.mycatproject.presentation.gameplay.GamePlayPhase
import com.example.mycatproject.presentation.gameplay.appendFurParticlesPreservingExisting
import com.example.mycatproject.presentation.gameplay.createFurParticles
import com.example.mycatproject.presentation.gameplay.nextCatTurnDelayMillis
import com.example.mycatproject.presentation.gameplay.nextCatTurnedHoldDelayMillis
import com.example.mycatproject.presentation.gameplay.phaseAfterTurnGrace
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GamePlayFurPhysicsTest {
    @Test
    fun everyFurParticleStartsDownwardAndAcceleratesWithGravity() {
        val particles = createFurParticles(
            position = Offset(200f, 500f),
            dragAmount = Offset(0f, -80f),
        )

        assertTrue(particles.isNotEmpty())
        particles.forEach { particle ->
            assertTrue(particle.velocity.y > 0f)

            val nextFrame = assertNotNull(particle.advance(sceneHeight = 1_600f))
            assertTrue(nextFrame.position.y > particle.position.y)
            assertTrue(nextFrame.velocity.y > particle.velocity.y)
        }
    }

    @Test
    fun furParticleLivesUntilItPassesTheBottomEdge() {
        val particle = createFurParticles(
            position = Offset(200f, 500f),
            dragAmount = Offset.Zero,
        ).first()

        val aboveBottom = particle.copy(
            position = Offset(200f, 1_190f),
            length = 40f,
        )
        assertNotNull(aboveBottom.advance(sceneHeight = 1_200f))

        val belowBottom = particle.copy(
            position = Offset(200f, 1_241f),
            length = 40f,
        )
        assertNull(belowBottom.advance(sceneHeight = 1_200f))
    }

    @Test
    fun spawningNewFurNeverRemovesExistingFur() {
        val template = createFurParticles(
            position = Offset(200f, 500f),
            dragAmount = Offset.Zero,
        ).first()
        val existing = List(MAX_FUR_PARTICLES - 2) { index ->
            template.copy(position = Offset(index.toFloat(), 500f))
        }
        val spawned = List(4) { index ->
            template.copy(position = Offset(10_000f + index, 500f))
        }

        val combined = appendFurParticlesPreservingExisting(existing, spawned)

        assertTrue(combined.take(existing.size) == existing)
        assertTrue(combined.size == MAX_FUR_PARTICLES)
        assertTrue(combined.takeLast(2) == spawned.take(2))
    }

    @Test
    fun catTurnDelayAlwaysStaysBetweenThreeAndTenSeconds() {
        assertTrue(MIN_CAT_TURN_DELAY_MS == 3_000L)
        assertTrue(MAX_CAT_TURN_DELAY_MS == 10_000L)
        repeat(200) { seed ->
            val delay = nextCatTurnDelayMillis(Random(seed))
            assertTrue(delay in MIN_CAT_TURN_DELAY_MS..MAX_CAT_TURN_DELAY_MS)
        }
    }

    @Test
    fun catStaysTurnedForTwoToFiveSecondsAfterSafeGrace() {
        repeat(200) { seed ->
            val delay = nextCatTurnedHoldDelayMillis(Random(seed))
            assertTrue(delay in MIN_CAT_TURNED_HOLD_MS..MAX_CAT_TURNED_HOLD_MS)
        }
    }

    @Test
    fun twoHundredMillisecondGraceDecidesWhetherThePlayerIsCaught() {
        assertTrue(CAT_TURN_GRACE_MS == 200L)
        assertTrue(phaseAfterTurnGrace(isBrushing = true) == GamePlayPhase.Caught)
        assertTrue(phaseAfterTurnGrace(isBrushing = false) == GamePlayPhase.CatTurnedSafe)
    }

    @Test
    fun caughtCatWaitsFiveSecondsBeforeYouDieScreen() {
        assertTrue(CAUGHT_BEFORE_DIE_MS == 5_000L)
        assertTrue(CAUGHT_MOUTH_INITIAL_SCALE == 2f)
        assertTrue(CAUGHT_MOUTH_TARGET_SCALE == 4f)
    }
}
