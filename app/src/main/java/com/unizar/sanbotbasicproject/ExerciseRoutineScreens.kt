package com.unizar.sanbotbasicproject

import android.net.Uri
import android.widget.VideoView
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.sanbot.opensdk.function.beans.EmotionsType
import com.sanbot.opensdk.function.beans.LED
import com.unizar.sanbotbasicproject.robotControl.HardwareControl
import com.unizar.sanbotbasicproject.robotControl.SystemControl
import com.unizar.sanbotbasicproject.robotControl.ProjectorControl
import kotlinx.coroutines.delay
import java.util.Locale

data class Exercise(
    val name: String,
    val durationSeconds: Int,
    val videoResName: String,
    val icon: ImageVector = Icons.Default.Accessibility
)

object RoutineProvider {
    fun getRoutine(posture: String, bodyPart: String): List<Exercise> {
        return when (posture) {
            "SITTING" -> when (bodyPart) {
                "ARMS_BACK" -> listOf(
                    Exercise("El Abrazo", 30, "sitting_arms_abrazo"),
                    Exercise("Limpiar el Cristal", 30, "sitting_arms_limpiar_cristal"),
                    Exercise("Juntar Escápulas", 30, "sitting_arms_juntar_escapulas"),
                    Exercise("Escalera al Cielo", 30, "sitting_arms_escalera_cielo"),
                    Exercise("Círculos de Hombros", 30, "sitting_arms_circulos_hombros")
                )
                "LEGS_FEET" -> listOf(
                    Exercise("Extensiones de Cuádriceps", 30, "sitting_legs_extensiones_cuadriceps"),
                    Exercise("Círculos de Tobillo", 30, "sitting_legs_circulos_tobillo"),
                    Exercise("Pisar el Acelerador", 30, "sitting_legs_pisar_acelerador"),
                    Exercise("Rodilla al Pecho", 30, "sitting_legs_rodilla_pecho"),
                    Exercise("Apertura de Cadera", 30, "sitting_legs_apertura_cadera")
                )
                "FULL_BODY" -> listOf(
                    Exercise("Marcha con Braceo", 30, "sitting_full_marcha_braceo"),
                    Exercise("Torsión de Cintura", 30, "sitting_full_torsion_cintura"),
                    Exercise("El Remador", 30, "sitting_full_el_remador"),
                    Exercise("Inclinación Lateral", 30, "sitting_full_inclinacion_lateral"),
                    Exercise("Coger la Fruta", 30, "sitting_full_coger_fruta")
                )
                else -> emptyList()
            }
            "STANDING" -> when (bodyPart) {
                "ARMS_BACK" -> listOf(
                    Exercise("Flexiones de Pared", 30, "standing_arms_flexiones_pared"),
                    Exercise("El Reloj", 30, "standing_arms_el_reloj"),
                    Exercise("Empuje Frontal", 30, "standing_arms_empuje_frontal"),
                    Exercise("Vuelo Lateral", 30, "standing_arms_vuelo_lateral"),
                    Exercise("Nadador de Espalda", 30, "standing_arms_nadador_espalda")
                )
                "LEGS_FEET" -> listOf(
                    Exercise("Puntillas con apoyo", 30, "standing_legs_puntillas"),
                    Exercise("Flexión de Rodilla Atrás", 30, "standing_legs_flexion_rodilla"),
                    Exercise("Separación Lateral", 30, "standing_legs_separacion_lateral"),
                    Exercise("Sentadilla de Sofá", 30, "standing_legs_sentadilla_sofa"),
                    Exercise("Balanceo de Peso", 30, "standing_legs_balanceo_peso")
                )
                "FULL_BODY" -> listOf(
                    Exercise("El Péndulo", 30, "standing_full_el_pendulo"),
                    Exercise("Paso Adelante y Atrás", 30, "standing_full_paso_adelante"),
                    Exercise("Estiramiento de Sol", 30, "standing_full_estiramiento_sol"),
                    Exercise("Rodilla-Codo Opuesto", 30, "standing_full_rodilla_codo"),
                    Exercise("Caminata en el Sitio", 30, "standing_full_caminata_sitio")
                )
                else -> emptyList()
            }
            else -> emptyList()
        }
    }
}

