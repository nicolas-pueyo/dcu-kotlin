package com.unizar.sanbotbasicproject

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sanbot.opensdk.base.TopBaseActivity
import com.sanbot.opensdk.beans.FuncConstant
import com.sanbot.opensdk.function.unit.HeadMotionManager
import com.sanbot.opensdk.function.unit.SystemManager
import com.sanbot.opensdk.function.unit.SpeechManager
import com.sanbot.opensdk.function.unit.WheelMotionManager
import com.sanbot.opensdk.function.unit.HardWareManager
import com.sanbot.opensdk.function.unit.WingMotionManager
import com.unizar.sanbotbasicproject.ui.theme.SanbotBasicProjectTheme
import com.unizar.sanbotbasicproject.robotControl.HeadControl
import com.unizar.sanbotbasicproject.robotControl.SystemControl
import com.unizar.sanbotbasicproject.robotControl.SpeechControl
import com.unizar.sanbotbasicproject.robotControl.WheelControl
import com.unizar.sanbotbasicproject.robotControl.HardwareControl
import com.unizar.sanbotbasicproject.robotControl.HandsControl
import com.sanbot.opensdk.function.unit.ProjectorManager
import com.unizar.sanbotbasicproject.robotControl.ProjectorControl
import com.sanbot.opensdk.function.unit.interfaces.hardware.TouchSensorListener

class MainActivity : TopBaseActivity() {
    private var isRobotReady by mutableStateOf(false)
    private var initErrorMessage by mutableStateOf<String?>(null)
    private var onTouchAction: ((Int) -> Unit)? = null
    lateinit var headMotionManager : HeadMotionManager
    lateinit var headControl : HeadControl
    lateinit var systemControl: SystemControl
    lateinit var systemManager: SystemManager
    lateinit var speechManager: SpeechManager
    lateinit var speechControl: SpeechControl
    lateinit var wheelManager: WheelMotionManager
    lateinit var wheelControl: WheelControl
    lateinit var hardwareManager: HardWareManager
    lateinit var hardwareControl: HardwareControl
    lateinit var handMotionManager: WingMotionManager
    lateinit var handsControl: HandsControl
    lateinit var projectorManager: ProjectorManager
    lateinit var projectorControl: ProjectorControl

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MainActivity", "onCreate: Registrando actividad")
        register(MainActivity::class.java)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        super.onCreate(savedInstanceState)
        
