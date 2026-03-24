package com.unizar.sanbotbasicproject.robotControl

import com.sanbot.opensdk.function.unit.ProjectorManager
import com.sanbot.opensdk.beans.OperationResult
import android.util.Log

/**
 * Clase para el control básico del proyector láser del Sanbot ELF.
 * Basado en las especificaciones del OpenSDK sección 3.9.
 */
class ProjectorControl(private val projectorManager: ProjectorManager) {

    // Enums para facilitar la configuración sin recordar IDs numéricos
    enum class ProjectorMode {
        WALL,    // Modo Pared (ID: 1)
        CEILING  // Modo Techo (ID: 2)
    }

    enum class MirrorMode {
        NONE,       // Sin rotación (ID: 0)
        HORIZONTAL, // Giro horizontal (ID: 1)
        VERTICAL,   // Giro vertical (ID: 2)
        BOTH        // Giro total (ID: 3)
    }

    /**
     * Activa o desactiva el proyector.
     * NOTA: Se recomienda esperar al menos 12 segundos entre cambios de estado.
     */
    fun switchProjector(isOpen: Boolean): Boolean {
        Log.d("ProjectorControl", "Cambiando estado del proyector a: $isOpen")
        val result: OperationResult? = projectorManager.switchProjector(isOpen)
        return (result?.errorCode ?: -1) > 0
    }

    /**
     * Configura el modo de proyección (Pared o Techo).
     * El robot ajusta automáticamente el ángulo de la cabeza.
     */
    fun setProjectionMode(mode: ProjectorMode) {
        val modeId = when (mode) {
            ProjectorMode.WALL -> 1 // ProjectorManager.MODE_WALL
            ProjectorMode.CEILING -> 2 // ProjectorManager.MODE_CEILING
        }
        Log.d("ProjectorControl", "Cambiando modo de proyección a: $mode")
        projectorManager.setMode(modeId)
    }

    /**
     * Ajusta el modo espejo de la imagen.
     */
    fun setMirrorMode(mode: MirrorMode) {
        val mirrorId = when (mode) {
            MirrorMode.NONE -> 0 // ProjectorManager.MIRROR_CLOSE
            MirrorMode.HORIZONTAL -> 1 // ProjectorManager.MIRROR_LR
            MirrorMode.VERTICAL -> 2 // ProjectorManager.MIRROR_UD
            MirrorMode.BOTH -> 3 // ProjectorManager.MIRROR_ALL
        }
        Log.d("Projector control", "Cambiando modo de espejo a: $mode")
        projectorManager.setMirror(mirrorId)
    }

    /**
     * Ajusta el brillo de la imagen.
     * @param value Rango de -31 a 31.
     */
    fun setBrightness(value: Int) {
        val clampedValue = value.coerceIn(-31, 31)
        projectorManager.setBright(clampedValue)
    }

    /**
     * Corrección trapezoidal (Keystone) horizontal y vertical.
     * @param horizontal Rango de -30 a 30.
     * @param vertical Rango de -20 a 30.
     */
    fun adjustTrapezoid(horizontal: Int, vertical: Int) {
        projectorManager.setTrapezoidH(horizontal.coerceIn(-30, 30))
        projectorManager.setTrapezoidV(vertical.coerceIn(-20, 30))
    }

    /**
     * Restablece los valores visuales por defecto (brillo, contraste, nitidez).
     */
    fun resetVisuals() {
        projectorManager.setBright(0)
        projectorManager.setContrast(0) // Rango -15 a 15
        projectorManager.setAcuity(3)   // Nitidez media (Rango 0-6)
    }

    /**
     * Pone el proyecto en el modo que esperamos por defecto
     */
    fun expectedSetup() {
        setProjectionMode(ProjectorMode.WALL)
        setMirrorMode(MirrorMode.NONE)
        resetVisuals()
    }
}
