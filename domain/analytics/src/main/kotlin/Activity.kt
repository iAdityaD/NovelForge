package com.novelforge.domain.analytics

import com.novelforge.core.model.*
import java.time.LocalDate

fun writingStreak(days: List<WritingDay>, today: LocalDate = LocalDate.now()): Int {
    val active = days.filter { it.edits > 0 }.map { it.day }.toSet()
    var day = if (today.toString() in active) today else today.minusDays(1)
    var count = 0
    while (day.toString() in active) {
        count++
        day = day.minusDays(1)
    }
    return count
}
