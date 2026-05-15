package com.unizar.sanbotbasicproject

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.animation.AnimatedVisibility
import androidx.core.net.toUri
import android.widget.VideoView
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.sanbot.opensdk.function.beans.EmotionsType
import com.sanbot.opensdk.function.beans.LED
import com.unizar.sanbotbasicproject.robotControl.HardwareControl
import com.unizar.sanbotbasicproject.robotControl.ProjectorControl
import com.unizar.sanbotbasicproject.robotControl.SpeechControl
import com.unizar.sanbotbasicproject.robotControl.SystemControl
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.random.Random

// ---------------------------------------------------
// Tema oscuro personalizado
// ---------------------------------------------------
val FitnessColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    secondary = Color(0xFF4ADE80),
    background = Color(0xFF0B1120),
    surface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFF334155),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF1F5F9),
    outline = Color(0xFF475569)
)

val FitnessShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)

val FitnessTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
    displayMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium)
)

// ---------------------------------------------------
// Modelo de datos y proveedor de rutinas (sin cambios)
// ---------------------------------------------------
data class Exercise(
    val name: String,
    val durationSeconds: Int,
    val videoResName: String,
    val icon: ImageVector = Icons.Default.Accessibility
)

object RoutineProvider {
    fun getRoutine(posture: String, bodyPart: String, difficulty: String): List<Exercise> {
        val baseRoutine = when (posture) {
            "SITTING" -> when (bodyPart) {
                "ARMS_BACK" -> listOf(
                    Exercise("Elevación de hombros al techo", 30, "brazoz_espalda_sentado_ej1"),
                    Exercise("Giro de torso", 30, "brazoz_espalda_sentado_ej2"),
                    Exercise("Remo inclinado", 30, "brazoz_espalda_sentado_ej3"),
                    Exercise("Círculos de brazos e inclinación", 30, "brazoz_espalda_sentado_ej4"),
                    Exercise("Inclinación frontal con estiramiento", 30, "brazoz_espalda_sentado_ej5")
                )
                "LEGS_FEET" -> listOf(
                    Exercise("Apertura de pierna", 30, "piernas_sentado_ej1"),
                    Exercise("Flexión de cadera", 30, "piernas_sentado_ej2"),
                    Exercise("Patada horizontal", 30, "piernas_sentado_ej3"),
                    Exercise("Patada recta", 30, "piernas_sentado_ej4"),
                    Exercise("Flexión con palmada", 30, "piernas_sentado_ej5")
                )
                "FULL_BODY" -> listOf(
                    Exercise("Extensión coordinada de brazos y piernas", 30, "cuerpo_entero_sentado_ej1"),
                    Exercise("Elevación de brazo y pierna unilatera", 30, "cuerpo_entero_sentado_ej2"),
                    Exercise("Crunch abdominal sentado", 30, "cuerpo_entero_sentado_ej3"),
                    Exercise("Giro de torso pierna con brazos cruzados", 30, "cuerpo_entero_sentado_ej4"),
                    Exercise("Elevación de rodilla con palmada", 30, "cuerpo_entero_sentado_ej5")
                )
                else -> emptyList()
            }
            "STANDING" -> when (bodyPart) {
                "ARMS_BACK" -> listOf(
                    Exercise("Empuje frontal con apertura de manos", 30, "brazoz_espalda_de_pie_ej1"),
                    Exercise("Apertura lateral de brazos", 30, "brazoz_espalda_de_pie_ej2"),
                    Exercise("Elevación frontal de brazos", 30, "brazoz_espalda_de_pie_ej3"),
                    Exercise("Remo de pie inclinado", 30, "brazoz_espalda_de_pie_ej4"),
                    Exercise("Extensión de brazos hacia atrás", 30, "brazoz_espalda_de_pie_ej5")
                )
                "LEGS_FEET" -> listOf(
                    Exercise("Levantamiento de rodilla", 30, "piernas_de_pie_ej1"),
                    Exercise("Patada trasera", 30, "piernas_de_pie_ej2"),
                    Exercise("Elevación lateral de pierna", 30, "piernas_de_pie_ej3"),
                    Exercise("Flexión diagonal", 30, "piernas_de_pie_ej4"),
                    Exercise("Toque de pie", 30, "piernas_de_pie_ej5")
                )
                "FULL_BODY" -> listOf(
                    Exercise("Crunch abdominal de pie", 30, "cuerpo_entero_de_pie_ej1"),
                    Exercise("Aperturas de hombro", 30, "cuerpo_entero_de_pie_ej2"),
                    Exercise("Semi sentadilla tocando pies", 30, "cuerpo_entero_de_pie_ej3"),
                    Exercise("Zancada", 30, "cuerpo_entero_de_pie_ej4"),
                    Exercise("Remo", 30, "cuerpo_entero_de_pie_ej5")
                )
                else -> emptyList()
            }
            else -> emptyList()
        }

        val (exerciseCount, durationSeconds) = when (difficulty) {
            "LOW" -> 3 to 30
            "MEDIUM" -> 4 to 45
            "HIGH" -> 5 to 60
            else -> 5 to 30
        }

        return baseRoutine.take(exerciseCount).map { exercise ->
            exercise.copy(durationSeconds = durationSeconds)
        }
    }
}

