package com.unizar.sanbotbasicproject

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanbot.opensdk.function.beans.EmotionsType
import com.sanbot.opensdk.function.beans.LED
import com.unizar.sanbotbasicproject.robotControl.HardwareControl
import com.unizar.sanbotbasicproject.robotControl.SpeechControl
import com.unizar.sanbotbasicproject.robotControl.SystemControl
import com.unizar.sanbotbasicproject.ui.VoiceHud

@Composable
fun PostureScreen(
    onOptionSelected: (String) -> Unit,
    speechControl: SpeechControl,
    systemControl: SystemControl,
    hardwareControl: HardwareControl
) {
    var isListening by remember { mutableStateOf(false) }

    // Gestión del speech al entrar/salir
    DisposableEffect(Unit) {
        speechControl.onListeningStateChanged = { hardwareState ->
            isListening = hardwareState
        }
        startPostureVoiceFlow(
            speechControl = speechControl,
            onOptionSelected = onOptionSelected,
            systemControl = systemControl,
            hardwareControl = hardwareControl
        )
        onDispose {
            speechControl.onListeningStateChanged = null
        }
    }

    MaterialTheme(
        colorScheme = FitnessColorScheme,
        shapes = FitnessShapes,
        typography = FitnessTypography
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0B1120), Color(0xFF1E293B))
                    )
                )
        ) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight
            val isLandscape = screenWidth > screenHeight

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Título
                Text(
                    text = "¿Cómo prefieres hacer\n ejercicio hoy?",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = if (isLandscape) 42.sp else 46.sp,
                        lineHeight = if (isLandscape) 38.sp else 44.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Tarjetas de opciones
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PostureOptionCard(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(0.85f),
                        title = "Sentado en silla",
                        subtitle = "Más seguro",
                        icon = Icons.Default.Chair,
                        gradientColors = listOf(Color(0xFFF7941D), Color(0xFFFF6D00)),
                        iconTint = Color.White,
                        onClick = {
                            speechControl.stopListening()
                            onOptionSelected("SITTING")
                        }
                    )

                    PostureOptionCard(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(0.85f),
                        title = "De pie",
                        subtitle = "Para equilibrio",
                        icon = Icons.Default.Person,
                        gradientColors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7)),
                        iconTint = Color.White,
                        onClick = {
                            speechControl.stopListening()
                            onOptionSelected("STANDING")
                        }
                    )
                }

                // VoiceHud
                VoiceHud(
                    isListening = isListening,
                    helpText = "O dime: \"Ejercicio sentado\" o \"Ejercicio de pie\"",
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun PostureOptionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(16.dp, RoundedCornerShape(32.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(gradientColors),
                    RoundedCornerShape(32.dp)
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icono dentro de círculo decorativo
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = iconTint
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 36.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 24.sp
                    ),
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

fun startPostureVoiceFlow(
    speechControl: SpeechControl,
    onOptionSelected: (String) -> Unit,
    systemControl: SystemControl,
    hardwareControl: HardwareControl
) {
    systemControl.setEmotion(EmotionsType.QUESTION)
    hardwareControl.setEarsLED(LED.MODE_BLUE)

    speechControl.ask("¿Cómo prefieres hacer ejercicio hoy? ¿sentado o de pie?") { text ->
        val texto = text.lowercase()
        when {
            "sentado" in texto || "silla" in texto -> {
                speechControl.stopListening()
                onOptionSelected("SITTING")
            }
            "pie" in texto || "levantado" in texto || "parado" in texto -> {
                speechControl.stopListening()
                onOptionSelected("STANDING")
            }
        }
    }
}

fun stopPostureVoiceFlow(
    speechControl: SpeechControl
) {
    speechControl.stopListening()
}