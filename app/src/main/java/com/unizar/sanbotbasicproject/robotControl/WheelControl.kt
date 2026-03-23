package com.unizar.sanbotbasicproject.robotControl

import com.sanbot.opensdk.function.beans.wheelmotion.RelativeAngleWheelMotion
import com.sanbot.opensdk.function.unit.WheelMotionManager

class WheelControl(private val wheelMotionManager: WheelMotionManager) {

    enum class WheelActions {
        LEFT,
        RIGHT,
        SPIN
    }


    fun controlBasicWheels(action: WheelActions?): Boolean {
        val wheelsMotion: RelativeAngleWheelMotion
        when (action) {
            WheelActions.LEFT -> {
                wheelsMotion =
                    RelativeAngleWheelMotion(RelativeAngleWheelMotion.TURN_LEFT, 5, 90)
                wheelMotionManager.doRelativeAngleMotion(wheelsMotion)
            }

            WheelActions.RIGHT -> {
                wheelsMotion =
                    RelativeAngleWheelMotion(RelativeAngleWheelMotion.TURN_RIGHT, 5, 90)
                wheelMotionManager.doRelativeAngleMotion(wheelsMotion)
            }

            WheelActions.SPIN -> {
                wheelsMotion =
                    RelativeAngleWheelMotion(RelativeAngleWheelMotion.TURN_LEFT, 5, 360)
                wheelMotionManager.doRelativeAngleMotion(wheelsMotion)
            }
            null -> {
                // If it is null, only return false
                return false
            }
        }

        try {
            Thread.sleep(3000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        return true
    }
}