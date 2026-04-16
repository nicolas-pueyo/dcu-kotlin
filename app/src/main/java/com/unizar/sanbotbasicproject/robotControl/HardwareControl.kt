package com.unizar.sanbotbasicproject.robotControl

import com.sanbot.opensdk.function.beans.LED
import com.sanbot.opensdk.function.unit.HardWareManager

/**
 * Clase para controlar el hardware físico del robot (LEDs, sensores, etc.)
 */
class HardwareControl(private val hardwareManager: HardWareManager) {

    /**
     * Ajusta el nivel de brillo de la luz LED blanca.
     * @param level Nivel de brillo (1: ahorro energía, 2: suave, 3: brillante)
     * Según sección 3.3.8 de la documentación.
     */
    fun setBrightness(level: Int) {
        hardwareManager.setWhiteLightLevel(level)
    }

    /**
     * Toma el control de las luces LED de colores.
     * Según sección 3.3.2 de la documentación.
     */
    fun setLED(part: Byte, mode: Byte, delayTime: Byte = 0, randomCount: Byte = 0) {
        hardwareManager.setLED(LED(part, mode, delayTime, randomCount))
    }

    /**
     * Configura específicamente las "orejas" (lados de la cabeza) al mismo tiempo.
     * Según sección 3.3.2 de la documentación.
     */
    fun setEarsLED(mode: Byte, delayTime: Byte = 0, randomCount: Byte = 0) {
        setLED(LED.PART_LEFT_HEAD, mode, delayTime, randomCount)
        setLED(LED.PART_RIGHT_HEAD, mode, delayTime, randomCount)
    }

    /**
     * Método de conveniencia para apagar un LED específico.
     */
    fun turnOffLED(part: Byte) {
        setLED(part, LED.MODE_CLOSE)
    }
}
