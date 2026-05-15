package com.unizar.sanbotbasicproject

import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun SettingsButtonWithDialog(
    ledBrightness: Int,
    onLedBrightnessChange: (Int) -> Unit,
    projectorBrightness: Int,
    onProjectorBrightnessChange: (Int) -> Unit,
    volume: Int,
    onVolumeChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSettingsDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val audioManager = remember(context) {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    val maxVolume = remember(audioManager) {
        audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }

    Box(
        modifier = modifier
            .size(72.dp)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.1f))
            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
            .clickable { showSettingsDialog = true },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Ajustes",
            modifier = Modifier.size(36.dp),
            tint = Color.White
        )
    }

    if (showSettingsDialog) {
        var localLed by remember(ledBrightness) {
            mutableIntStateOf(ledBrightness.coerceIn(1, 3))
        }
        var localProjector by remember(projectorBrightness) {
            mutableIntStateOf(projectorBrightness.coerceIn(-31, 31))
        }
        var localVolume by remember(volume, maxVolume) {
            mutableIntStateOf(volume.coerceIn(0, maxVolume))
        }

        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            shape = MaterialTheme.shapes.large,
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            icon = {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Ajustes") },
            text = {
                androidx.compose.foundation.layout.Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFFFFA726)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Brillo LED",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            "$localLed",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = localLed.toFloat(),
                        onValueChange = { localLed = it.roundToInt().coerceIn(1, 3) },
                        valueRange = 1f..3f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                    Text(
                        "Modo bajo / medio / alto",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Videocam,
                            contentDescription = null,
                            tint = Color(0xFF4ADE80)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Brillo proyector",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            "$localProjector",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = localProjector.toFloat(),
                        onValueChange = { localProjector = it.roundToInt().coerceIn(-31, 31) },
                        valueRange = -31f..31f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.secondary,
                            activeTrackColor = MaterialTheme.colorScheme.secondary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                    Text(
                        "De más oscuro (-31) a más brillante (31)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Volumen",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            "$localVolume / $maxVolume",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = localVolume.toFloat(),
                        onValueChange = { localVolume = it.roundToInt().coerceIn(0, maxVolume) },
                        valueRange = 0f..maxVolume.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF38BDF8),
                            activeTrackColor = Color(0xFF38BDF8),
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onLedBrightnessChange(localLed)
                        onProjectorBrightnessChange(localProjector)
                        onVolumeChange(localVolume)
                        showSettingsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.shadow(6.dp, RoundedCornerShape(20.dp))
                ) {
                    Box(
                        Modifier
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF2563EB), Color(0xFF1E40AF))
                                ),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 24.dp, vertical = 10.dp)
                    ) {
                        Text("Aceptar", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text(
                        "Cancelar",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        )
    }
}