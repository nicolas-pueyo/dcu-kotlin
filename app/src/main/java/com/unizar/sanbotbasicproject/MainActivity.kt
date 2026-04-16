package com.unizar.sanbotbasicproject

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
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

class MainActivity : TopBaseActivity() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        register(MainActivity::class.java)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        super.onCreate(savedInstanceState)
        onMainServiceConnected()
        
        setContent {
            SanbotBasicProjectTheme {
                val navController = rememberNavController()
                
                // Estado global de la sesión de ejercicio
                var currentRoutine by remember { mutableStateOf<List<Exercise>>(emptyList()) }
                var currentExerciseIndex by remember { mutableIntStateOf(0) }
                var totalTimeSeconds by remember { mutableIntStateOf(0) }

                NavHost(navController = navController, startDestination = "start_session") {
                    
                    composable("start_session") {
                        totalTimeSeconds = 0 // Reset
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
                                hardwareControl = hardwareControl
                            )
                        }
                    }

                    composable("exercise_execution") {
                        val exercise = currentRoutine.getOrNull(currentExerciseIndex)
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
                                hardwareControl = hardwareControl
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
                            hardwareControl = hardwareControl
                        )
                    }

                    // Nueva pantalla de video accesible directamente para pruebas
                    composable("video_test") {
                        VideoScreen(onFinish = { navController.popBackStack() })
                    }
                }
            }
        }
    }

    override fun onMainServiceConnected() {
        headMotionManager = getUnitManager(FuncConstant.HEADMOTION_MANAGER) as HeadMotionManager
        headControl = HeadControl(headMotionManager)
        systemManager = getUnitManager(FuncConstant.SYSTEM_MANAGER) as SystemManager
        systemControl = SystemControl(systemManager)
        speechManager = getUnitManager(FuncConstant.SPEECH_MANAGER) as SpeechManager
        if(speechManager!=null)
            speechControl = SpeechControl(speechManager)
        else
            Log.e("MainActivity", "SpeechManager is null so SpeechControl cannot be initialized")
        wheelManager = getUnitManager(FuncConstant.WHEELMOTION_MANAGER) as WheelMotionManager
        wheelControl = WheelControl(wheelManager)
        hardwareManager = getUnitManager(FuncConstant.HARDWARE_MANAGER) as HardWareManager
        hardwareControl = HardwareControl(hardwareManager)
        handMotionManager = getUnitManager(FuncConstant.WINGMOTION_MANAGER) as WingMotionManager
        handsControl = HandsControl(handMotionManager)
        
        // Ajuste de brillo (Nivel 2) según sección 3.3.8
        hardwareControl.setBrightness(2)
    }
}
