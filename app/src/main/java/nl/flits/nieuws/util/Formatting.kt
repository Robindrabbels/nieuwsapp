package nl.flits.nieuws.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val NL = Locale.forLanguageTag("nl-NL")
private val dayMonth = DateTimeFormatter.ofPattern("d MMM", NL)
private val dayMonthYear = DateTimeFormatter.ofPattern("d MMM yyyy", NL)
private val time = DateTimeFormatter.ofPattern("HH:mm", NL)
private val fullDate = DateTimeFormatter.ofPattern("EEEE d MMMM", NL)

/** "Zojuist", "12 min", "3 uur", "gisteren 14:05", "4 okt". */
fun relativeTime(epochMillis: Long?, now: Long = System.currentTimeMillis()): String {
    if (epochMillis == null) return ""
    val diff = now - epochMillis
    val minutes = diff / 60_000
    val zone = ZoneId.systemDefault()
    val date = Instant.ofEpochMilli(epochMillis).atZone(zone)
    val today = LocalDate.now(zone)
    return when {
        diff < 0 -> date.format(time)
        minutes < 1 -> "Zojuist"
        minutes < 60 -> "$minutes min geleden"
        minutes < 60 * 12 -> "${minutes / 60} uur geleden"
        date.toLocalDate() == today -> "Vandaag ${date.format(time)}"
        date.toLocalDate() == today.minusDays(1) -> "Gisteren ${date.format(time)}"
        date.year == today.year -> date.format(dayMonth)
        else -> date.format(dayMonthYear)
    }
}

fun todayLabel(): String =
    LocalDate.now().format(fullDate).replaceFirstChar { it.titlecase(NL) }
