package com.unizar.sanbotbasicproject.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VoiceHud(
    isListening: Boolean,
    helpText: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "listening")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Surface(
        color = if (isListening) Color(0xFF2C2C2C) else Color(0xFF1E1E1E),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.width(520.dp),
        tonalElevation = if (isListening) 8.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isListening) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .scale(scale)
                            .background(Color(0x224CAF50), CircleShape)
                    )
                }
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = if (isListening) Color(0xFF4CAF50) else Color.White,
                    modifier = Modifier.size(40.dp).scale(if (isListening) 1.1f else 1f)
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = helpText,
                color = if (isListening) Color.White else Color.Gray,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
