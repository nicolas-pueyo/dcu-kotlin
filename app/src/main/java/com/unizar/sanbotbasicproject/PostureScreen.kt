package com.unizar.sanbotbasicproject

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanbot.opensdk.function.beans.speech.Grammar
import com.sanbot.opensdk.function.unit.interfaces.speech.RecognizeListener
import com.unizar.sanbotbasicproject.robotControl.SpeechControl

@Composable
fun PostureScreen(onOptionSelected: (String) -> Unit, speechControl: SpeechControl) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val isLandscape = screenWidth > screenHeight

        // Calculamos escalas basadas en el tamaño de la pantalla (Responsive por si acaso)
        val baseUnit = if (isLandscape) screenHeight else screenWidth

        // Gestion de speechcontrol al entrar y salir de la pantalla
        DisposableEffect(Unit) {
            startPostureVoiceFlow(speechControl, onOptionSelected)

            onDispose {
                stopPostureVoiceFlow(speechControl)
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
                    onClick = { onOptionSelected("SITTING") }
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
                    onClick = { onOptionSelected("STANDING") }
                )
            }

            // Barra inferior AÑADIR FUNCIONALIDAD DE MICRO
            Surface(
                color = Color(0xFF1C1C1E),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(bottom = screenHeight * 0.02f)
                    .wrapContentSize()
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = baseUnit * 0.04f, 
                        vertical = baseUnit * 0.02f
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size((baseUnit.value * 0.05f).dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(baseUnit * 0.02f))
                    Text(
                        text = "O dime: \"Ejercicio sentado\" o \"Ejercicio de pie\"",
                        color = Color.White,
                        fontSize = (baseUnit.value * 0.035f).sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
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


//FUNCIONES SPEECH CONTROL

/*
*  Esta funcion se ocupa de gestionar la inicialización y la gestion del robot para escuchar y procesar la informacion
*  Además manda un mensaje por voz para avisar de que solicita
* */

fun startPostureVoiceFlow(
    speechControl: SpeechControl,
    onOptionSelected: (String) -> Unit
) {
    speechControl.startListening({ text ->
        val texto = text.lowercase()

        when {
            "sentado" in texto || "silla" in texto -> {
                onOptionSelected("SITTING")
                speechControl.stopListening()
            }
            "de pie" in texto || "pie" in texto || "levantado" in texto -> {
                onOptionSelected("STANDING")
                speechControl.stopListening()
            }
        }
    })

    speechControl.wakeUp()                                                   // Pone en modo despierto al robot
    speechControl.talk("¿Cómo prefieres hacer ejercicio hoy? sentado o de pie")    // Mensaje por voz para el user


}

fun stopPostureVoiceFlow(
    speechControl: SpeechControl
) {
    speechControl.stopTalking()
    speechControl.sleep()

}
