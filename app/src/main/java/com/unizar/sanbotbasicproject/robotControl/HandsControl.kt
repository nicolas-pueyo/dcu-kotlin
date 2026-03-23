package com.unizar.sanbotbasicproject.robotControl

import com.sanbot.opensdk.function.beans.handmotion.AbsoluteAngleHandMotion
import com.sanbot.opensdk.function.beans.wing.AbsoluteAngleWingMotion
import com.sanbot.opensdk.function.unit.WingMotionManager

class HandsControl(handMotionManager: WingMotionManager) {
    private val handMotionManager: WingMotionManager = handMotionManager

    // Enum used to define arm actions, in this case: lifting and lowering
    enum class ActionsArms {
        RAISE_ARM,
        LOWER_ARM,
    }

    // Enum used to define the types of arms that can be worked with,
    // in this case: right, left and both
    enum class TypeArm {
        RIGHT,
        LEFT,
        BOTH
    }

    // Function used to indicate the action you want to perform
    // and the arm or arms to be moved
    fun controlBasicArms(action: ActionsArms, arm: TypeArm): Boolean {
        val absolutePart = byteArrayOf(
            AbsoluteAngleWingMotion.PART_LEFT,
            AbsoluteAngleWingMotion.PART_RIGHT,
            AbsoluteAngleWingMotion.PART_BOTH
        )
        val absoluteAngleHandMotion: AbsoluteAngleWingMotion
        when (action) {
            ActionsArms.RAISE_ARM -> when (arm) {
                TypeArm.LEFT -> {

                    // Speed is a value between 1 and 8
                    // Angle is a value between 0 and 270
                    absoluteAngleHandMotion = AbsoluteAngleWingMotion(absolutePart[0],7, 10)
                    handMotionManager.doAbsoluteAngleMotion(absoluteAngleHandMotion)

                }

                TypeArm.RIGHT -> {

                    absoluteAngleHandMotion = AbsoluteAngleWingMotion(absolutePart[1],7, 10)
                    handMotionManager.doAbsoluteAngleMotion(absoluteAngleHandMotion)

                }

                TypeArm.BOTH -> {

                    absoluteAngleHandMotion = AbsoluteAngleWingMotion(absolutePart[0],7, 10)
                    handMotionManager.doAbsoluteAngleMotion(absoluteAngleHandMotion)
                }
            }

            ActionsArms.LOWER_ARM -> when (arm) {
                TypeArm.LEFT -> {

                    absoluteAngleHandMotion = AbsoluteAngleWingMotion(absolutePart[0],7, 170)
                    handMotionManager.doAbsoluteAngleMotion(absoluteAngleHandMotion)
                }

                TypeArm.RIGHT -> {
                    absoluteAngleHandMotion = AbsoluteAngleWingMotion(absolutePart[1],7, 170)
                    handMotionManager.doAbsoluteAngleMotion(absoluteAngleHandMotion)
                }

                TypeArm.BOTH -> {

                    absoluteAngleHandMotion = AbsoluteAngleWingMotion(absolutePart[2],7, 170)
                    handMotionManager.doAbsoluteAngleMotion(absoluteAngleHandMotion)
                }
            }
        }
        return true
    }

    // Function to put the arms in their original position, in this case: downwards
    fun reset(): Boolean {
        val absolutePart = byteArrayOf(
            AbsoluteAngleWingMotion.PART_LEFT,
            AbsoluteAngleWingMotion.PART_RIGHT,
            AbsoluteAngleWingMotion.PART_BOTH
        )

        val absoluteAngleHandMotion: AbsoluteAngleWingMotion = AbsoluteAngleWingMotion(absolutePart[2],7, 170)

        handMotionManager.doAbsoluteAngleMotion(absoluteAngleHandMotion)

        return true
    }
}