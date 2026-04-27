package com.unizar.sanbotbasicproject.robotControl

import android.os.Handler
import android.os.Looper
import android.util.Log
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
    var onListeningStateChanged: ((Boolean) -> Unit)? = null
    private var onTextRecognized: ((String) -> Unit)? = null

    // El Handler para gestionar los tiempos de respuesta
    private val speechHandler = Handler(Looper.getMainLooper())

    init {
        setupListeners()
    }


    private fun setupListeners() {
        if (speechManager == null) return

        // Listener de habla ya no usado por la funcionalidad de darle en la cabeza
//        speechManager.setOnSpeechListener(object : SpeakListener {
//            override fun onSpeakStatus(speakStatus: SpeakStatus) {
//                if (speakStatus.progress >= 100f) {
//                    Log.d("SpeechControl", "Robot terminó de hablar")
//                    if (isWaitingForResponse) {
//                        // Forzamos el despertado con un pequeño delay como hace Igor
//                        speechHandler.postDelayed({
//                            speechManager.doWakeUp()
//                        }, 200)
//                    }
//                }
//            }
//        })

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
                onListeningStateChanged?.invoke(true)
            }
            override fun onSleep() {
                Log.d("SpeechControl", "Micro Cerrado")
                onListeningStateChanged?.invoke(false)
            }
        })
    }

    /**
     * Detiene el habla actual y abre el micrófono inmediatamente.
     */
    fun interruptAndListen() {
        Log.d("SpeechControl", "Interrumpiendo habla y abriendo micro")
        isWaitingForResponse = true
        speechManager?.stopSpeak() // Detiene el habla si la hay
        // Damos un pequeño margen para que el motor de voz se detenga antes de abrir micro
        speechHandler.postDelayed({
            speechManager?.doWakeUp()
        }, 300)
    }

    fun ask(question: String, onResponse: (String) -> Unit) {
        if (speechManager == null) return

        isWaitingForResponse = true
        onTextRecognized = onResponse

        // Hablamos
        speechManager.startSpeak(question, SpeakOption().apply { speed = 50; intonation = 50 })
    }

    fun stopListening() {
        isWaitingForResponse = false
        speechManager?.doSleep()
        speechManager?.stopSpeak()
        speechHandler.removeCallbacksAndMessages(null)
    }
}