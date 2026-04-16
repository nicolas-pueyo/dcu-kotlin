package com.unizar.sanbotbasicproject

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri

@Composable
fun VideoScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F4F4))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Rutina de Ejercicio",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Usamos AndroidView para integrar el VideoView tradicional en Compose
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    // Configuración del video
                    // Nota: Asegúrate de tener un video en res/raw/tu_video
                    // O cambia esta URI por una URL de internet para probar
                    val packageName = ctx.packageName

                    setVideoURI(
                        "android.resource://$packageName/${R.raw.proyecto_dcu_final}".toUri()
                    )

                    // Controles de reproducción
                    val mediaController = MediaController(ctx)
                    mediaController.setAnchorView(this)
                    setMediaController(mediaController)

                    setOnPreparedListener { 
                        start() 
                    }
                }
            },
            modifier = Modifier
                .size(width = 800.dp, height = 450.dp) // Tamaño basado en tu XML
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Finalizar Ejercicio")
        }
    }
}
