package com.unizar.sanbotbasicproject.robotControl

import android.util.Log
import com.sanbot.opensdk.beans.OperationResult
import com.sanbot.opensdk.function.beans.SpeakOption
import com.sanbot.opensdk.function.beans.speech.Grammar
import com.sanbot.opensdk.function.beans.speech.RecognizeTextBean
import com.sanbot.opensdk.function.beans.speech.SpeakStatus
import com.sanbot.opensdk.function.unit.SpeechManager
import com.sanbot.opensdk.function.unit.interfaces.speech.RecognizeListener
import com.sanbot.opensdk.function.unit.interfaces.speech.SpeakListener
import com.sanbot.opensdk.function.unit.interfaces.speech.WakenListener
import java.util.concurrent.CountDownLatch

class SpeechControl(speechManager: SpeechManager) {
    private val speechManager: SpeechManager = speechManager
    private var recognizedString: String? = null
    private var stopTalking = false

    val isRobotTalking: Boolean
        // Function indicating whether the robot is talking or not
        get() {
            val or: OperationResult? = speechManager.isSpeaking
            // Returns true if or.result == “1”, otherwise false
            return or?.result == "1"
        }

    // Function that uses speech synthesis to pronounce the phrase passed as a parameter with
    // phrase that is passed as a parameter with the intonation
    // and speed passed in the constructor.
    fun talk(answer: String) {
        Log.d("speak", "I'm going to speak")
        Log.d("speakoption speed", java.lang.String.valueOf(speakOption.speed))
        Log.d("speakoption intonation", java.lang.String.valueOf(speakOption.intonation))
        speechManager.startSpeak(answer, speakOption)
    }

    // Method to to pronounce the phrase passed as a parameter and an int parameter to change the speed
    fun talk(answer: String, speed: Int) {
        speakOption.speed=speed
        Log.d("speak", "I'm going to speak")
        Log.d("speakoption speed", java.lang.String.valueOf(speakOption.speed))
        Log.d("speakoption intonation", java.lang.String.valueOf(speakOption.intonation))
        speechManager.startSpeak(answer, speakOption)
    }

    // Function that stops robot speech.
    fun stopTalking() {
        speechManager.stopSpeak()
    }

    // Function that obtains the dialog that the user has said
    // from the moment the robot goes into WakeUp mode until the
    // user stops talking
    fun modeListening(): String? {
        recognizedString = null
        speechManager.doWakeUp()
        speechManager.setOnSpeechListener(object : RecognizeListener {
            override fun onError(engine: Int, errorCode: Int) {
                TODO("Not yet implemented")
            }

            override fun onRecognizeResult(grammar: Grammar): Boolean {
                recognizedString = grammar.text
                Log.d("test RecognizeResult", recognizedString!!)
                return true
            }

            override fun onRecognizeText(recognizeText: RecognizeTextBean) {
                TODO("Not yet implemented")
            }

            override fun onRecognizeVolume(i: Int) {
            }

            override fun onStartRecognize() {
                TODO("Not yet implemented")
            }

            override fun onStopRecognize() {
                TODO("Not yet implemented")
            }
        })
        while (recognizedString == null || (recognizedString ?: "").isEmpty()) {
        }
        return recognizedString
    }

    // Function that waits for the robot to finish speaking
    fun isFinished(): Boolean {
        stopTalking = false
        speechManager.setOnSpeechListener(object : SpeakListener {
            // Action to be executed when the robot has finished speaking
            fun onSpeakFinish() {
                // If in automatic conversation mode
                Log.d("end", "finish speaking")
                stopTalking = true
            }

            fun onSpeakProgress(i: Int) {
                // ...
            }

            override fun onSpeakStatus(speakStatus: SpeakStatus) {
                TODO("Not yet implemented")
            }
        })
        while (!stopTalking) {
        }
        return stopTalking
    }

    fun isFinished2(): Boolean {
        val latch = CountDownLatch(1)

        speechManager.setOnSpeechListener(object : SpeakListener {
            fun onSpeakFinish() {
                Log.d("the end", "I finished talking")
                latch.countDown() // Decrease the latch when speech has ended
            }

            fun onSpeakProgress(i: Int) {
                // Implement if necessary
            }

            override fun onSpeakStatus(speakStatus: SpeakStatus) {
                TODO("Not yet implemented")
            }
        })

        try {
            latch.await() // Wait until countDown() is called
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt() // Restore interruption status
            Log.e("SpeechHandler", "Thread was interrupted", e)
        }

        return true // After latch.countDown() is called, we know that it has finished.
    }

    var speakingSpeed: Int
        get() = speakOption.speed
        set(speed) {
            Log.d("speakoption speed", "changing speed to $speed")
            speakOption.speed = speed
        }

    var speakingIntonation: Int
        get() = speakOption.intonation
        set(intonation) {
            Log.d("speakoption intonation", "changing intonation to $intonation")
            speakOption.intonation = intonation
        }

    fun initListener() {

        speechManager.setOnSpeechListener(object : WakenListener {
            override fun onWakeUp() {
                println("onWakeUp ----------------------------------------------")
            }

            override fun onWakeUpStatus(isWakeUpByVoice: Boolean) {
                TODO("Not yet implemented")
            }

            override fun onSleep() {
                println("onSleep ----------------------------------------------")
            }
        })


        speechManager.setOnSpeechListener(object : RecognizeListener {
            override fun onError(engine: Int, errorCode: Int) {
                TODO("Not yet implemented")
            }

            override fun onRecognizeResult(grammar: Grammar): Boolean {
                //Log.i("recognition：", "onRecognizeResult: "+grammar.getText());
                // If you acknowledge “hello” sanbot responds “hello”.
                System.out.println(grammar.text)
                return true
            }

            override fun onRecognizeText(recognizeText: RecognizeTextBean) {
                TODO("Not yet implemented")
            }

            override fun onRecognizeVolume(i: Int) {
                println("onRecognizeVolume ----------------------------------------------")
            }

            override fun onStartRecognize() {
                TODO("Not yet implemented")
            }

            override fun onStopRecognize() {
                TODO("Not yet implemented")
            }
        })
    }


    companion object {
        private val speakOption: SpeakOption = SpeakOption()
    }
}


