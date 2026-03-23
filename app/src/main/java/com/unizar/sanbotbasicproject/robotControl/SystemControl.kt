package com.unizar.sanbotbasicproject.robotControl

import com.sanbot.opensdk.function.beans.EmotionsType
import com.sanbot.opensdk.function.unit.SystemManager

class SystemControl(systemManager: SystemManager) {
    private val systemManager: SystemManager = systemManager
    private var currentEmotion: EmotionsType = EmotionsType.NORMAL

    // Function used to change the facial expression of the robot
    // by any of the emotions defined in the system.
    fun setEmotion(emotion: EmotionsType) {
        currentEmotion = emotion
        systemManager.showEmotion(currentEmotion)
    }
}