package com.unizar.sanbotbasicproject.robotControl

import com.sanbot.opensdk.function.beans.headmotion.AbsoluteAngleHeadMotion
import com.sanbot.opensdk.function.beans.headmotion.RelativeAngleHeadMotion
import com.sanbot.opensdk.function.unit.HeadMotionManager

class HeadControl(headMotionManager: HeadMotionManager) {
    private val headMotionManager: HeadMotionManager = headMotionManager

    // Enum used to define the head actions, in this case: right, left, up, down and center.
    enum class HeadActions {
        RIGHT,
        LEFT,
        UP,
        DOWN,
        CENTER
    }

    // Function used to indicate the action you want to perform.
    // with the head
    fun controlHeadBasic(headActions: HeadActions): Boolean {
        var relativeAngleHeadMotion: RelativeAngleHeadMotion
        val absoluteAngleHeadMotion: AbsoluteAngleHeadMotion
        println("ACTION$headActions")
        when (headActions) {
            HeadActions.LEFT -> {
                absoluteAngleHeadMotion =
                    AbsoluteAngleHeadMotion(AbsoluteAngleHeadMotion.ACTION_HORIZONTAL, 0)
                headMotionManager.doAbsoluteAngleMotion(absoluteAngleHeadMotion)
            }

            HeadActions.RIGHT -> {
                absoluteAngleHeadMotion =
                    AbsoluteAngleHeadMotion(AbsoluteAngleHeadMotion.ACTION_HORIZONTAL, 180)
                headMotionManager.doAbsoluteAngleMotion(absoluteAngleHeadMotion)
            }

            HeadActions.UP -> {
                absoluteAngleHeadMotion =
                    AbsoluteAngleHeadMotion(AbsoluteAngleHeadMotion.ACTION_VERTICAL, 30)
                headMotionManager.doAbsoluteAngleMotion(absoluteAngleHeadMotion)
            }

            HeadActions.DOWN -> {
                absoluteAngleHeadMotion =
                    AbsoluteAngleHeadMotion(AbsoluteAngleHeadMotion.ACTION_VERTICAL, 7)
                headMotionManager.doAbsoluteAngleMotion(absoluteAngleHeadMotion)
            }

            HeadActions.CENTER -> {
                absoluteAngleHeadMotion =
                    AbsoluteAngleHeadMotion(AbsoluteAngleHeadMotion.ACTION_HORIZONTAL, 90)
                headMotionManager.doAbsoluteAngleMotion(absoluteAngleHeadMotion)
            }
        }

        try {
            Thread.sleep(3000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        return true
    }

    // Function to put the head in its original position, in this case: in the center.
    fun reset(): Boolean {
        controlHeadBasic(HeadActions.CENTER)

        return true
    }
}