@Composable
fun ExerciseExecutionScreen(
    exercise: Exercise,
    onExerciseFinished: (Int) -> Unit,
    onFinishRoutine: (Int) -> Unit,
    systemControl: SystemControl,
    hardwareControl: HardwareControl,
    externalPauseTrigger: Int = 0
) {
    val context = LocalContext.current
    var timeLeft by remember { mutableIntStateOf(exercise.durationSeconds) }
    var isPaused by remember { mutableStateOf(false) }
    var totalSpentInThisExercise by remember { mutableIntStateOf(0) }

    val videoResId = remember(exercise.videoResName) {
        context.resources.getIdentifier(exercise.videoResName, "raw", context.packageName)
    }

    LaunchedEffect(externalPauseTrigger) {
        if (externalPauseTrigger > 0) { isPaused = !isPaused }
    }

    LaunchedEffect(isPaused) {
        if (!isPaused) {
            systemControl.setEmotion(EmotionsType.SMILE)
            hardwareControl.setEarsLED(LED.MODE_BLUE)
        } else {
            systemControl.setEmotion(EmotionsType.NORMAL)
            hardwareControl.setEarsLED(LED.MODE_YELLOW)
        }
    }

    LaunchedEffect(exercise) {
        timeLeft = exercise.durationSeconds
        totalSpentInThisExercise = 0
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
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (videoResId != 0) {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                val uri = Uri.parse("android.resource://${ctx.packageName}/$videoResId")
                                setVideoURI(uri)
                                setOnPreparedListener { mp ->
                                    mp.isLooping = true
                                    if (!isPaused) start()
                                }
                            }
                        },
                        update = { view ->
                            if (isPaused) view.pause() else view.start()
                        },
                        modifier = Modifier.size(width = 640.dp, height = 360.dp)
                    )
                } else {
                    Icon(
                        imageVector = exercise.icon,
                        contentDescription = null,
                        modifier = Modifier.size(240.dp),
                        tint = Color.White
                    )
                }
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
                    Text(text = if (isPaused) "Reanudar" else "Pausar", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                
                TextButton(onClick = { onFinishRoutine(totalSpentInThisExercise) }, modifier = Modifier.padding(top = 16.dp)) {
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
    var timeLeft by remember { mutableIntStateOf(90) }

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
        modifier = Modifier.fillMaxSize().background(Color(0xFF121212)).padding(24.dp),
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
                    Text("Terminar por hoy", textAlign = TextAlign.Center, fontSize = 20.sp, color = Color.White)
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
    hardwareControl: HardwareControl,
    projectorControl: ProjectorControl
) {
    DisposableEffect(Unit) {
        systemControl.setEmotion(EmotionsType.LAUGHTER)
        hardwareControl.setEarsLED(LED.MODE_FLICKER_RANDOM, 3, 5)
        // Apagamos el láser con seguridad al terminar
        projectorControl.switchProjector(false)
        onDispose { }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF121212)).padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFFFD700), modifier = Modifier.size(120.dp))
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = if (completed) "¡Felicidades!\nHas completado tu rutina" else "¡Buen trabajo!\nHas terminado por hoy",
                color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 48.sp
            )
            Spacer(modifier = Modifier.height(48.dp))
            Surface(color = Color(0xFF1E1E1E), shape = RoundedCornerShape(20.dp), modifier = Modifier.width(280.dp)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Schedule, null, tint = Color(0xFF56CCF2), modifier = Modifier.size(32.dp))
                    Text("Tiempo:", color = Color.Gray, fontSize = 20.sp)
                    Text(text = "$totalMinutes minutos", color = Color(0xFF56CCF2), fontSize = 36.sp, fontWeight = FontWeight.Bold)
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
