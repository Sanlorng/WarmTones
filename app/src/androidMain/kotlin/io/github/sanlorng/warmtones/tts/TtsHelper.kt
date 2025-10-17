package io.github.sanlorng.warmtones.tts

interface TtsHelper {
    suspend fun speak(text: String)
    fun shutdown()
}