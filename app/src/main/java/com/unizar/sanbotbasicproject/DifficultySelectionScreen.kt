package com.unizar.sanbotbasicproject

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
fun DifficultySelectionScreen(
    onBack: () -> Unit,
    onOptionSelected: (String) -> Unit,
    speechControl: SpeechControl,
    systemControl: SystemControl,
    hardwareControl: HardwareControl,
    ledBrightness: Int,
    onLedBrightnessChange: (Int) -> Unit,
    projectorBrightness: Int,
    onProjectorBrightnessChange: (Int) -> Unit,
    volume: Int,
    onVolumeChange: (Int) -> Unit
) {
    var isListening by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        speechControl.onListeningStateChanged = { hardwareState ->
            isListening = hardwareState
        }
        startDifficultyVoiceFlow(
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0B1120), Color(0xFF1E293B))
                    )
                )
                .padding(24.dp)
        ) {
            SettingsButtonWithDialog(
                ledBrightness = ledBrightness,
                onLedBrightnessChange = onLedBrightnessChange,
                projectorBrightness = projectorBrightness,
                onProjectorBrightnessChange = onProjectorBrightnessChange,
                volume = volume,
                onVolumeChange = onVolumeChange,
                modifier = Modifier.align(Alignment.TopEnd)
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.3f),
                                    Color.White.copy(alpha = 0.05f)
                                )
                            )
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Atrás",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 24.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "¿Qué dificultad\nquieres hoy?",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 48.sp,
                        lineHeight = 56.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DifficultyCard(
                        modifier = Modifier.weight(1f),
                        title = "Baja",
                        subtitle = "3 ejercicios\n30 segundos cada uno",
                        icon = Icons.Default.Eco,
                        gradientColors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7)),
                        onClick = {
                            speechControl.stopListening()
                            onOptionSelected("LOW")
                        }
                    )
                    DifficultyCard(
                        modifier = Modifier.weight(1f),
                        title = "Media",
                        subtitle = "4 ejercicios\n45 segundos cada uno",
                        icon = Icons.Default.Balance,
                        gradientColors = listOf(Color(0xFF4ADE80), Color(0xFF16A34A)),
                        onClick = {
                            speechControl.stopListening()
                            onOptionSelected("MEDIUM")
                        }
                    )
                    DifficultyCard(
                        modifier = Modifier.weight(1f),
                        title = "Alta",
                        subtitle = "5 ejercicios\n60 segundos cada uno",
                        icon = Icons.Default.LocalFireDepartment,
                        gradientColors = listOf(Color(0xFFF97316), Color(0xFFEA580C)),
                        onClick = {
                            speechControl.stopListening()
                            onOptionSelected("HIGH")
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                VoiceHud(
                    isListening = isListening,
                    helpText = "Dime: \"baja\", \"media\" o \"alta\"",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun DifficultyCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxHeight(0.85f)
            .shadow(16.dp, RoundedCornerShape(32.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(gradientColors),
                    RoundedCornerShape(32.dp)
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp,
                        lineHeight = 36.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 22.sp,
                        lineHeight = 28.sp
                    ),
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

fun startDifficultyVoiceFlow(
    speechControl: SpeechControl,
    onOptionSelected: (String) -> Unit,
    systemControl: SystemControl,
    hardwareControl: HardwareControl
) {
    systemControl.setEmotion(EmotionsType.QUESTION)
    hardwareControl.setEarsLED(LED.MODE_BLUE)

    speechControl.ask("¿Qué dificultad quieres? Baja, media o alta") { text ->
        val texto = text.lowercase()
        when {
            "baja" in texto || "facil" in texto || "fácil" in texto -> {
                speechControl.stopListening()
                onOptionSelected("LOW")
            }
            "media" in texto || "medio" in texto -> {
                speechControl.stopListening()
                onOptionSelected("MEDIUM")
            }
            "alta" in texto || "dificil" in texto || "difícil" in texto -> {
                speechControl.stopListening()
                onOptionSelected("HIGH")
            }
        }
    }
}
