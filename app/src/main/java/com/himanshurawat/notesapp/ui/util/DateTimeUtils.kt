package com.himanshurawat.notesapp.ui.util

import android.content.Context
import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtils {

    fun formatDateTime(context: Context, timeInMillis: Long): String {
        val is24Hour = DateFormat.is24HourFormat(context)
        return formatDateTime(timeInMillis, is24Hour)
    }

    fun formatDateTime(timeInMillis: Long, is24Hour: Boolean): String {
        if (timeInMillis <= 0L) return ""
        val date = Date(timeInMillis)
        val datePart = SimpleDateFormat("d MMMM", Locale.getDefault()).format(date)
        val timePattern = if (is24Hour) "HH:mm" else "hh:mm a"
        val timePart = SimpleDateFormat(timePattern, Locale.getDefault()).format(date)
        return "$datePart, $timePart"
    }
}
