package io.github.sanlorng.warmtones.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import java.util.Locale

class SystemTtsHelper(
    context: Context,
    private val onDataMissing: () -> Unit
) : TtsHelper, TextToSpeech.OnInitListener {

    private val tts: TextToSpeech by lazy { TextToSpeech(context, this) }
    private val ttsInitialized = CompletableDeferred<Unit>()

    init {
        // Eagerly initialize TTS
        tts
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language data missing or language not supported.")
                onDataMissing()
                ttsInitialized.completeExceptionally(IllegalStateException("TTS language data missing or not supported"))
            } else {
                Log.d("TTS", "Initialization successful.")
                ttsInitialized.complete(Unit)
            }
        } else {
            Log.e("TTS", "Initialization failed with status: $status")
            ttsInitialized.completeExceptionally(IllegalStateException("TTS initialization failed with status: $status"))
        }
    }

    override suspend fun speak(text: String) {
        try {
            ttsInitialized.await()
            val result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            if (result == TextToSpeech.ERROR) {
                Log.e("TTS", "Speak failed for text: $text")
            }
        } catch (e: Exception) {
            Log.e("TTS", "Speak failed with exception", e)
        }
    }

    override fun shutdown() {
        tts.shutdown()
    }
}