package com.example.matrusneh.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.matrusneh.R
import com.example.matrusneh.ui.theme.PrimaryPink
import com.example.matrusneh.ui.theme.SoftPink
import com.example.matrusneh.ui.theme.White
import com.example.matrusneh.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun KickCounterScreen(viewModel: MainViewModel) {
    val kicksToday by viewModel.getKicksForToday().collectAsState()
    
    // Animation states
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "buttonScale"
    )

    // Breathing effect for the outer circle
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.kick_counter_title),
            style = MaterialTheme.typography.headlineMedium,
            color = PrimaryPink
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.kick_counter_subtitle),
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(64.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(250.dp)
                .scale(breathingScale)
        ) {
            // Outer glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(SoftPink.copy(alpha = 0.3f))
            )
            
            // Inner clickable button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(PrimaryPink)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null // Custom ripple/scale handled manually
                    ) {
                        isPressed = true
                        viewModel.addKick()
                    }
            ) {
                Text(
                    text = "Tap",
                    style = MaterialTheme.typography.headlineLarge,
                    color = White
                )
            }
        }
        
        // Reset scale after click
        LaunchedEffect(isPressed) {
            if (isPressed) {
                delay(100)
                isPressed = false
            }
        }

        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = stringResource(R.string.kick_counter_total, kicksToday),
            style = MaterialTheme.typography.titleLarge
        )
    }
}
