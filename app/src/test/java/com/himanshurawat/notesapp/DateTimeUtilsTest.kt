package com.himanshurawat.notesapp

import com.himanshurawat.notesapp.ui.util.DateTimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Locale

class DateTimeUtilsTest {

    @Test
    fun formatDateTime_emptyOnZeroOrNegative() {
        assertEquals("", DateTimeUtils.formatDateTime(0L, true))
        assertEquals("", DateTimeUtils.formatDateTime(-100L, false))
    }

    @Test
    fun formatDateTime_24HourFormat() {
        val calendar = Calendar.getInstance(Locale.US).apply {
            set(2024, Calendar.MAY, 13, 14, 30, 0)
        }
        val formatted = DateTimeUtils.formatDateTime(calendar.timeInMillis, true)
        assertTrue(formatted.contains("14:30"))
    }

    @Test
    fun formatDateTime_12HourFormat() {
        val calendar = Calendar.getInstance(Locale.US).apply {
            set(2024, Calendar.MAY, 13, 14, 30, 0)
        }
        val formatted = DateTimeUtils.formatDateTime(calendar.timeInMillis, false)
        assertTrue(formatted.contains("02:30") || formatted.contains("2:30"))
    }
}
