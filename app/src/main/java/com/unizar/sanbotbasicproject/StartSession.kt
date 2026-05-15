package com.unizar.sanbotbasicproject

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
fun StartSession(
    onStartClick: () -> Unit,
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
    val isListening = speechControl.isListening

    // Animación de pulso suave en el botón principal
    val infiniteTransition = rememberInfiniteTransition(label = "startPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    DisposableEffect(speechControl) {
        startStartSessionVoiceFlow(
            speechControl = speechControl,
            onStartClick = onStartClick,
            systemControl = systemControl,
            hardwareControl = hardwareControl
        )

        onDispose {
            speechControl.stopListening()
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
                        colors = listOf(Color(0xFF0B1120), Color(0xFF1E3A8A))
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

            // Contenido principal (columna central)
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mensaje de bienvenida
                Text(
                    text = "Tu compañero de\nentrenamiento",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 40.sp,
                        lineHeight = 48.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Botón principal de comenzar
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .scale(pulseScale)
                        .shadow(20.dp, RoundedCornerShape(48.dp))
                ) {
                    Button(
                        onClick = {
                            speechControl.stopListening()
                            onStartClick()
                        },
                        modifier = Modifier
                            .size(width = 560.dp, height = 300.dp)
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.3f),
                                        Color.White.copy(alpha = 0.0f)
                                    )
                                ),
                                shape = RoundedCornerShape(48.dp)
                            ),
                        shape = RoundedCornerShape(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF2563EB),
                                            Color(0xFF1E40AF)
                                        )
                                    ),
                                    RoundedCornerShape(48.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MonitorHeart,
                                        contentDescription = null,
                                        modifier = Modifier.size(70.dp),
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "Comenzar Ejercicio",
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 36.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                VoiceHud(
                    isListening = isListening,
                    helpText = "O dime: \"Empezar ejercicio\""
                )
            }
        }
    }
}

fun startStartSessionVoiceFlow(
    speechControl: SpeechControl,
    onStartClick: () -> Unit,
    systemControl: SystemControl,
    hardwareControl: HardwareControl
) {
    systemControl.setEmotion(EmotionsType.SMILE)
    hardwareControl.setEarsLED(LED.MODE_BLUE)

    speechControl.ask("Hola, pulsa el botón o tócame la cabeza para abrir el micrófono") { text ->
        val textoLimpio = text.lowercase()
        Log.d("Speech Control", "Texto limpio: $textoLimpio")
        if ("empezar" in textoLimpio || "ejercicio" in textoLimpio || "comenzar" in textoLimpio) {
            speechControl.stopListening()
            onStartClick()
        }
    }
}
