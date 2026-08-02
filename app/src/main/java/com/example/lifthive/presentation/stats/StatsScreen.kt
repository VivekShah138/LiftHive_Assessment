package com.example.lifthive.presentation.stats

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Speed
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
                // Metric cards grid
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatsMetricRow(
                            title1 = "Total Sessions",
                            value1 = "${stats.totalWorkouts}",
                            icon1 = Icons.Default.FitnessCenter,
                            tint1 = MaterialTheme.colorScheme.primary,
                            title2 = "Top Lift",
                            value2 = stats.mostFrequentExercise,
                            icon2 = Icons.Default.Speed,
                            tint2 = MaterialTheme.colorScheme.secondary
                        )

                        // Big Volume Card
                        val formattedWeight = String.format(Locale.US, "%,.0f", stats.totalWeightLifted)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Scale,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "Total Cumulative Volume",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$formattedWeight kg",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Consistency Map Card (GitHub-style)
                item {
                    WorkoutContributionGraph(workoutDates = stats.workoutDates)
                }

                // Volume Progress Chart Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Training Intensity Progress",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Total volume (sets × reps × weight) per session",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            VolumeBarChart(volumes = stats.lastWorkoutsVolume)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsMetricRow(
    title1: String,
    value1: String,
    icon1: ImageVector,
    tint1: Color,
    title2: String,
    value2: String,
    icon2: ImageVector,
    tint2: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Icon(
                    imageVector = icon1,
                    contentDescription = null,
                    tint = tint1,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title1,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = value1,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Icon(
                    imageVector = icon2,
                    contentDescription = null,
                    tint = tint2,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title2,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = value2,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun VolumeBarChart(
    volumes: List<Pair<String, Double>>
) {
    if (volumes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Log workouts to display charts",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val maxVal = volumes.maxOf { it.second }
    val maxVolume = if (maxVal == 0.0) 1.0 else maxVal

    // Trigger bar animation
    var startAnimate by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = true) {
        startAnimate = true
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        volumes.forEach { (title, volume) ->
            // Animated bar height
            val targetHeight = (volume / maxVolume * 140).dp
            val animatedHeight by animateDpAsState(
                targetValue = if (startAnimate) targetHeight else 0.dp,
                animationSpec = tween(durationMillis = 1000)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Volume Value on Top
                val displayVal = if (volume >= 1000) {
                    String.format(Locale.US, "%.1fk", volume / 1000.0)
                } else {
                    String.format(Locale.US, "%.0f", volume)
                }
                
                Text(
                    text = displayVal,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))

                // The bar box with gradient
                Box(
                    modifier = Modifier
                        .width(22.dp)
                        .height(animatedHeight)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Label at bottom
                Text(
                    text = title,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

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
            val activeDates = remember(workoutDates) {
                workoutDates.map {
                    val cal = Calendar.getInstance().apply { timeInMillis = it }
                    "" + cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.MONTH) + "-" + cal.get(Calendar.DAY_OF_MONTH)
                }.toSet()
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Day of week labels
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    val days = listOf("Sun", "", "Tue", "", "Thu", "", "Sat")
                    days.forEach { day ->
                        Text(
                            text = day,
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.height(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Columns (12 weeks)
                val cellCalendar = Calendar.getInstance()
                for (weekIndex in 0 until 12) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (dayIndex in 0 until 7) {
                            cellCalendar.timeInMillis = startDateMillis
                            cellCalendar.add(Calendar.DAY_OF_YEAR, weekIndex * 7 + dayIndex)

                            val dateKey = "" + cellCalendar.get(Calendar.YEAR) + "-" + cellCalendar.get(Calendar.MONTH) + "-" + cellCalendar.get(Calendar.DAY_OF_MONTH)
                            val hasWorkout = activeDates.contains(dateKey)
                            val isFuture = cellCalendar.timeInMillis > System.currentTimeMillis()

                            val color = when {
                                isFuture -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                hasWorkout -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(color)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
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
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.surfaceVariant))
                Spacer(modifier = Modifier.width(4.dp))
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.primary))
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

