package com.bestid.core.data.repository

import com.bestid.core.domain.port.AppClock
import com.bestid.core.domain.port.IdGenerator
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SystemAppClock : AppClock {
    override fun now(): Instant = Clock.System.now()
    override fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
}

class UuidGenerator : IdGenerator {
    @OptIn(ExperimentalUuidApi::class)
    override fun newId(): String = Uuid.random().toString()
}
