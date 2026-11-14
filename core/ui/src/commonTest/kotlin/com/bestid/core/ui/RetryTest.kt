package com.bestid.core.ui

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RetryTest {
    @Test
    fun failureEmitsTheErrorStateAndRetryStartsAgain() = runTest {
        var calls = 0
        val retry = Retry()
        val states = retry.wrap("error") {
            flow {
                calls++
                if (calls == 1) throw IllegalStateException("disco lleno")
                emit("ok")
            }
        }
        val collected = mutableListOf<String>()
        val job = launch { states.take(2).toList(collected) }
        testScheduler.advanceUntilIdle()
        retry.retry()
        job.join()
        assertEquals(listOf("error", "ok"), collected)
    }

    @Test
    fun attemptReportsFailureWithoutThrowing() = runTest {
        assertTrue(attempt { error("no") }.isFailure)
        assertEquals(2, attempt { 1 + 1 }.getOrThrow())
        assertEquals("x", Retry().wrap("e") { flow { emit("x") } }.first())
    }
}
