package com.unizar.sanbotbasicproject.robotControl

import com.sanbot.opensdk.function.beans.LED
import com.sanbot.opensdk.function.unit.HardWareManager

class HardwareControl(hardWareManager: HardWareManager) {
    private val hardwareManager: HardWareManager = hardWareManager

    // Function to turn on the LED in the part and mode to be passed as a parameter
    fun turnOnLED(part: Byte, mode: Byte): Boolean {
        hardwareManager.setLED(LED(part, mode))
        return true
    }

    // Function to turn off the LED in the part and mode to be passed as parameter
    fun turnOffLED(part: Byte): Boolean {
        hardwareManager.setLED(LED(part, LED.MODE_CLOSE))
        return true
    }
}