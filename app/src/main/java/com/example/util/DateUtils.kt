package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    fun getStartOfDay(timestamp: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getEndOfDay(timestamp: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun getStartOfMonth(year: Int, month: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getEndOfMonth(year: Int, month: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun formatMonthYear(year: Int, month: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return sdf.format(cal.time)
    }

    fun formatDateShort(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDateOnly(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun isSameDay(t1: Long, t2: Long): Boolean {
        val c1 = Calendar.getInstance().apply { timeInMillis = t1 }
        val c2 = Calendar.getInstance().apply { timeInMillis = t2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
               c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    fun isToday(timestamp: Long): Boolean {
        return isSameDay(timestamp, System.currentTimeMillis())
    }

    data class CalendarDay(
        val dayOfMonth: Int,
        val timestamp: Long,
        val isCurrentMonth: Boolean,
        val isToday: Boolean
    )

    /**
     * Builds calendar days for a 7-column grid starting with Monday.
     */
    fun buildMonthDays(year: Int, month: Int): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, 1)

        // Day of week: Sunday=1, Monday=2, ... Saturday=7
        // Monday-based index: Mon=0, Tue=1, Wed=2, Thu=3, Fri=4, Sat=5, Sun=6
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val leadDays = (firstDayOfWeek + 5) % 7

        // Previous month filler days
        val prevCal = Calendar.getInstance()
        prevCal.set(Calendar.YEAR, year)
        prevCal.set(Calendar.MONTH, month)
        prevCal.add(Calendar.MONTH, -1)
        val maxDaysInPrev = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in leadDays - 1 downTo 0) {
            val d = maxDaysInPrev - i
            prevCal.set(Calendar.DAY_OF_MONTH, d)
            val t = getStartOfDay(prevCal.timeInMillis)
            days.add(CalendarDay(dayOfMonth = d, timestamp = t, isCurrentMonth = false, isToday = isToday(t)))
        }

        // Current month days
        val maxDaysCurrent = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (d in 1..maxDaysCurrent) {
            cal.set(Calendar.DAY_OF_MONTH, d)
            val t = getStartOfDay(cal.timeInMillis)
            days.add(CalendarDay(dayOfMonth = d, timestamp = t, isCurrentMonth = true, isToday = isToday(t)))
        }

        // Next month filler to complete trailing row (multiple of 7)
        val trailDays = (7 - (days.size % 7)) % 7
        val nextCal = Calendar.getInstance()
        nextCal.set(Calendar.YEAR, year)
        nextCal.set(Calendar.MONTH, month)
        nextCal.add(Calendar.MONTH, 1)
        for (d in 1..trailDays) {
            nextCal.set(Calendar.DAY_OF_MONTH, d)
            val t = getStartOfDay(nextCal.timeInMillis)
            days.add(CalendarDay(dayOfMonth = d, timestamp = t, isCurrentMonth = false, isToday = isToday(t)))
        }

        return days
    }
}
