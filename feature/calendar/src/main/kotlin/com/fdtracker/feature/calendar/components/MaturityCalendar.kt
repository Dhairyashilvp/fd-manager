package com.fdtracker.feature.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fdtracker.core.domain.model.FixedDeposit
import com.fdtracker.core.ui.theme.FdActive
import com.fdtracker.core.ui.theme.FdMaturing
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MaturityCalendar(
    currentMonth: YearMonth,
    maturityDates: Map<LocalDate, List<FixedDeposit>>,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDay = currentMonth.atDay(1).dayOfWeek.value % 7 // Sunday = 0

    Column(modifier = modifier.fillMaxWidth()) {
        // Day of week headers
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Calendar grid
        var dayCounter = 1
        val totalCells = firstDay + daysInMonth
        val rows = (totalCells + 6) / 7

        repeat(rows) { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { dayOfWeek ->
                    val cellIndex = week * 7 + dayOfWeek
                    if (cellIndex >= firstDay && dayCounter <= daysInMonth) {
                        val date = currentMonth.atDay(dayCounter)
                        val hasFds = maturityDates.containsKey(date)
                        val isSelected = date == selectedDate
                        val isToday = date == today
                        val isWithin30Days = !date.isBefore(today) && date.isBefore(today.plusDays(31))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primaryContainer
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayCounter.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                if (hasFds) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isWithin30Days) FdMaturing else FdActive
                                            )
                                    )
                                }
                            }
                        }
                        dayCounter++
                    } else {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}
