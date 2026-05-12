package com.example.matrusneh.ui.screens
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.matrusneh.ui.viewmodel.HealthAlert
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.matrusneh.R
import com.example.matrusneh.ui.theme.PastelLavender
import com.example.matrusneh.ui.theme.PastelPink
import com.example.matrusneh.ui.theme.PrimaryPink
import com.example.matrusneh.ui.theme.SoftPink
import com.example.matrusneh.ui.theme.White
import com.example.matrusneh.ui.theme.White
import com.example.matrusneh.ui.viewmodel.MainViewModel
import com.example.matrusneh.ui.viewmodel.ReminderPeriod
import com.example.matrusneh.ui.viewmodel.SmartReminder
import com.example.matrusneh.worker.ReminderWorker
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun HomeScreen(viewModel: MainViewModel) {

    val user by viewModel.user.collectAsState()
    val kicksToday by viewModel.kicksToday.collectAsState()
    val healthAlerts by viewModel.healthAlerts.collectAsState()
    val smartReminder by viewModel.smartReminder.collectAsState()
    val context = LocalContext.current

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    val infiniteTransition = rememberInfiniteTransition(label = "floatingBanner")

    val bannerOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bannerOffset"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        // TOP BANNER
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(1000)) + expandVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SoftPink.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Banner",
                    modifier = Modifier
                        .size(100.dp)
                        .offset(y = bannerOffset.dp),
                    tint = PrimaryPink
                )
            }
        }

        // GREETING
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(800))
        ) {
            Column {
                Text(
                    text = stringResource(
                        R.string.home_greeting,
                        user?.name ?: "Amma"
                    ),
                    style = MaterialTheme.typography.headlineMedium,
                    color = PrimaryPink
                )

                Text(
                    text = stringResource(R.string.home_pregnancy_week),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // PREGNANCY CARD
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(1000))
        ) {
            PregnancyTrackerCard()
        }

        Spacer(modifier = Modifier.height(24.dp))

        // KICK + CHECKUP
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { 100 },
                animationSpec = tween(500, 200)
            ) + fadeIn()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HomeCard(
                    title = stringResource(R.string.card_kick_count),
                    value = kicksToday.toString(),
                    modifier = Modifier.weight(1f),
                    backgroundColor = SoftPink
                )

                HomeCard(
                    title = stringResource(R.string.card_next_checkup),
                    value = "12 Days",
                    modifier = Modifier.weight(1f),
                    backgroundColor = SoftPink,

                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // HEALTH ALERTS SECTION
        AnimatedVisibility(
            visible = isVisible && healthAlerts.isNotEmpty(),
            enter = fadeIn(tween(500)) + expandVertically()
        ) {
            Column {
                Text(
                    text = stringResource(R.string.card_health_alerts),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = PrimaryPink
                )
                AlertSection(alerts = healthAlerts)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // SMART REMINDER CARD
        val isRemindersEnabled = user?.remindersEnabled ?: false
        AnimatedVisibility(
            visible = isVisible && isRemindersEnabled && smartReminder != null,
            enter = fadeIn(tween(500)) + expandVertically(),
            exit = fadeOut(tween(500)) + androidx.compose.animation.shrinkVertically()
        ) {
            smartReminder?.let { reminder ->
                SmartReminderCard(reminder = reminder)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // MOTIVATION
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { 100 },
                animationSpec = tween(500, 600)
            ) + fadeIn()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PrimaryPink.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = stringResource(R.string.card_motivation),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = PrimaryPink
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))


        // WATER TRACKER (✔️ SINGLE CLEAN PLACEMENT)
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { 100 },
                animationSpec = tween(500, 700)
            ) + fadeIn()
        ) {
            WaterTrackerCard(viewModel = viewModel)
        }

        Spacer(modifier = Modifier.height(24.dp))


    }
}

@Composable
fun AlertSection(alerts: List<HealthAlert>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        alerts.forEach { alert ->
            key(alert.id) {
                AlertCard(alert)
            }
        }
    }
}

@Composable
fun AlertCard(alert: HealthAlert) {
    val backgroundColor = if (alert.isDanger) Color(0xFFFFEBEE) else PastelLavender
    val iconColor = if (alert.isDanger) Color(0xFFD32F2F) else PrimaryPink
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (alert.isDanger) 4.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = alert.emoji, fontSize = 20.sp)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = iconColor
                )
                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun HomeCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    textColor: Color = Color(0xFF424242)

) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                color = PrimaryPink
            )
        }
    }
}
@Composable
fun SmartReminderCard(reminder: SmartReminder) {
    val backgroundColor = when (reminder.period) {
        ReminderPeriod.MORNING -> Color(0xFFE3F2FD) // Soft Blue
        ReminderPeriod.AFTERNOON -> Color(0xFFFFF3E0) // Soft Orange
        ReminderPeriod.NIGHT -> Color(0xFFF3E5F5) // Soft Purple
        else -> Color.White
    }
    
    val iconColor = when (reminder.period) {
        ReminderPeriod.MORNING -> Color(0xFF1976D2)
        ReminderPeriod.AFTERNOON -> Color(0xFFF57C00)
        ReminderPeriod.NIGHT -> Color(0xFF7B1FA2)
        else -> PrimaryPink
    }

    AnimatedContent(
        targetState = reminder,
        transitionSpec = {
            (fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { it }))
                .togetherWith(fadeOut(animationSpec = tween(500)) + slideOutVertically(targetOffsetY = { -it }))
        },
        label = "smartReminderAnimation"
    ) { currentReminder ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(iconColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = currentReminder.icon, fontSize = 28.sp)
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = when (currentReminder.period) {
                            ReminderPeriod.MORNING -> "Morning Suggestion"
                            ReminderPeriod.AFTERNOON -> "Afternoon Nutrition"
                            ReminderPeriod.NIGHT -> "Evening Rest"
                            else -> "Reminder"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = iconColor.copy(alpha = 0.7f)
                    )
                    Text(
                        text = currentReminder.message,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.DarkGray,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}
