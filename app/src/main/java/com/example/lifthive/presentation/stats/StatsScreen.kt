package com.example.lifthive.presentation.stats

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.roundToInt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import java.util.Locale
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    navController: NavController,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (state.isLoading || state.stats == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            val stats = state.stats!!

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // ── Row 1: 4 key stat chips ──────────────────────────
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatChip(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.FitnessCenter,
                            label = "Sessions",
                            value = "${stats.totalWorkouts}",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        StatChip(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.LocalFireDepartment,
                            label = "Streak",
                            value = "${stats.currentStreak}d",
                            tint = Color(0xFFFF6B35)
                        )
                        StatChip(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.TrendingUp,
                            label = "Best Streak",
                            value = "${stats.longestStreak}d",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        StatChip(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Speed,
                            label = "Avg / Session",
                            value = if (stats.avgVolumePerSession >= 1000)
                                String.format(Locale.US, "%.1fk", stats.avgVolumePerSession / 1000)
                            else String.format(Locale.US, "%.0f", stats.avgVolumePerSession),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                // ── PR Banner ─────────────────────────────────────────
                item {
                    val prVol = if (stats.bestSessionVolume >= 1000)
                        String.format(Locale.US, "%,.1fk kg", stats.bestSessionVolume / 1000)
                    else String.format(Locale.US, "%,.0f kg", stats.bestSessionVolume)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Personal Record — Best Session",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = prVol,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Text(
                                text = stats.bestSessionLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // ── Consistency Map ───────────────────────────────────
                item {
                    WorkoutContributionGraph(workoutDates = stats.workoutDates)
                }

                // ── Training Intensity Progress ───────────────────────
                item {
                    TrainingIntensityCard(volumes = stats.lastWorkoutsVolume)
                }

                // ── Weekly Volume Trend ───────────────────────────────
                item {
                    WeeklyTrendCard(weeklyVolumes = stats.weeklyVolumes)
                }

                // ── Top Exercises Podium ──────────────────────────────
                item {
                    TopExercisesCard(topExercises = stats.topExercises)
                }
            }
        }
    }
}

// ── Compact stat chip ────────────────────────────────────────────────────
@Composable
fun StatChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// ── Training Intensity Progress (redesigned bar chart) ────────────────────
@Composable
fun TrainingIntensityCard(volumes: List<Pair<String, Double>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Training Intensity", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Volume per session  (sets × reps × weight kg)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            if (volumes.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    Text("Log workouts to see your intensity trend", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            } else {
                val maxVal = volumes.maxOf { it.second }.let { if (it == 0.0) 1.0 else it }
                var startAnimate by remember { mutableStateOf(false) }
                LaunchedEffect(true) { startAnimate = true }

                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

                // Trend line drawn on a Canvas overlay
                val barHeightFraction = volumes.map { (it.second / maxVal).toFloat() }

                // Horizontally scrollable bar chart — auto-snaps to latest (rightmost) session
                val barWidth = 52.dp
                val chartWidth = barWidth * volumes.size + 6.dp * (volumes.size - 1)
                val scrollState = rememberScrollState()

                // Scroll to the rightmost end after first composition so latest session is visible
                LaunchedEffect(volumes.size) {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    // Background guide lines span the full visible width
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val lineY25 = size.height * 0.80f
                        val lineY50 = size.height * 0.60f
                        val lineY75 = size.height * 0.40f
                        listOf(lineY25, lineY50, lineY75).forEach { y ->
                            drawLine(
                                color = surfaceVariantColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .horizontalScroll(scrollState)
                            .width(chartWidth),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        volumes.forEachIndexed { i, (dateLabel, volume) ->
                            val targetFrac = barHeightFraction[i]
                            val animatedFrac by animateDpAsState(
                                targetValue = if (startAnimate) (targetFrac * 140).dp else 0.dp,
                                animationSpec = tween(durationMillis = 900, delayMillis = i * 80)
                            )
                            val displayVal = if (volume >= 1000)
                                String.format(Locale.US, "%.1fk", volume / 1000.0)
                            else String.format(Locale.US, "%.0f", volume)

                            Column(
                                modifier = Modifier
                                    .width(barWidth)
                                    .fillMaxHeight(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                // Value chip above bar
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = displayVal,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))

                                // Gradient bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.65f)
                                        .height(animatedFrac)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(primaryColor, secondaryColor)
                                            )
                                        )
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                // Date label
                                Text(
                                    text = dateLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Summary row: trend change
                if (volumes.size >= 2) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val first = volumes.first().second
                    val last = volumes.last().second
                    val changePercent = if (first > 0) ((last - first) / first * 100).roundToInt() else 0
                    val isUp = changePercent >= 0
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isUp) Color(0xFF22C55E).copy(alpha = 0.12f)
                                else Color(0xFFEF4444).copy(alpha = 0.12f)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = if (isUp) Color(0xFF22C55E) else Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isUp) "+$changePercent% vs first recorded session"
                            else "$changePercent% vs first recorded session",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isUp) Color(0xFF22C55E) else Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
    }
}

