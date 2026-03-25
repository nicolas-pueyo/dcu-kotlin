package com.unizar.sanbotbasicproject

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.unizar.sanbotbasicproject.robotControl.SpeechControl

@Composable
fun StartSession(onStartClick: () -> Unit, speechControl: SpeechControl) {
    // Gestion de speechcontrol al entrar y salir de la pantalla
    DisposableEffect(Unit) {
        startStartSessionVoiceFlow(speechControl, onStartClick)

        onDispose {
            stopStartSessionVoiceFlow(speechControl)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // Fondo casi negro como en la imagen
            .padding(24.dp)
    ) {

        // Contenedor central
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // BOTÓN PRINCIPAL AZUL
            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .size(width = 560.dp, height = 300.dp), // Más grande y horizontal
                shape = RoundedCornerShape(32.dp), // Bordes redondeados
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0056D2) // Azul vibrante de la imagen
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Icono de pulso/corazón
                    Icon(
                        imageVector = Icons.Default.MonitorHeart,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Comenzar Ejercicio",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Pista de voz (Fondo oscuro con icono de micro)
            Surface(
                color = Color(0xFF1E1E1E),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.width(500.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "O dime: \"Empezar ejercicio\"",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

fun startStartSessionVoiceFlow(
    speechControl: SpeechControl,
    onStartClick: () -> Unit
) {
    speechControl.startListening({ text ->
        val texto = text.lowercase()

        if ("empezar" in texto || "ejercicio" in texto || "comenzar" in texto) {
            onStartClick()
            speechControl.stopListening()
        }
    })

    speechControl.wakeUp()
    speechControl.talk("Hola, pulsa el botón o dime empezar ejercicio para comenzar")
}

fun stopStartSessionVoiceFlow(
    speechControl: SpeechControl
) {
    speechControl.stopTalking()
    speechControl.sleep()
}
