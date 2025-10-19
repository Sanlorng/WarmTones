package io.github.sanlorng.warmtones.data

import android.content.Context
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import java.util.Locale

class TtsRepository(private val context: Context) {

    private val _isTtsAvailable = MutableStateFlow(isTtsEngineAvailable())
    val isTtsAvailable = _isTtsAvailable.asStateFlow()

    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status != TextToSpeech.SUCCESS) {
                _isTtsAvailable.tryEmit(false)
            } else {
                _isTtsAvailable.value = true
                tts?.let {
//                    val result = it.setLanguage(Locale.getDefault())
//                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//                        _isTtsAvailable.tryEmit(false)
//                    } else {
//                        _isTtsAvailable.value = true
//                    }
                }
            }
        }
    }

    private fun isTtsEngineAvailable(): Boolean {
        val tts = TextToSpeech(context, null)
        val isAvailable = tts.engines.isNotEmpty()
        tts.shutdown()
        return isAvailable
    }

    suspend fun checkTtsAvailability() {
         _isTtsAvailable.value = callbackFlow {
            var tts: TextToSpeech? = null
            tts = TextToSpeech(context) {
                if (it == TextToSpeech.SUCCESS) {
                    if (tts?.engines.isNullOrEmpty()) {
                        channel.trySend(false)
                        close()
                    }
                    val langResult = tts?.setLanguage(Locale.getDefault())
                    channel.trySend(when(langResult) {
//                        TextToSpeech.LANG_MISSING_DATA -> false
//                        TextToSpeech.LANG_NOT_SUPPORTED -> false
                        else -> {
                            if (tts?.engines.isNullOrEmpty()) {
                                false
                            } else {
                                this@TtsRepository.tts?.shutdown()
                                this@TtsRepository.tts = tts
                                true
                            }

                        }
                    })
                } else {
                    channel.trySend(false)
                }
                close()
            }
            awaitClose {  }
        }.first()
    }

    suspend fun speak(text: String) {
        val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        if (result == TextToSpeech.ERROR) {
            checkTtsAvailability()
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun shutdown() {
        tts?.shutdown()
    }
}