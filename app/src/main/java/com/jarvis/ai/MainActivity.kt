package com.jarvis.ai

import android.app.Activity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this) {
            tts.language = Locale.US
        }

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 80, 40, 40)

        val title = TextView(this)
        title.text = "JARVIS AI"
        title.textSize = 32f

        val button = Button(this)
        button.text = "TALK TO JARVIS"

        button.setOnClickListener {
            tts.speak(
                "Hello! I am Jarvis. How can I help you?",
                TextToSpeech.QUEUE_FLUSH,
                null,
                "jarvis"
            )
        }

        layout.addView(title)
        layout.addView(button)

        setContentView(layout)
    }

    override fun onDestroy() {
        if (::tts.isInitialized) tts.shutdown()
        super.onDestroy()
    }
}
