package com.unizar.sanbotbasicproject

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanbot.opensdk.function.beans.EmotionsType
import com.sanbot.opensdk.function.beans.LED
import com.unizar.sanbotbasicproject.robotControl.HardwareControl
import com.unizar.sanbotbasicproject.robotControl.ProjectorControl
import com.unizar.sanbotbasicproject.robotControl.SpeechControl
import com.unizar.sanbotbasicproject.robotControl.SystemControl
import kotlinx.coroutines.delay

@Composable
fun ExercisePreparationScreen(
    posture: String, // "SITTING" or "STANDING"
    bodyPart: String,
    onCountdownFinished: () -> Unit,
    systemControl: SystemControl,
    hardwareControl: HardwareControl,
    projectorControl: ProjectorControl,
    projectorBrightness: Int,
    speechControl: SpeechControl
) {
    var timeLeft by remember { mutableIntStateOf(12) }
    val totalTime = 12

    // Animación del progreso circular
    val progress by animateFloatAsState(
        targetValue = timeLeft.toFloat() / totalTime,
        animationSpec = tween(durationMillis = 1000, easing = androidx.compose.animation.core.LinearEasing),
        label = "prepProgress"
    )

    // Nombre del ejercicio basado en la parte del cuerpo
    val exerciseName = when (bodyPart) {
        "ARMS_BACK" -> "Brazos y Espalda"
        "LEGS_FEET" -> "Piernas y Pies"
        "FULL_BODY" -> "Cuerpo Entero"
        else -> bodyPart
    }

    // Mensaje basado en la postura
    val instructionMessage = if (posture == "SITTING") {
        "Busca una silla y ponte cómodo.\nVamos a empezar"
    } else {
        "Ponte de pie y busca un lugar despejado.\nVamos a empezar"
    }

    // Reacción física inicial al entrar en la pantalla
    DisposableEffect(Unit) {
        systemControl.setEmotion(EmotionsType.SURPRISE)
        hardwareControl.setEarsLED(LED.MODE_FLICKER_BLUE, 10, 0)

        // Configuramos el proyector a la pared
        projectorControl.expectedSetup()
        // Encendemos el láser
        projectorControl.switchProjector(true)
        speechControl.talk(instructionMessage)
        onDispose {
            // No lo apagamos aquí, porque queremos que siga encendido
            // al pasar a la pantalla de ejecución.
        }
    }

    // Temporizador
    LaunchedEffect(key1 = timeLeft) {
        if (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        } else {
            // Después de los 12 segundos, aplicar el brillo
            projectorControl.setBrightness(projectorBrightness)
            onCountdownFinished()
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
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Instrucción
                Text(
                    text = instructionMessage,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 42.sp,
                        lineHeight = 50.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Contador circular animado
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(240.dp),
                        strokeWidth = 14.dp,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.White.copy(alpha = 0.15f),
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        text = "$timeLeft",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 100.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Siguiente ejercicio info
                Text(
                    text = "Siguiente ejercicio:",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 32.sp
                    ),
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = exerciseName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 40.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}