        setContent {
            SanbotBasicProjectTheme {
                if (isRobotReady) {
                    val navController = rememberNavController()
                    
                    var currentRoutine by remember { mutableStateOf<List<Exercise>>(emptyList()) }
                    var currentExerciseIndex by remember { mutableIntStateOf(0) }
                    var totalTimeSeconds by remember { mutableIntStateOf(0) }

                    NavHost(navController = navController, startDestination = "start_session") {
                        
                        composable("start_session") {
                            totalTimeSeconds = 0
                            DisposableEffect(Unit) {
                                onTouchAction = { part ->
                                    if (part == 11) {
                                        Log.d("SanbotTouch", "Navegando desde start_session por toque en cabeza")
                                        navController.navigate("posture_screen")
                                    }
                                }
                                onDispose { onTouchAction = null }
                            }

                            StartSession(
                                onStartClick = { navController.navigate("posture_screen") },
                                onVideoClick = { navController.navigate("video_test") },
                                speechControl = speechControl,
                                systemControl = systemControl,
                                hardwareControl = hardwareControl
                            )
                        }

                        composable("posture_screen") {
                            PostureScreen(
                                onOptionSelected = { posture -> 
                                    Log.d("Selection", "Selected posture: $posture")
                                    navController.navigate("body_selection/$posture")
                                },
                                speechControl = speechControl,
                                systemControl = systemControl,
                                hardwareControl = hardwareControl
                            )

                            DisposableEffect(Unit) {
                                onTouchAction = { part ->
                                    if (part == 9) {
                                        Log.d("SanbotTouch", "Navegando desde posture_screen por toque en brazo izquierdo")
                                        navController.navigate("body_selection/SITTING")
                                    } else if (part == 10) {
                                        Log.d("SanbotTouch", "Navegando desde posture_screen por toque en brazo derecho")
                                        navController.navigate("body_selection/STANDING")
                                    }
                                }
                                onDispose { onTouchAction = null }
                            }
                        }

                        composable(
                            "body_selection/{posture}",
                            arguments = listOf(navArgument("posture") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val posture = backStackEntry.arguments?.getString("posture") ?: "SITTING"
                            BodyPartSelectionScreen(
                                onBack = { navController.popBackStack() },
                                onOptionSelected = { part -> 
                                    currentRoutine = RoutineProvider.getRoutine(posture, part)
                                    currentExerciseIndex = 0
                                    navController.navigate("exercise_preparation/$posture/$part")
                                },
                                speechControl = speechControl,
                                systemControl = systemControl,
                                hardwareControl = hardwareControl
                            )
                        }

                        composable(
                            "exercise_preparation/{posture}/{bodyPart}",
                            arguments = listOf(
                                navArgument("posture") { type = NavType.StringType },
                                navArgument("bodyPart") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val exercise = currentRoutine.getOrNull(currentExerciseIndex)
                            if (exercise != null) {
                                ExercisePreparationScreen(
                                    posture = backStackEntry.arguments?.getString("posture") ?: "",
                                    bodyPart = exercise.name, 
                                    onCountdownFinished = {
                                        navController.navigate("exercise_execution")
                                    },
                                    systemControl = systemControl,
                                    hardwareControl = hardwareControl,
                                    projectorControl = projectorControl
                                )
                            }
                        }

                        composable("exercise_execution") {
                            val exercise = currentRoutine.getOrNull(currentExerciseIndex)
                            var headTouchTrigger by remember { mutableIntStateOf(0) }

                            DisposableEffect(Unit) {
                                onTouchAction = { part ->
                                    if (part == 11) {
                                        Log.d("SanbotTouch", "Cabeza tocada: pausa/reanudar")
                                        headTouchTrigger++
                                    }
                                }
                                onDispose { onTouchAction = null }
                            }

                            if (exercise != null) {
                                ExerciseExecutionScreen(
                                    exercise = exercise,
                                    onExerciseFinished = { spent ->
                                        totalTimeSeconds += spent
                                        if (currentExerciseIndex < currentRoutine.size - 1) {
                                            navController.navigate("rest_screen")
                                        } else {
                                            navController.navigate("routine_finished/true")
                                        }
                                    },
                                    onFinishRoutine = { spent ->
                                        totalTimeSeconds += spent
                                        navController.navigate("routine_finished/false")
                                    },
                                    systemControl = systemControl,
                                    hardwareControl = hardwareControl,
                                    externalPauseTrigger = headTouchTrigger
                                )
                            }
                        }

                        composable("rest_screen") {
                            RestScreen(
                                onContinue = {
                                    currentExerciseIndex++
                                    navController.navigate("exercise_execution")
                                },
                                onFinishEarly = {
                                    navController.navigate("routine_finished/false")
                                },
                                systemControl = systemControl,
                                hardwareControl = hardwareControl
                            )
                        }

                        composable(
                            "routine_finished/{completed}",
                            arguments = listOf(navArgument("completed") { type = NavType.BoolType })
                        ) { backStackEntry ->
                            val completed = backStackEntry.arguments?.getBoolean("completed") ?: false
                            RoutineFinishedScreen(
                                totalMinutes = totalTimeSeconds / 60,
                                completed = completed,
                                onBackToStart = {
                                    navController.navigate("start_session") {
                                        popUpTo("start_session") { inclusive = true }
                                    }
                                },
                                systemControl = systemControl,
                                hardwareControl = hardwareControl,
                                projectorControl = projectorControl
                            )

                            DisposableEffect(Unit) {
                                onTouchAction = { part ->
                                    if (part == 11) {
                                        Log.d("SanbotTouch", "Navegando desde routine_finished por toque en cabeza")
                                        navController.navigate("start_session") {
                                            popUpTo("start_session") { inclusive = true }
                                        }
                                    }
                                }
                                onDispose { onTouchAction = null }
                            }
                        }

                        composable("video_test") {
                            VideoScreen(onFinish = { navController.popBackStack() })
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "Conectando con el robot...")
                            initErrorMessage?.let {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = it, color = androidx.compose.ui.graphics.Color.Red)
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            // Botón para pasar a la pantalla directamente sin esperar conexión
                            Button(onClick = { 
                                Log.w("MainActivity", "Inicio FORZADO por el usuario")
                                forceInit() 
                            }) {
                                Text(text = "Entrar a la aplicación")
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onMainServiceConnected() {
        Log.d("MainActivity", "onMainServiceConnected: RECIBIDO")
        try {
            headMotionManager = getUnitManager(FuncConstant.HEADMOTION_MANAGER) as HeadMotionManager
            headControl = HeadControl(headMotionManager)
            systemManager = getUnitManager(FuncConstant.SYSTEM_MANAGER) as SystemManager
            systemControl = SystemControl(systemManager)
            speechManager = getUnitManager(FuncConstant.SPEECH_MANAGER) as SpeechManager
            speechControl = SpeechControl(speechManager)
            wheelManager = getUnitManager(FuncConstant.WHEELMOTION_MANAGER) as WheelMotionManager
            wheelControl = WheelControl(wheelManager)
            hardwareManager = getUnitManager(FuncConstant.HARDWARE_MANAGER) as HardWareManager
            hardwareControl = HardwareControl(hardwareManager)
            projectorManager = getUnitManager(FuncConstant.PROJECTOR_MANAGER) as ProjectorManager
            projectorControl = ProjectorControl(projectorManager)

            hardwareManager.setOnHareWareListener(object : TouchSensorListener {
                override fun onTouch(part: Int) {
                    runOnUiThread { 
                        Log.d("RobotTouch", "Parte tocada: $part")
                        onTouchAction?.invoke(part) 
                    }
                }
                override fun onTouch(part: Int, isTouch: Boolean) {
                    if (isTouch) { 
                        runOnUiThread { 
                            Log.d("RobotTouch", "Parte tocada: $part")
                            onTouchAction?.invoke(part) 
                        } 
                    }
                }
            })

            handMotionManager = getUnitManager(FuncConstant.WINGMOTION_MANAGER) as WingMotionManager
            handsControl = HandsControl(handMotionManager)
            
            Log.d("MainActivity", "onMainServiceConnected: Inicialización completa")
            isRobotReady = true
        } catch (e: Exception) {
            Log.e("MainActivity", "Error inicializando los controles del robot", e)
            initErrorMessage = e.message
        }
    }

    private fun forceInit() {
        try {
            Log.w("MainActivity", "Ejecutando forceInit para evitar crashes de lateinit")
            // Inicializamos con nulls para que la app no pete al usar las variables lateinit
            speechControl = SpeechControl(null)
            projectorControl = ProjectorControl(getUnitManager(FuncConstant.PROJECTOR_MANAGER) as ProjectorManager)
            try {
                // Intentamos capturar los managers por si alguno sí está disponible
                if (!::headMotionManager.isInitialized) {
                    headMotionManager = getUnitManager(FuncConstant.HEADMOTION_MANAGER) as HeadMotionManager
                    headControl = HeadControl(headMotionManager)
                }
                if (!::systemManager.isInitialized) {
                    systemManager = getUnitManager(FuncConstant.SYSTEM_MANAGER) as SystemManager
                    systemControl = SystemControl(systemManager)
                }
                if (!::hardwareManager.isInitialized) {
                    hardwareManager = getUnitManager(FuncConstant.HARDWARE_MANAGER) as HardWareManager
                    hardwareControl = HardwareControl(hardwareManager)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "No se pudieron inicializar todos los Managers en forceInit", e)
            }
            
            isRobotReady = true
        } catch (e: Exception) {
            Log.e("MainActivity", "Error crítico en forceInit", e)
            initErrorMessage = "Error al forzar: ${e.message}"
        }
    }
}
