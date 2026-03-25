package com.unizar.sanbotbasicproject

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun ExercisePreparationScreen(
    posture: String, // "SITTING" or "STANDING"
    bodyPart: String,
    onCountdownFinished: () -> Unit
) {
    var timeLeft by remember { mutableIntStateOf(10) }

    // Nombre del ejercicio basado en la parte del cuerpo
    val exerciseName = when (bodyPart) {
        "ARMS_BACK" -> "Estiramiento de brazos"
        "LEGS_FEET" -> "Movilidad de piernas"
        "FULL_BODY" -> "Calentamiento general"
        else -> "Ejercicio suave"
    }

    // Mensaje basado en la postura
    val instructionMessage = if (posture == "SITTING") {
        "Busca una silla y ponte cómodo.\nVamos a empezar"
    } else {
        "Ponte de pie y busca un lugar despejado.\nVamos a empezar"
    }

    LaunchedEffect(key1 = timeLeft) {
        if (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        } else {
            onCountdownFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Título
            Text(
                text = instructionMessage,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Contador circular
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .border(4.dp, Color(0xFF333333), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = timeLeft.toString(),
                    color = Color.White,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            // Siguiente ejercicio info
            Text(
                text = "Siguiente ejercicio:",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = exerciseName,
                color = Color(0xFF56CCF2), // Azul claro como en la imagen
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Botón Saltar
            Button(
                onClick = onCountdownFinished,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(56.dp).width(300.dp)
            ) {
                Text(
                    text = "Saltar espera (Empezar ya)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