fun getVideoResId(videoName: String): Int = when (videoName) {
    "brazoz_espalda_sentado_ej1" -> R.raw.brazoz_espalda_sentado_ej1
    "brazoz_espalda_sentado_ej2" -> R.raw.brazoz_espalda_sentado_ej2
    "brazoz_espalda_sentado_ej3" -> R.raw.brazoz_espalda_sentado_ej3
    "brazoz_espalda_sentado_ej4" -> R.raw.brazoz_espalda_sentado_ej4
    "brazoz_espalda_sentado_ej5" -> R.raw.brazoz_espalda_sentado_ej5
    "piernas_sentado_ej1" -> R.raw.piernas_sentado_ej1
    "piernas_sentado_ej2" -> R.raw.piernas_sentado_ej2
    "piernas_sentado_ej3" -> R.raw.piernas_sentado_ej3
    "piernas_sentado_ej4" -> R.raw.piernas_sentado_ej4
    "piernas_sentado_ej5" -> R.raw.piernas_sentado_ej5
    "cuerpo_entero_sentado_ej1" -> R.raw.cuerpo_entero_sentado_ej1
    "cuerpo_entero_sentado_ej2" -> R.raw.cuerpo_entero_sentado_ej2
    "cuerpo_entero_sentado_ej3" -> R.raw.cuerpo_entero_sentado_ej3
    "cuerpo_entero_sentado_ej4" -> R.raw.cuerpo_entero_sentado_ej4
    "cuerpo_entero_sentado_ej5" -> R.raw.cuerpo_entero_sentado_ej5
    "brazoz_espalda_de_pie_ej1" -> R.raw.brazoz_espalda_de_pie_ej1
    "brazoz_espalda_de_pie_ej2" -> R.raw.brazoz_espalda_de_pie_ej2
    "brazoz_espalda_de_pie_ej3" -> R.raw.brazoz_espalda_de_pie_ej3
    "brazoz_espalda_de_pie_ej4" -> R.raw.brazoz_espalda_de_pie_ej4
    "brazoz_espalda_de_pie_ej5" -> R.raw.brazoz_espalda_de_pie_ej5
    "piernas_de_pie_ej1" -> R.raw.piernas_de_pie_ej1
    "piernas_de_pie_ej2" -> R.raw.piernas_de_pie_ej2
    "piernas_de_pie_ej3" -> R.raw.piernas_de_pie_ej3
    "piernas_de_pie_ej4" -> R.raw.piernas_de_pie_ej4
    "piernas_de_pie_ej5" -> R.raw.piernas_de_pie_ej5
    "cuerpo_entero_de_pie_ej1" -> R.raw.cuerpo_entero_de_pie_ej1
    "cuerpo_entero_de_pie_ej2" -> R.raw.cuerpo_entero_de_pie_ej2
    "cuerpo_entero_de_pie_ej3" -> R.raw.cuerpo_entero_de_pie_ej3
    "cuerpo_entero_de_pie_ej4" -> R.raw.cuerpo_entero_de_pie_ej4
    "cuerpo_entero_de_pie_ej5" -> R.raw.cuerpo_entero_de_pie_ej5
    else -> 0
}

