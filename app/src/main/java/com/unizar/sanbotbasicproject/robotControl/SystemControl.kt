package com.unizar.sanbotbasicproject.robotControl

import com.sanbot.opensdk.function.beans.EmotionsType
import com.sanbot.opensdk.function.unit.SystemManager

/**
 * Clase para controlar el sistema del robot (emociones faciales, etc.)
 */
class SystemControl(private val systemManager: SystemManager) {
    private var currentEmotion: EmotionsType = EmotionsType.NORMAL

    /**
     * Cambia la expresión facial del robot (ojos LED).
     * Según sección 3.7.3 de la documentación.
     * @param emotion El tipo de emoción (SMILE, SURPRISE, NORMAL, etc.)
     */
    fun setEmotion(emotion: EmotionsType) {
        currentEmotion = emotion
        systemManager.showEmotion(currentEmotion)
    }
}
