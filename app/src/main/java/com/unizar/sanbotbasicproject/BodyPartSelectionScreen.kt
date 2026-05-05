package com.unizar.sanbotbasicproject

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
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
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.AccessibilityNew
@Composable
fun BodyPartSelectionScreen(
    onBack: () -> Unit,
    onOptionSelected: (String) -> Unit,
    speechControl: SpeechControl,
    systemControl: SystemControl,
    hardwareControl: HardwareControl
) {
    var isListening by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        speechControl.onListeningStateChanged = { hardwareState ->
            isListening = hardwareState
        }
        startBodyPartVoiceFlow(
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
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Botón Atrás estilizado
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

                // Título
                Text(
                    text = "¿Qué parte del cuerpo\nquieres mover hoy?",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 48.sp,
                        lineHeight = 56.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Fila de Tarjetas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BodyPartCard(
                        modifier = Modifier.weight(1f),
                        title = "Brazos y\nEspalda",
                        icon = Icons.Default.FitnessCenter,
                        gradientColors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7)),
                        onClick = {
                            speechControl.stopListening()
                            onOptionSelected("ARMS_BACK")
                        }
                    )
                    BodyPartCard(
                        modifier = Modifier.weight(1f),
                        title = "Piernas y\nPies",
                        icon = Icons.AutoMirrored.Filled.DirectionsRun,
                        gradientColors = listOf(Color(0xFF4ADE80), Color(0xFF16A34A)),
                        onClick = {
                            speechControl.stopListening()
                            onOptionSelected("LEGS_FEET")
                        }
                    )
                    BodyPartCard(
                        modifier = Modifier.weight(1f),
                        title = "Cuerpo\nEntero",
                        icon = Icons.Default.AccessibilityNew,
                        gradientColors = listOf(Color(0xFFC084FC), Color(0xFF9333EA)),
                        onClick = {
                            speechControl.stopListening()
                            onOptionSelected("FULL_BODY")
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // VoiceHud
                VoiceHud(
                    isListening = isListening,
                    helpText = "Dime: \"Brazos\", \"Piernas\" o \"Cuerpo entero\"",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun BodyPartCard(
    modifier: Modifier = Modifier,
    title: String,
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
                // Icono dentro de círculo decorativo
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
            }
        }
    }
}

fun startBodyPartVoiceFlow(
    speechControl: SpeechControl,
    onOptionSelected: (String) -> Unit,
    systemControl: SystemControl,
    hardwareControl: HardwareControl
) {
    systemControl.setEmotion(EmotionsType.QUESTION)
    hardwareControl.setEarsLED(LED.MODE_BLUE)

    speechControl.ask("¿Qué parte del cuerpo quieres mover hoy? Brazos, piernas o el cuerpo entero") { text ->
        val texto = text.lowercase()
        when {
            "brazo" in texto || "espalda" in texto || "arriba" in texto -> {
                speechControl.stopListening()
                onOptionSelected("ARMS_BACK")
            }
            "pierna" in texto || "pie" in texto || "abajo" in texto -> {
                speechControl.stopListening()
                onOptionSelected("LEGS_FEET")
            }
            "cuerpo" in texto || "entero" in texto || "todo" in texto -> {
                speechControl.stopListening()
                onOptionSelected("FULL_BODY")
            }
        }
    }
}

fun stopBodyPartVoiceFlow(
    speechControl: SpeechControl
) {
    speechControl.stopListening()
}