// ---------------------------------------------------
// Pantalla de ejecución de ejercicio (estilizada)
// ---------------------------------------------------
@Composable
fun ExerciseExecutionScreen(
    exercise: Exercise,
    onExerciseFinished: (Int) -> Unit,
    onFinishRoutine: (Int) -> Unit,
    systemControl: SystemControl,
    hardwareControl: HardwareControl,
    speechControl: SpeechControl,
    externalPauseTrigger: Int = 0,
) {

    var timeLeft by remember { mutableIntStateOf(exercise.durationSeconds) }
    var isPaused by remember { mutableStateOf(false) }
    var totalSpentInThisExercise by remember { mutableIntStateOf(0) }

    // Animación del progreso circular
    val animatedProgress by animateFloatAsState(
        targetValue = if (exercise.durationSeconds > 0) timeLeft.toFloat() / exercise.durationSeconds.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "progress"
    )

    val videoResId = remember(exercise.videoResName) {
        getVideoResId(exercise.videoResName)
    }

    // Sincronización con pausa externa
    LaunchedEffect(externalPauseTrigger) {
        if (externalPauseTrigger > 0) { isPaused = !isPaused }
    }

    // Emociones y luces según pausa
    LaunchedEffect(isPaused) {
        if (!isPaused) {
            systemControl.setEmotion(EmotionsType.SMILE)
            hardwareControl.setEarsLED(LED.MODE_BLUE)
        } else {
            systemControl.setEmotion(EmotionsType.NORMAL)
            hardwareControl.setEarsLED(LED.MODE_YELLOW)
        }
    }

    // Reiniciar al cambiar de ejercicio
    LaunchedEffect(exercise) {
        timeLeft = exercise.durationSeconds
        totalSpentInThisExercise = 0
        speechControl.talk("Vamos a empezar el ejercicio: ${exercise.name}")
    }

    // Temporizador
    LaunchedEffect(key1 = timeLeft, key2 = isPaused) {
        if (!isPaused && timeLeft > 0) {
            delay(1000L)
            timeLeft--
            totalSpentInThisExercise++
        } else if (timeLeft == 0) {
            onExerciseFinished(totalSpentInThisExercise)
        }
    }


    MaterialTheme(colorScheme = FitnessColorScheme, shapes = FitnessShapes, typography = FitnessTypography) {
        // Usamos Box en lugar de Column para poder poner cosas ENCIMA del video
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black) // Fondo negro para los bordes del video
        ) {
            // 1. EL VIDEO (Capa del fondo, ocupa toda la pantalla)
            if (videoResId != 0) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            val uri = "android.resource://${ctx.packageName}/$videoResId".toUri()
                            setVideoURI(uri)
                            setOnPreparedListener { mp ->
                                mp.isLooping = true
                                mp.setVolume(0f, 0f)
                                // Esto hace que el video se estire para llenar la pantalla
                                mp.setVideoScalingMode(android.media.MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                                if (!isPaused) start()
                            }
                        }
                    },
                    update = { view ->
                        if (isPaused) view.pause() else view.start()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = exercise.icon,
                        contentDescription = null,
                        modifier = Modifier.size(200.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                }
            }

            // Capa oscura que aparece solo cuando el video está en pausa
            androidx.compose.animation.AnimatedVisibility(
                visible = isPaused,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PauseCircle,
                        contentDescription = "Pausado",
                        modifier = Modifier.size(120.dp),
                        tint = Color.White
                    )
                }
            }

            // 2. EL TÍTULO (Superpuesto arriba, con un leve degradado oscuro para que se lea)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)
                        )
                    )
                    .padding(top = 24.dp, bottom = 48.dp, start = 16.dp, end = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 36.sp),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            // 3. LOS CONTROLES (Superpuestos abajo, en una sola fila)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter) // Pegado abajo
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // A la izquierda: Botón de terminar
                Button(
                    onClick = { onFinishRoutine(totalSpentInThisExercise) },
                    modifier = Modifier
                        .wrapContentWidth()
                        .height(72.dp)
                        // Le damos una sombra más pronunciada que pega con el tema oscuro
                        .shadow(12.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    // Quitamos el color de fondo por defecto para usar un Box con degradado
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp) // Importante a 0 para que el Box ocupe todo
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    // Degradado de rojo vivo a rojo oscuro (Alerta/Parar)
                                    colors = listOf(Color(0xFFEF4444), Color(0xFFB91C1C))
                                )
                            )
                            .padding(horizontal = 28.dp), // Espaciado interior del Box
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // StopCircle suele verse mucho más amigable y moderno que el Stop cuadrado solo
                            Icon(
                                imageVector = Icons.Default.StopCircle,
                                contentDescription = "Terminar Rutina",
                                modifier = Modifier.size(30.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Terminar",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold // Un poco más de peso para que destaque
                                ),
                                color = Color.White
                            )
                        }
                    }
                }

                // Al centro: Botón redondo de Pausa / Reanudar
                FloatingActionButton(
                    onClick = { isPaused = !isPaused },
                    containerColor = if (isPaused) Color(0xFF4CAF50) else Color(0xFFFF941D),
                    contentColor = Color.White,
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                }

                // A la derecha: El reloj circular (más compacto)
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(100.dp),
                        strokeWidth = 8.dp,
                        color = if (isPaused) Color(0xFFFFA726) else MaterialTheme.colorScheme.primary,
                        trackColor = Color.White.copy(alpha = 0.2f),
                        strokeCap = StrokeCap.Round
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$timeLeft",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 36.sp),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------
// Pantalla de descanso (estilizada con efecto de respiración)
// ---------------------------------------------------
@Composable
fun RestScreen(
    onContinue: () -> Unit,
    onFinishEarly: () -> Unit,
    systemControl: SystemControl,
    hardwareControl: HardwareControl,
    speechControl: SpeechControl,
    ledBrightness: Int,
    onLedBrightnessChange: (Int) -> Unit,
    projectorBrightness: Int,
    onProjectorBrightnessChange: (Int) -> Unit,
    volume: Int,
    onVolumeChange: (Int) -> Unit
) {
    var timeLeft by remember { mutableIntStateOf(30) }

    LaunchedEffect(Unit) {
        speechControl.talk("Tómate un respiro. Respira hondo y relájate durante 30 segundos.")
    }

    // Efecto de respiración
    val infiniteTransition = rememberInfiniteTransition(label = "breath")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )

    // Temporizador circular
    val animatedProgress by animateFloatAsState(
        targetValue = timeLeft / 30f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "restProgress"
    )

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

    MaterialTheme(colorScheme = FitnessColorScheme, shapes = FitnessShapes, typography = FitnessTypography) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0B1120), Color(0xFF0F3460))
                    )
                )
                .padding(24.dp)
        ) {
            // Círculo de fondo con efecto de respiración
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(400.dp)
                        .scale(breathScale)
                        .background(Color.White.copy(alpha = 0.03f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .scale(breathScale * 0.9f)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                )
            }

            // Contenido principal
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(0.3f))

                Icon(
                    Icons.Default.SelfImprovement,
                    contentDescription = null,
                    modifier = Modifier.size(90.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "¡Buen trabajo!\nTómate un respiro",
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 44.sp),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    lineHeight = 52.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Temporizador circular
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(240.dp),
                        strokeWidth = 16.dp,
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = Color.White.copy(alpha = 0.15f),
                        strokeCap = StrokeCap.Round
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                            style = MaterialTheme.typography.displaySmall.copy(fontSize = 64.sp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            onFinishEarly()
                        },
                        modifier = Modifier
                            .height(84.dp)
                            .weight(1f)
                            .shadow(12.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFFF7043), Color(0xFFD32F2F))
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Terminar por hoy",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 24.sp),
                                textAlign = TextAlign.Center,
                                color = Color.White
                            )
                        }
                    }

                    Button(
                        onClick = {
                            onContinue()
                        },
                        modifier = Modifier
                            .height(84.dp)
                            .weight(1f)
                            .shadow(12.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF4ADE80), Color(0xFF22C55E))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Continuar ahora",
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(0.3f))
            }

            SettingsButtonWithDialog(
                ledBrightness = ledBrightness,
                onLedBrightnessChange = onLedBrightnessChange,
                projectorBrightness = projectorBrightness,
                onProjectorBrightnessChange = onProjectorBrightnessChange,
                volume = volume,
                onVolumeChange = onVolumeChange,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

// ---------------------------------------------------
// Pantalla de finalización (con trofeo y confeti)
// ---------------------------------------------------
@Composable
fun RoutineFinishedScreen(
    totalTimeInSeconds: Int,
    completed: Boolean,
    onBackToStart: () -> Unit,
    systemControl: SystemControl,
    hardwareControl: HardwareControl,
    projectorControl: ProjectorControl,
    speechControl: SpeechControl,
    ledBrightness: Int,
    onLedBrightnessChange: (Int) -> Unit,
    projectorBrightness: Int,
    onProjectorBrightnessChange: (Int) -> Unit,
    volume: Int,
    onVolumeChange: (Int) -> Unit
) {
    // Animación del trofeo
    val trophyScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "trophy"
    )

    // Confeti (partículas animadas)
    var showConfetti by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        showConfetti = true
    }

    DisposableEffect(Unit) {
        systemControl.setEmotion(EmotionsType.LAUGHTER)
        hardwareControl.setEarsLED(LED.MODE_FLICKER_RANDOM, 3, 5)
        onDispose {
            projectorControl.switchProjector(false)
        }
    }

    // Mensaje de voz según si completó toda la rutina o terminó antes
    LaunchedEffect(Unit) {
        val mensaje = if (completed) {
            "¡Felicidades! Has completado toda la rutina. Eres increíble."
        } else {
            "Buen trabajo por hoy. Has hecho un gran esfuerzo. ¡Sigue así!"
        }
        speechControl.talk(mensaje)
    }

    val m = totalTimeInSeconds / 60
    val s = totalTimeInSeconds % 60
    val finalTimeDisplay = if (m > 0) {
        String.format(Locale.getDefault(), "%d:%02d min", m, s)
    } else {
        "$s segundos"
    }

    MaterialTheme(colorScheme = FitnessColorScheme, shapes = FitnessShapes, typography = FitnessTypography) {
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
            // Capa de confeti encima de todo
            AnimatedVisibility(
                visible = showConfetti,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ConfettiView()
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(0.2f))

                // Trofeo animado
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = "Trofeo",
                    modifier = Modifier
                        .size(200.dp)
                        .scale(trophyScale),
                    tint = Color(0xFFFFD700)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (completed) "¡Felicidades!\nHas completado tu rutina" else "¡Buen trabajo!\nHas terminado por hoy",
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 42.sp),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    lineHeight = 50.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Tarjeta de resumen
                Card(
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.width(360.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Tiempo total",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 40.sp),
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = finalTimeDisplay,
                            style = MaterialTheme.typography.displaySmall.copy(fontSize = 48.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Botón volver
                Button(
                    onClick = onBackToStart,
                    modifier = Modifier
                        .height(84.dp)
                        .fillMaxWidth(0.45f)
                        .shadow(12.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF2563EB), Color(0xFF1E40AF))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Volver al inicio",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(0.2f))
            }

            SettingsButtonWithDialog(
                ledBrightness = ledBrightness,
                onLedBrightnessChange = onLedBrightnessChange,
                projectorBrightness = projectorBrightness,
                onProjectorBrightnessChange = onProjectorBrightnessChange,
                volume = volume,
                onVolumeChange = onVolumeChange,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

// ---------------------------------------------------
// Componente de confeti (partículas simples con Canvas)
// ---------------------------------------------------
@Composable
private fun ConfettiView() {
    val particles = remember {
        List(50) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -0.5f,
                speed = Random.nextFloat() * 0.005f + 0.003f,
                size = Random.nextFloat() * 8f + 4f,
                color = Color(
                    red = Random.nextFloat() * 0.8f + 0.2f,
                    green = Random.nextFloat() * 0.8f + 0.2f,
                    blue = Random.nextFloat() * 0.8f + 0.2f,
                    alpha = 1f
                ),
                angle = Random.nextFloat() * 360f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiProgress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val currentY = particle.y + progress * particle.speed * 100f
            val drawY = (currentY % 1.1f) * size.height
            val drawX = particle.x * size.width

            rotate(
                degrees = particle.angle + progress * 360f,
                pivot = Offset(drawX, drawY)
            ) {
                drawRect(
                    color = particle.color,
                    topLeft = Offset(drawX - particle.size / 2, drawY - particle.size / 2),
                    size = androidx.compose.ui.geometry.Size(particle.size, particle.size)
                )
            }
        }
    }
}

private data class Particle(
    val x: Float,
    val y: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val angle: Float
)