// ── Weekly Volume Trend ────────────────────────────────────────────────────
@Composable
fun WeeklyTrendCard(weeklyVolumes: List<Pair<String, Double>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Scale, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Weekly Volume Trend", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Total weight moved each week (kg)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (weeklyVolumes.all { it.second == 0.0 }) {
                Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                    Text("Keep logging — weekly trend will appear here", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            } else {
                val maxVol = weeklyVolumes.maxOf { it.second }.let { if (it == 0.0) 1.0 else it }
                // Show only last 3 weeks (most recent 3), latest at bottom
                val displayWeeks = weeklyVolumes.takeLast(3)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    displayWeeks.forEach { (label, vol) ->
                        val fraction = (vol / maxVol).toFloat().coerceIn(0f, 1f)
                        val displayVol = if (vol >= 1000)
                            String.format(Locale.US, "%,.1fk", vol / 1000)
                        else String.format(Locale.US, "%,.0f", vol)
                        val isThisWeek = label == "This week"

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            // Label + value on same row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isThisWeek) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isThisWeek) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (vol == 0.0) "Rest week" else "$displayVol kg",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isThisWeek) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            // Horizontal progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            ) {
                                if (fraction > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(fraction)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                brush = Brush.horizontalGradient(
                                                    colors = if (isThisWeek)
                                                        listOf(
                                                            MaterialTheme.colorScheme.primary,
                                                            MaterialTheme.colorScheme.secondary
                                                        )
                                                    else
                                                        listOf(
                                                            MaterialTheme.colorScheme.tertiary,
                                                            MaterialTheme.colorScheme.primary
                                                        )
                                                )
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
}

// ── Top Exercises Podium ────────────────────────────────────────────────────
@Composable
fun TopExercisesCard(topExercises: List<Pair<String, Int>>) {
    if (topExercises.isEmpty()) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFACC15).copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Top Exercises", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Most logged exercises across all sessions", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            val maxCount = topExercises.maxOf { it.second }.toFloat().let { if (it == 0f) 1f else it }
            val medalColors = listOf(Color(0xFFFFD700), Color(0xFFC0C0C0), Color(0xFFCD7F32))

            topExercises.forEachIndexed { index, (name, count) ->
                val fraction = count / maxCount
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rank badge
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (index < 3) medalColors[index].copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#${index + 1}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = if (index < 3) medalColors[index] else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    // Name + bar
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            fontSize = 12.sp,
                            fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth().height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxHeight().fillMaxWidth(fraction)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        if (index < 3) medalColors[index]
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${count}x",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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

            // Compute dates for the last 12 weeks (7 rows, 12 columns = 84 cells)
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.WEEK_OF_YEAR, -11)
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val startDateMillis = calendar.timeInMillis

            // Group workouts by day string: "YYYY-MM-DD"
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

            // Outer Row for week labels and graph
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Week Label column
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Spacer to match Month label height + padding
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

                // Scrollable row of months
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    monthlyData.forEach { (monthName, weeks) ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Month Label
                            Text(
                                text = monthName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                weeks.forEach { week ->
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        week.days.forEach { day ->
                                            val color = if (day.isCompleted) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                // High contrast tint for empty days (visible in both light and dark themes)
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

