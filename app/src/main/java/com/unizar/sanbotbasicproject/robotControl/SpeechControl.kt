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

class SpeechControl(val speechManager: SpeechManager) {
    private var stopTalking = false

    val isRobotTalking: Boolean
        get() {
            val or: OperationResult? = speechManager.isSpeaking
            return or?.result == "1"
        }

    fun talk(answer: String) {
        Log.d("SpeechControl", "Talking: $answer")
        speechManager.startSpeak(answer, speakOption)
    }

    fun talk(answer: String, speed: Int) {
        speakOption.speed = speed
        Log.d("SpeechControl", "Talking: $answer with speed $speed")
        speechManager.startSpeak(answer, speakOption)
    }

    fun stopTalking() {
        if (isRobotTalking) {
            speechManager.stopSpeak()
        }
    }

    fun wakeUp() {
        speechManager.doWakeUp()
    }

    fun sleep() {
        speechManager.doSleep()
    }

    fun startListening(
        onRecognized: (String) -> Unit,
        onError: ((Int, Int) -> Unit)? = null,
        onStart: (() -> Unit)? = null,
        onStop: (() -> Unit)? = null,
        blockRobotResponse: Boolean = true
    ) {
        // Establecemos el listener antes de despertar para no perder eventos
        speechManager.setOnSpeechListener(object : RecognizeListener {
            override fun onError(engine: Int, errorCode: Int) {
                Log.e("SpeechControl", "Error: $engine / $errorCode")
                onError?.invoke(engine, errorCode)
            }

            override fun onRecognizeResult(grammar: Grammar): Boolean {
                val text = grammar.text?.trim().orEmpty()
                if (text.isNotEmpty()) {
                    Log.d("SpeechControl", "Recognized Result: $text")
                    onRecognized(text)
                }
                return blockRobotResponse
            }

            override fun onRecognizeText(recognizeText: RecognizeTextBean) {
                val text = recognizeText.text?.trim().orEmpty()
                if (text.isNotEmpty()) {
                    Log.d("SpeechControl", "Recognized Text: $text")
                    onRecognized(text)
                }
            }

            override fun onRecognizeVolume(i: Int) {
                // opcional
            }

            override fun onStartRecognize() {
                Log.d("SpeechControl", "Start listening")
                onStart?.invoke()
            }

            override fun onStopRecognize() {
                Log.d("SpeechControl", "Stop listening")
                onStop?.invoke()
            }
        })
        
        speechManager.doWakeUp()
    }

    fun stopListening() {
        speechManager.doSleep()
    }

    companion object {
        private val speakOption: SpeakOption = SpeakOption().apply {
            speed = 50
            intonation = 50
        }
    }
}
