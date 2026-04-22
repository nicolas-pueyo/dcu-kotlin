package com.unizar.sanbotbasicproject

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Person
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
import com.unizar.sanbotbasicproject.robotControl.SpeechControl
import com.unizar.sanbotbasicproject.robotControl.SystemControl
import com.unizar.sanbotbasicproject.ui.VoiceHud

@Composable
fun BodyPartSelectionScreen(
    onBack: () -> Unit, 
    onOptionSelected: (String) -> Unit,
    speechControl: SpeechControl,
    systemControl: SystemControl,
    hardwareControl: HardwareControl
) {
    var isListening by remember { mutableStateOf(false) }

    // Reacción física al entrar en la pantalla
    DisposableEffect(Unit) {
        speechControl.onListeningStateChanged = { hardwareState ->
            isListening = hardwareState
        }
        startBodyPartVoiceFlow(
            speechControl = speechControl,
            onOptionSelected = onOptionSelected,
            systemControl = systemControl,
            hardwareControl = hardwareControl
        )
        onDispose {
            speechControl.onListeningStateChanged = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // Fondo oscuro
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Botón Atrás
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Atrás", color = Color.White, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Título central
        Text(
            text = "¿Qué parte del cuerpo\nquieres mover hoy?",
            color = Color.White,
            fontSize = 44.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            lineHeight = 52.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Fila de Tarjetas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BodyPartCard(
                modifier = Modifier.weight(1f),
                title = "Brazos y Espalda",
                icon = Icons.Default.ArrowUpward,
                backgroundColor = Color(0xFFA8E6E2),
                onClick = {
                    speechControl.stopListening()
                    onOptionSelected("ARMS_BACK")
                }
            )
            BodyPartCard(
                modifier = Modifier.weight(1f),
                title = "Piernas y Pies",
                icon = Icons.Default.DirectionsWalk,
                backgroundColor = Color(0xFFF5C2E0),
                onClick = {
                    speechControl.stopListening()
                    onOptionSelected("LEGS_FEET")
                }
            )
            BodyPartCard(
                modifier = Modifier.weight(1f),
                title = "Cuerpo (Entero)",
                icon = Icons.Default.Person,
                backgroundColor = Color(0xFFD1B3FF),
                onClick = {
                    speechControl.stopListening()
                    onOptionSelected("FULL_BODY")
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // HUD de Voz Reutilizable
        VoiceHud(
            isListening = isListening,
            helpText = "Dime: \"Brazos\", \"Piernas\" o \"Cuerpo entero\""
        )
    }
}

@Composable
fun BodyPartCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxHeight(0.85f)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = title,
                color = Color.Black,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

fun startBodyPartVoiceFlow(
    speechControl: SpeechControl,
    onOptionSelected: (String) -> Unit,
    systemControl: SystemControl,
    hardwareControl: HardwareControl
) {

    // Reacción física del robot
    systemControl.setEmotion(EmotionsType.QUESTION)
    hardwareControl.setEarsLED(LED.MODE_BLUE)

    // El robot pregunta y abre el micro solo cuando termina de hablar
    speechControl.ask("¿Qué parte del cuerpo quieres mover hoy? Brazos, piernas o el cuerpo entero") { text ->
        val texto = text.lowercase()
        when {
            "brazo" in texto || "espalda" in texto || "arriba" in texto -> {
                speechControl.stopListening()
                onOptionSelected("ARMS_BACK")
            }

            "pierna" in texto || "pie" in texto || "abajo" in texto -> {
                speechControl.stopListening()
                onOptionSelected("LEGS_FEET")
            }

            "cuerpo" in texto || "entero" in texto || "todo" in texto -> {
                speechControl.stopListening()
                onOptionSelected("FULL_BODY")
            }
        }
    }
}
fun stopBodyPartVoiceFlow(
    speechControl: SpeechControl
) {
    // Usamos el método unificado que ya detiene voz y micro
    speechControl.stopListening()
}
