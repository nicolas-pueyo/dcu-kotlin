package com.unizar.sanbotbasicproject

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanbot.opensdk.function.beans.EmotionsType
import com.sanbot.opensdk.function.beans.LED
import com.unizar.sanbotbasicproject.robotControl.HardwareControl
import com.unizar.sanbotbasicproject.robotControl.SystemControl
import kotlinx.coroutines.delay
import java.util.Locale

data class Exercise(
    val name: String,
    val durationSeconds: Int,
    val icon: ImageVector = Icons.Default.Accessibility
)

object RoutineProvider {
    fun getRoutine(posture: String, bodyPart: String): List<Exercise> {
        return when (bodyPart) {
            "ARMS_BACK" -> if (posture == "SITTING") {
                listOf(
                    Exercise("Estiramiento de brazos lateral", 30, Icons.Default.Accessibility),
                    Exercise("Rotación de hombros", 30, Icons.Default.AccessibilityNew),
                    Exercise("Estiramiento de cuello", 20, Icons.Default.SelfImprovement)
                )
            } else {
                listOf(
                    Exercise("Elevación de brazos", 45, Icons.Default.Accessibility),
                    Exercise("Apertura de pecho", 45, Icons.Default.AccessibilityNew),
                    Exercise("Estiramiento de tríceps", 30, Icons.Default.Accessibility)
                )
            }
            "LEGS_FEET" -> if (posture == "SITTING") {
                listOf(
                    Exercise("Extensión de rodilla", 40, Icons.Default.AirlineSeatLegroomExtra),
                    Exercise("Círculos con tobillos", 30, Icons.Default.DirectionsWalk),
                    Exercise("Elevación de talones sentado", 40, Icons.Default.AirlineSeatLegroomNormal)
                )
            } else {
                listOf(
                    Exercise("Sentadillas suaves", 45, Icons.Default.AccessibilityNew),
                    Exercise("Elevación de rodillas", 45, Icons.Default.DirectionsRun),
                    Exercise("Equilibrio sobre una pierna", 30, Icons.Default.Accessibility)
                )
            }
            "FULL_BODY" -> if (posture == "SITTING") {
                listOf(
                    Exercise("Marcha sentado", 60, Icons.Default.DirectionsRun),
                    Exercise("Torsión de tronco", 45, Icons.Default.AccessibilityNew),
                    Exercise("Coordinación brazos-piernas", 45, Icons.Default.Accessibility)
                )
            } else {
                listOf(
                    Exercise("Marcha en el sitio", 60, Icons.Default.DirectionsWalk),
                    Exercise("Estiramiento lateral completo", 45, Icons.Default.Accessibility),
                    Exercise("Movimiento circular de cadera", 45, Icons.Default.AccessibilityNew)
                )
            }
            else -> listOf(Exercise("Calentamiento suave", 30, Icons.Default.Accessibility))
        }
    }
}

@Composable
fun ExerciseExecutionScreen(
    exercise: Exercise,
    onExerciseFinished: (Int) -> Unit, // pass duration spent
    onFinishRoutine: (Int) -> Unit,
    systemControl: SystemControl,
    hardwareControl: HardwareControl,
    externalPauseTrigger: Int = 0 // Disparador externo para pausa (ej. desde la cabeza del robot)
) {
    var timeLeft by remember { mutableIntStateOf(exercise.durationSeconds) }
    var isPaused by remember { mutableStateOf(false) }
    var totalSpentInThisExercise by remember { mutableIntStateOf(0) }

    // Reacción al disparador externo (toque en la cabeza)
    LaunchedEffect(externalPauseTrigger) {
        if (externalPauseTrigger > 0) {
            isPaused = !isPaused
        }
    }

    // Reacción física al iniciar el ejercicio o cambiar estado de pausa
    LaunchedEffect(isPaused) {
        if (!isPaused) {
            systemControl.setEmotion(EmotionsType.SMILE)
            hardwareControl.setEarsLED(LED.MODE_BLUE)
        } else {
            systemControl.setEmotion(EmotionsType.NORMAL)
            hardwareControl.setEarsLED(LED.MODE_YELLOW)
        }
    }

    LaunchedEffect(key1 = timeLeft, key2 = isPaused) {
        if (!isPaused && timeLeft > 0) {
            delay(1000L)
            timeLeft--
            totalSpentInThisExercise++
        } else if (timeLeft == 0) {
            onExerciseFinished(totalSpentInThisExercise)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(24.dp)
    ) {
        // Botón de ayuda arriba a la derecha (estilo flotante oscuro)
        Surface(
            modifier = Modifier.align(Alignment.TopEnd),
            color = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(20.dp),
            onClick = { /* Ayuda */ }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pedir ayuda", color = Color.White, fontSize = 14.sp)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = exercise.name,
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Área central con el icono/ilustración representativo
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = exercise.icon,
                    contentDescription = null,
                    modifier = Modifier.size(240.dp),
                    tint = Color.White
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Tiempo: 0:${timeLeft.toString().padStart(2, '0')}",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { isPaused = !isPaused },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isPaused) Color(0xFF4CAF50) else Color(0xFFFF941D)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(84.dp).width(280.dp)
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isPaused) "Reanudar" else "Pausar",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                TextButton(
                    onClick = { onFinishRoutine(totalSpentInThisExercise) },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Terminar rutina por hoy", color = Color.Gray, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun RestScreen(
    onContinue: () -> Unit,
    onFinishEarly: () -> Unit,
    systemControl: SystemControl,
    hardwareControl: HardwareControl
) {
    var timeLeft by remember { mutableIntStateOf(90) } // 1:30

    // Reacción física al entrar en el descanso
    DisposableEffect(Unit) {
        systemControl.setEmotion(EmotionsType.NORMAL)
        hardwareControl.setEarsLED(LED.MODE_GREEN)
        onDispose { }
    }

    LaunchedEffect(key1 = timeLeft) {
        if (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        } else {
            onContinue()
        }
    }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "¡Buen trabajo!\nTómate un respiro",
                color = Color.White,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 52.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                color = Color.White,
                fontSize = 100.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(64.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Button(
                    onClick = onFinishEarly,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7043)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(88.dp).weight(1f)
                ) {
                    Text("Estoy cansado,\nterminar por hoy", textAlign = TextAlign.Center, fontSize = 20.sp, color = Color.White)
                }

                Button(
                    onClick = onContinue,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(88.dp).weight(1f)
                ) {
                    Text("Continuar ahora", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun RoutineFinishedScreen(
    totalMinutes: Int,
    completed: Boolean,
    onBackToStart: () -> Unit,
    systemControl: SystemControl,
    hardwareControl: HardwareControl
) {
    // Reacción física al terminar la rutina
    DisposableEffect(Unit) {
        systemControl.setEmotion(EmotionsType.LAUGHTER)
        // Parpadeo aleatorio festivo (300ms entre cambios, 5 colores)
        hardwareControl.setEarsLED(LED.MODE_FLICKER_RANDOM, 3, 5)
        onDispose { }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(120.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = if (completed) "¡Felicidades, has\ncompletado tu rutina!" else "¡Buen trabajo!\nHas terminado por hoy",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 48.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            Surface(
                color = Color(0xFF1E1E1E),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.width(280.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF56CCF2), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tiempo:", color = Color.Gray, fontSize = 20.sp)
                    Text(
                        text = "$totalMinutes minutos",
                        color = Color(0xFF56CCF2),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            Button(
                onClick = onBackToStart,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(68.dp).width(320.dp)
            ) {
                Text("Volver al inicio", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
