package com.example.medreminder.reminder

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsSpeaker(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    init {
        tts = TextToSpeech(context, this)
    }
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.CHINESE
        }
    }
    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts")
    }
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
