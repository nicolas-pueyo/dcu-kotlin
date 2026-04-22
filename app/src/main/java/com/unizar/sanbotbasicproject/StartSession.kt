package com.unizar.sanbotbasicproject
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import android.util.Log

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

        speechControl.onListeningStateChanged = { hardwareState ->
            isListening = hardwareState
        }

        startStartSessionVoiceFlow(
            speechControl = speechControl,
            onStartClick = onStartClick,
            systemControl = systemControl,
            hardwareControl = hardwareControl
        )

        onDispose {
            speechControl.onListeningStateChanged = null
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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    speechControl.stopListening()
                    onStartClick()
                },
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
    systemControl: SystemControl,
    hardwareControl: HardwareControl
) {
    // REACCIÓN: Sonrisa y orejas azules (fijo)
    systemControl.setEmotion(EmotionsType.SMILE)
    hardwareControl.setEarsLED(LED.MODE_BLUE)

    // FLUJO NUEVO: Usamos el método `ask` de nuestro SpeechControl.
    // El robot hablará y, automáticamente, abrirá el micro al terminar.
    speechControl.ask("Hola, pulsa el botón, tócame la cabeza o dime empezar ejercicio para comenzar") { text ->
        val textoLimpio = text.lowercase()
        Log.d("Speech Control", "Texto limpio: $textoLimpio")
        // Si detecta la palabra clave, avanza a la siguiente pantalla
        if ("empezar" in textoLimpio || "ejercicio" in textoLimpio || "comenzar" in textoLimpio) {
            speechControl.stopListening() // Paramos el micro por seguridad
            onStartClick()
        }
//        else {
//            // (Opcional) Si dice otra cosa, podemos hacer que el robot avise de que no lo ha entendido
//            speechControl.talk("No te he entendido bien. Por favor, dime empezar ejercicio.")
//        }
    }
}

fun stopStartSessionVoiceFlow(
    speechControl: SpeechControl
) {
    speechControl.stopListening() // Usamos nuestro método limpio de parada
}
