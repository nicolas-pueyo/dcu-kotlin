package com.unizar.sanbotbasicproject

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sanbot.opensdk.base.TopBaseActivity
import com.sanbot.opensdk.beans.FuncConstant
import com.sanbot.opensdk.function.beans.EmotionsType
import com.sanbot.opensdk.function.beans.LED
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
        // Register the activity
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
                //Parece que aqui se añaden las rutas de navegacion
                NavHost(navController = navController, startDestination = "main") {
                    composable("main") {
                        ControlPanel(
                            onNavigateToStartSession = { navController.navigate("start_session") },
                            onNavigateToPostureScreen = { navController.navigate("posture_screen") }
                        )
                    }
                    composable("start_session") {
                        StartSession()
                    }
                    composable("posture_screen") {
                        PostureScreen(onOptionSelected = { /* No hace nada por ahora */ })
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
    }
    @Composable
    fun ControlPanel(onNavigateToStartSession: () -> Unit, onNavigateToPostureScreen: () -> Unit) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Basic Functions", modifier = Modifier.padding(bottom = 20.dp))
            // Head Motion
            Row {
                // Left button
                Button(
                    onClick = { moveHead("LEFT") },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Left")
                }

                // Right button
                Button(
                    onClick = { moveHead("RIGHT") },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Right")
                }

                // Center button
                Button(
                    onClick = { moveHead("CENTER") },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Center")
                }

                // Up button
                Button(
                    onClick = { moveHead("UP") },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Up")
                }

                // Down button
                Button(
                    onClick = { moveHead("DOWN") },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Down")
                }
            }
            // Emotions and Speech
            Row {
                Button(
                    onClick = {systemControl.setEmotion(EmotionsType.FAINT)},
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Change emotion")
                }
                Button(
                    onClick = {speechControl.talk("Hola, soy Sanbot, ¿cómo estás?",50)},
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Greetings")
                }
            }
            // Wheel Motion
            Row {
                Button(
                    onClick = {wheelControl.controlBasicWheels(WheelControl.WheelActions.LEFT)},
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Turn Left")
                }
                Button(
                    onClick = {wheelControl.controlBasicWheels(WheelControl.WheelActions.SPIN)},
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Spin")
                }
                Button(
                    onClick = {wheelControl.controlBasicWheels(WheelControl.WheelActions.RIGHT)},
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Turn Right")
                }
            }
            // LEDs Functions
            Row {
                Button(
                    onClick = {hardwareControl.turnOnLED(LED.PART_ALL, LED.MODE_BLUE)},
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Turn On LEDs")
                }
                Button(
                    onClick = {hardwareControl.turnOffLED(LED.PART_ALL)},
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Turn Off LEDs")
                }
            }
            Row {
                Button(
                    onClick = {handsControl.controlBasicArms(HandsControl.ActionsArms.RAISE_ARM,
                        HandsControl.TypeArm.RIGHT)},
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Raise Arms")
                }
                Button(
                    onClick = {handsControl.controlBasicArms(HandsControl.ActionsArms.LOWER_ARM,
                        HandsControl.TypeArm.RIGHT)},
                    modifier = Modifier.padding(8.dp)
                )
                {
                    Text("Lower Arms")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Navigation Buttons
            Row {
                Button(
                    onClick = onNavigateToStartSession,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Ir a Comenzar Sesión")
                }

                Button(
                    onClick = onNavigateToPostureScreen,
                    modifier = Modifier.padding(8.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = colorScheme.secondary
                    )
                ) {
                    Text("Ir a Posture Screen")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Exit button
            Button(
                onClick = { finish() },
                modifier = Modifier.padding(8.dp),
                // Red color
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = colorScheme.error
                )
            ) {
                Text("Quit")
            }
        }
    }

    private fun moveHead(direction: String) {
            when (direction) {
                "LEFT" -> headControl.controlHeadBasic(HeadControl.HeadActions.LEFT)
                "RIGHT" -> headControl.controlHeadBasic(HeadControl.HeadActions.RIGHT)
                "CENTER" -> headControl.controlHeadBasic(headActions = HeadControl.HeadActions.CENTER)
                "UP" -> headControl.controlHeadBasic(headActions = HeadControl.HeadActions.UP)
                "DOWN" -> headControl.controlHeadBasic(headActions = HeadControl.HeadActions.DOWN)
            }

    }

}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

// Method that contains the buttons interaction
fun setonClicks()
{

}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SanbotBasicProjectTheme {
        Greeting("Android")
    }
}
