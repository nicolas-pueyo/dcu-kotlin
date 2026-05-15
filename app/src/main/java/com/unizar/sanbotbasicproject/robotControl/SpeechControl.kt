package com.unizar.sanbotbasicproject.robotControl

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sanbot.opensdk.function.beans.SpeakOption
import com.sanbot.opensdk.function.beans.speech.Grammar
import com.sanbot.opensdk.function.beans.speech.RecognizeTextBean
import com.sanbot.opensdk.function.beans.speech.SpeakStatus
import com.sanbot.opensdk.function.unit.SpeechManager
import com.sanbot.opensdk.function.unit.interfaces.speech.RecognizeListener
import com.sanbot.opensdk.function.unit.interfaces.speech.SpeakListener
import com.sanbot.opensdk.function.unit.interfaces.speech.WakenListener

class SpeechControl(val speechManager: SpeechManager?) {

    private var isWaitingForResponse = false
    private var isSpeaking = false
    private var pendingSleepAfterSpeak = false
    var isListening by mutableStateOf(false)
        private set
    private var onTextRecognized: ((String) -> Unit)? = null

    // El Handler para gestionar los tiempos de respuesta
    private val speechHandler = Handler(Looper.getMainLooper())

    init {
        setupListeners()
    }


    private fun setupListeners() {
        if (speechManager == null) return

        // Cuando termina una locución pendiente, cerramos el micro si alguien pidió parar antes.
        speechManager.setOnSpeechListener(object : SpeakListener {
            override fun onSpeakStatus(speakStatus: SpeakStatus) {
                isSpeaking = speakStatus.progress < 100f
                if (speakStatus.progress >= 100f) {
                    Log.d("SpeechControl", "Robot terminó de hablar")
                    if (pendingSleepAfterSpeak) {
                        speechHandler.postDelayed({
                            finishStopListening()
                        }, 200)
                    }
                }
            }
        })

        // Listener de reconocimiento
        speechManager.setOnSpeechListener(object : RecognizeListener {
            override fun onRecognizeText(recognizeText: RecognizeTextBean) {}

            override fun onRecognizeResult(grammar: Grammar): Boolean {
                val text = grammar.text?.trim().orEmpty()
                if (text.isNotEmpty() && isWaitingForResponse) {
                    Log.i("SpeechControl", "Reconocido: $text")
                    isWaitingForResponse = false
                    onTextRecognized?.invoke(text)
                }
                // Quizás sobra el return por el manifest, lo dejo porqe funciona
                return true
            }

            override fun onRecognizeVolume(volume: Int) {}
            override fun onStartRecognize() {}
            override fun onStopRecognize() {}
            override fun onError(engine: Int, errorCode: Int) {
                Log.e("SpeechControl", "Error: $errorCode")
            }
        })

        // Listener del estado del micrófono
        speechManager.setOnSpeechListener(object : WakenListener {
            override fun onWakeUpStatus(b: Boolean) {}
            override fun onWakeUp() {
                Log.d("SpeechControl", "Micro Abierto")
                isListening = true
            }
            override fun onSleep() {
                Log.d("SpeechControl", "Micro Cerrado")
                isListening = false
            }
        })
    }

    /**
     * Detiene el habla actual y abre el micrófono inmediatamente.
     */
    fun interruptAndListen() {
        Log.d("SpeechControl", "Interrumpiendo habla y abriendo micro")
        isWaitingForResponse = true
        pendingSleepAfterSpeak = false
        speechManager?.stopSpeak() // Detiene el habla si la hay
        // Damos un pequeño margen para que el motor de voz se detenga antes de abrir micro
        speechHandler.postDelayed({
            speechManager?.doWakeUp()
        }, 300)
    }

    fun ask(question: String, onResponse: (String) -> Unit) {
        if (speechManager == null) return

        isWaitingForResponse = true
        pendingSleepAfterSpeak = false
        isSpeaking = true
        onTextRecognized = onResponse

        // Hablamos
        speechManager.startSpeak(question, SpeakOption().apply { speed = 50; intonation = 50 })
    }

    fun talk(text: String) {
        pendingSleepAfterSpeak = false
        isSpeaking = true
        speechManager?.startSpeak(text, SpeakOption().apply { speed = 50; intonation = 50 })
    }

    fun stopListening() {
        isWaitingForResponse = false
        pendingSleepAfterSpeak = true

        if (!isSpeaking) {
            finishStopListening()
        }
    }

    private fun finishStopListening() {
        pendingSleepAfterSpeak = false
        isListening = false
        speechManager?.doSleep()
        speechHandler.removeCallbacksAndMessages(null)
    }
}