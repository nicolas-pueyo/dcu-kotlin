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

class SpeechControl(val speechManager: SpeechManager?) {
    private var lastTextToSpeak: String = ""
    private var isListeningEnabled = false
    private var isCurrentlySpeaking = false

    val isRobotTalking: Boolean
        get() = isCurrentlySpeaking || (speechManager?.isSpeaking?.result == "1")

    private val unifiedSpeechListener = object : RecognizeListener, SpeakListener {
        override fun onSpeakStatus(speakStatus: SpeakStatus) {
            if (speakStatus.progress >= 100f) {
                isCurrentlySpeaking = false
                if (isListeningEnabled) {
                    speechManager?.doWakeUp()
                }
            } else {
                isCurrentlySpeaking = true
            }
        }

        override fun onError(engine: Int, errorCode: Int) {
            if (isListeningEnabled && errorCode == 20005 && !isCurrentlySpeaking) {
                if (lastTextToSpeak.isNotEmpty()) {
                    talk(lastTextToSpeak)
                } else {
                    speechManager?.doWakeUp()
                }
            }
        }

        override fun onRecognizeResult(grammar: Grammar): Boolean {
            val text = grammar.text?.trim().orEmpty()
            if (text.isNotEmpty()) {
                externalRecognizedCallback?.invoke(text)
            }
            return true
        }

        override fun onRecognizeText(recognizeText: RecognizeTextBean) {
            val text = recognizeText.text?.trim().orEmpty()
            if (text.isNotEmpty()) {
                externalRecognizedCallback?.invoke(text)
            }
        }

        override fun onRecognizeVolume(volume: Int) {}
        override fun onStartRecognize() {}
        override fun onStopRecognize() {}
    }

    private var externalRecognizedCallback: ((String) -> Unit)? = null

    init {
        speechManager?.setOnSpeechListener(unifiedSpeechListener)
    }

    fun talk(answer: String) {
        if (answer.isEmpty() || speechManager == null) return
        lastTextToSpeak = answer
        isCurrentlySpeaking = true
        speechManager.startSpeak(answer, speakOption)
    }

    fun talk(answer: String, speed: Int) {
        if (answer.isEmpty() || speechManager == null) return
        lastTextToSpeak = answer
        speakOption.speed = speed
        isCurrentlySpeaking = true
        speechManager.startSpeak(answer, speakOption)
    }

    fun stopTalking() {
        speechManager?.stopSpeak()
        isCurrentlySpeaking = false
    }

    fun wakeUp() {
        speechManager?.doWakeUp()
    }

    fun sleep() {
        speechManager?.doSleep()
    }

    fun startListening(
        onRecognized: (String) -> Unit,
        onError: ((Int, Int) -> Unit)? = null,
        onStart: (() -> Unit)? = null,
        onStop: (() -> Unit)? = null,
        blockRobotResponse: Boolean = true
    ) {
        isListeningEnabled = true
        externalRecognizedCallback = onRecognized
        if (!isCurrentlySpeaking) {
            speechManager?.doWakeUp()
        }
    }

    fun stopListening() {
        isListeningEnabled = false
        externalRecognizedCallback = null
        speechManager?.doSleep()
    }

    companion object {
        private val speakOption: SpeakOption = SpeakOption().apply {
            speed = 50
            intonation = 50
        }
    }
}
