package com.unizar.sanbotbasicproject
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sanbot.opensdk.function.beans.EmotionsType
import com.sanbot.opensdk.function.beans.LED
import com.unizar.sanbotbasicproject.robotControl.HardwareControl
import com.unizar.sanbotbasicproject.robotControl.SpeechControl
import com.unizar.sanbotbasicproject.robotControl.SystemControl
import com.unizar.sanbotbasicproject.ui.VoiceHud

@Composable
fun StartSession(
    onStartClick: () -> Unit,
    onVideoClick: () -> Unit,
    speechControl: SpeechControl,
    systemControl: SystemControl,
    hardwareControl: HardwareControl
) {
    var isListening by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        startStartSessionVoiceFlow(
            speechControl = speechControl,
            onStartClick = onStartClick,
            onListeningStateChange = { isListening = it },
            systemControl = systemControl,
            hardwareControl = hardwareControl
        )

        onDispose {
            stopStartSessionVoiceFlow(speechControl)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(24.dp)
    ) {
        // Indicador de estado superior
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(Color(0xFF1E1E1E), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(if (isListening) Color(0xFF4CAF50) else Color.Gray, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isListening) "Robot listo" else "Cargando...",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onStartClick,
                modifier = Modifier.size(width = 560.dp, height = 300.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0056D2)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MonitorHeart,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Comenzar Ejercicio",
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Botón de prueba de video
            OutlinedButton(
                onClick = onVideoClick,
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.VideoLibrary, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Probar Reproductor de Video")
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Usamos el componente VoiceHud
            VoiceHud(
                isListening = isListening,
                helpText = "O dime: \"Empezar ejercicio\""
            )
        }
    }
}

fun startStartSessionVoiceFlow(
    speechControl: SpeechControl,
    onStartClick: () -> Unit,
    onListeningStateChange: (Boolean) -> Unit,
    systemControl: SystemControl,
    hardwareControl: HardwareControl
) {
    speechControl.startListening(
        onRecognized = { text ->
            val texto = text.lowercase()
            if ("empezar" in texto || "ejercicio" in texto || "comenzar" in texto) {
                onStartClick()
                speechControl.stopListening()
            }
        },
        onStart = { onListeningStateChange(true) },
        onStop = { onListeningStateChange(false) }
    )

    speechControl.wakeUp()

    // REACCIÓN: Sonrisa y orejas azules (fijo)
    systemControl.setEmotion(EmotionsType.SMILE)
    hardwareControl.setEarsLED(LED.MODE_BLUE)

    speechControl.talk("Hola, pulsa el botón o dime empezar ejercicio para comenzar")
}

fun stopStartSessionVoiceFlow(
    speechControl: SpeechControl
) {
    speechControl.stopTalking()
    speechControl.sleep()
}
