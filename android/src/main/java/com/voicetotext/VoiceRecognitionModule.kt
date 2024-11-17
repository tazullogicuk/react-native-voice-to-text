package com.voicetotext

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.facebook.react.bridge.*
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.modules.core.DeviceEventManagerModule

@ReactModule(name = VoiceRecognitionModule.NAME)
class VoiceRecognitionModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext), RecognitionListener {

    companion object {
        const val NAME = "VoiceRecognition"
    }

    private var speechRecognizer: SpeechRecognizer? = null

    init {
        // Initialize SpeechRecognizer on the main thread
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(reactContext)
            speechRecognizer?.setRecognitionListener(this)
        }
    }

    override fun getName(): String {
        return NAME
    }

    @ReactMethod
    fun requestMicrophonePermission(callback: Callback) {
        if (reactContext.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            currentActivity?.let {
                it.requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1)
                callback.invoke("Permission requested")
            } ?: run {
                callback.invoke("Error: Activity not available")
            }
        } else {
            callback.invoke("Permission already granted")
        }
    }

    @ReactMethod
    fun startRecognition(locale: String, callback: Callback) {
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post {
            speechRecognizer?.let {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale)
                it.startListening(intent)
                callback.invoke(null)
            } ?: run {
                callback.invoke("SpeechRecognizer is not initialized")
            }
        }
    }

    @ReactMethod
    fun stopRecognition(callback: Callback) {
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post {
            speechRecognizer?.stopListening()
            callback.invoke(null)
        }
    }

    override fun onReadyForSpeech(params: Bundle) {
        val event = Arguments.createMap()
        sendEvent("onSpeechStart", event)
    }

    override fun onBeginningOfSpeech() {
        val event = Arguments.createMap()
        sendEvent("onSpeechStart", event)
    }

    override fun onRmsChanged(rmsdB: Float) {
        val event = Arguments.createMap()
        event.putDouble("value", rmsdB.toDouble())
        sendEvent("onSpeechVolumeChanged", event)
    }

    override fun onBufferReceived(buffer: ByteArray) {
        // No action needed for now
    }

    override fun onEndOfSpeech() {
        val event = Arguments.createMap()
        sendEvent("onSpeechEnd", event)
    }

    override fun onError(error: Int) {
        val event = Arguments.createMap()
        event.putString("error", getErrorText(error))
        sendEvent("onSpeechError", event)
    }

    override fun onResults(results: Bundle) {
        val arr = Arguments.createArray()
        val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        matches?.forEach { result ->
            arr.pushString(result)
        }

        val event = Arguments.createMap()
        event.putArray("value", arr)
        sendEvent("onSpeechResults", event)
        Log.d("ASR", "onResults()")
    }

    override fun onPartialResults(results: Bundle) {
        val arr = Arguments.createArray()
        val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        matches?.forEach { result ->
            arr.pushString(result)
        }

        val event = Arguments.createMap()
        event.putArray("value", arr)
        sendEvent("onSpeechPartialResults", event)
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        // Implementing onEvent to satisfy RecognitionListener interface
    }

    private fun sendEvent(eventName: String, params: WritableMap?) {
        if (reactContext.hasActiveCatalystInstance()) {
            reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit(eventName, params)
        }
    }

    private fun getErrorText(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "Error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error, please try again."
        }
    }
}
