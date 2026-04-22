package com.unizar.sanbotbasicproject

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chair
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
fun PostureScreen(
    onOptionSelected: (String) -> Unit, 
    speechControl: SpeechControl,
    systemControl: SystemControl,
    hardwareControl: HardwareControl
) {
    var isListening by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val isLandscape = screenWidth > screenHeight
        val baseUnit = if (isLandscape) screenHeight else screenWidth

        // Gestion de speechcontrol al entrar y salir de la pantalla
        DisposableEffect(Unit) {
            speechControl.onListeningStateChanged = { hardwareState ->
                isListening = hardwareState
            }
            startPostureVoiceFlow(
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
                .padding(baseUnit * 0.05f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Título adaptable
            Text(
                text = "¿Cómo prefieres hacer ejercicio hoy?",
                color = Color.White,
                fontSize = (baseUnit.value * 0.07f).sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = (baseUnit.value * 0.09f).sp,
                modifier = Modifier.padding(top = screenHeight * 0.02f)
            )

            // Contenedor de tarjetas adaptable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = screenHeight * 0.03f),
                horizontalArrangement = Arrangement.spacedBy(screenWidth * 0.04f, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PostureOptionCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(0.9f),
                    title = "Sentado en silla",
                    subtitle = "(Más seguro)",
                    icon = Icons.Default.Chair,
                    backgroundColor = Color(0xFFF7941D),
                    baseUnit = baseUnit,
                    onClick = {
                        speechControl.stopListening()
                        onOptionSelected("SITTING")
                    }
                )

                PostureOptionCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(0.9f),
                    title = "De pie",
                    subtitle = "(Para equilibrio)",
                    icon = Icons.Default.Person,
                    backgroundColor = Color(0xFF56CCF2),
                    baseUnit = baseUnit,
                    onClick = {
                        speechControl.stopListening()
                        onOptionSelected("STANDING")
                    }
                )
            }

            // Usamos el componente VoiceHud
            VoiceHud(
                isListening = isListening,
                helpText = "O dime: \"Ejercicio sentado\" o \"Ejercicio de pie\"",
                modifier = Modifier.padding(bottom = screenHeight * 0.02f)
            )
        }
    }
}

@Composable
fun PostureOptionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundColor: Color,
    baseUnit: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = backgroundColor,
        shape = RoundedCornerShape(baseUnit * 0.03f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(baseUnit * 0.02f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size((baseUnit.value * 0.2f).dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.height(baseUnit * 0.02f))
            Text(
                text = title,
                color = Color.Black,
                fontSize = (baseUnit.value * 0.05f).sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = (baseUnit.value * 0.06f).sp
            )
            Text(
                text = subtitle,
                color = Color.Black,
                fontSize = (baseUnit.value * 0.035f).sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun startPostureVoiceFlow(
    speechControl: SpeechControl,
    onOptionSelected: (String) -> Unit,
    systemControl: SystemControl,
    hardwareControl: HardwareControl
) {
    // El robot pone cara de duda y orejas azules
    systemControl.setEmotion(EmotionsType.QUESTION)
    hardwareControl.setEarsLED(LED.MODE_BLUE)

    // Habla y abre el micro automáticamente al terminar
    speechControl.ask("¿Cómo prefieres hacer ejercicio hoy? ¿sentado o de pie?") { text ->
        val texto = text.lowercase()
        when {
            "sentado" in texto || "silla" in texto -> {
                speechControl.stopListening()
                onOptionSelected("SITTING")
            }
            "pie" in texto || "levantado" in texto || "parado" in texto -> {
                speechControl.stopListening()
                onOptionSelected("STANDING")
            }
        }
    }
}

fun stopPostureVoiceFlow(
    speechControl: SpeechControl
) {
    // Método unificado que apaga voz y micrófono
    speechControl.stopListening()
}
