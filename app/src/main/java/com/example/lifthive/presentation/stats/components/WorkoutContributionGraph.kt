package com.example.lifthive.presentation.stats.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.Locale

data class ContributionDay(
    val timeMillis: Long,
    val isCompleted: Boolean
)

data class ContributionWeek(
    val days: List<ContributionDay>
)

data class ContributionMonth(
    val name: String,
    val weeks: List<ContributionWeek>
)

@Composable
fun WorkoutContributionGraph(
    workoutDates: List<Long>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Consistency Map",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Log workout sessions to fill the last 12 weeks grid",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.WEEK_OF_YEAR, -11)
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val startDateMillis = calendar.timeInMillis

            val monthlyData = remember(workoutDates, startDateMillis) {
                val activeDates = workoutDates.map {
                    val cal = Calendar.getInstance().apply { timeInMillis = it }
                    "" + cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.MONTH) + "-" + cal.get(Calendar.DAY_OF_MONTH)
                }.toSet()

                val monthsList = mutableListOf<ContributionMonth>()
                val weekCalendar = Calendar.getInstance()
                val dayCalendar = Calendar.getInstance()

                var currentMonthName = ""
                var currentWeeks = mutableListOf<ContributionWeek>()

                for (w in 0 until 12) {
                    weekCalendar.timeInMillis = startDateMillis
                    weekCalendar.add(Calendar.DAY_OF_YEAR, w * 7)

                    val monthName = weekCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) ?: ""

                    val daysList = mutableListOf<ContributionDay>()
                    for (d in 0 until 7) {
                        dayCalendar.timeInMillis = weekCalendar.timeInMillis
                        dayCalendar.add(Calendar.DAY_OF_YEAR, d)

                        val dateKey = "" + dayCalendar.get(Calendar.YEAR) + "-" + dayCalendar.get(Calendar.MONTH) + "-" + dayCalendar.get(Calendar.DAY_OF_MONTH)
                        val isCompleted = activeDates.contains(dateKey)

                        daysList.add(ContributionDay(dayCalendar.timeInMillis, isCompleted))
                    }

                    val week = ContributionWeek(daysList)

                    if (monthName != currentMonthName) {
                        if (currentMonthName.isNotEmpty()) {
                            monthsList.add(ContributionMonth(currentMonthName, currentWeeks))
                        }
                        currentMonthName = monthName
                        currentWeeks = mutableListOf(week)
                    } else {
                        currentWeeks.add(week)
                    }
                }
                if (currentMonthName.isNotEmpty()) {
                    monthsList.add(ContributionMonth(currentMonthName, currentWeeks))
                }
                monthsList
            }

            val dayLabels = listOf("Su", "", "Tu", "", "Th", "", "Sa")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Spacer(modifier = Modifier.height(18.dp))

                    dayLabels.forEach { day ->
                        Box(
                            modifier = Modifier.size(14.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            if (day.isNotEmpty()) {
                                  Text(
                                      text = day,
                                      style = MaterialTheme.typography.labelSmall.copy(
                                          fontSize = 8.sp,
                                          fontWeight = FontWeight.Medium,
                                          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                      )
                                  )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    monthlyData.forEach { (monthName, weeks) ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = monthName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                weeks.forEach { week ->
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        week.days.forEach { day ->
                                            val color = if (day.isCompleted) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .background(
                                                        color = color,
                                                        shape = RoundedCornerShape(3.dp)
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Less",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Workout Day",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}
