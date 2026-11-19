package com.jarvis.ai

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.speech.tts.TextToSpeech
import android.widget.*
import android.graphics.Color
import android.view.Gravity
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var speech: SpeechRecognizer? = null
    private lateinit var status: TextView
    private lateinit var result: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setPadding(30, 30, 30, 30)
        layout.setBackgroundColor(Color.rgb(8, 15, 30))

        val title = TextView(this)
        title.text = "J.A.R.V.I.S"
        title.textSize = 34f
        title.setTextColor(Color.CYAN)
        title.gravity = Gravity.CENTER

        status = TextView(this)
        status.text = "SYSTEM ONLINE"
        status.textSize = 18f
        status.setTextColor(Color.GREEN)
        status.gravity = Gravity.CENTER

        result = TextView(this)
        result.text = "Hello! I am JARVIS."
        result.textSize = 20f
        result.setTextColor(Color.WHITE)
        result.gravity = Gravity.CENTER

        val button = Button(this)
        button.text = "🎙 TALK TO JARVIS"
        button.textSize = 18f

        button.setOnClickListener {
            startListening()
        }

        layout.addView(title)
        layout.addView(status)
        layout.addView(result)
        layout.addView(button)

        setContentView(layout)
    }

    override fun onInit(code: Int) {
        if (code == TextToSpeech.SUCCESS) {
            tts.language = Locale("en", "IN")
            tts.speak(
                "Hello, I am Jarvis. How can I help you?",
                TextToSpeech.QUEUE_FLUSH,
                null,
                "welcome"
            )
        }
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.text = "Speech recognition unavailable"
            return
        }

        speech?.destroy()
        speech = SpeechRecognizer.createSpeechRecognizer(this)

        speech?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                status.text = "Listening..."
            }

            override fun onResults(results: Bundle?) {
                val words = results?.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION
                )
                val command = words?.firstOrNull() ?: ""
                result.text = command
                status.text = "Command received"
                reply(command)
            }

            override fun onError(error: Int) {
                status.text = "Please try again"
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            "en-IN"
        )

        speech?.startListening(intent)
    }

    private fun reply(command: String) {
        val answer = when {
            command.contains("hello", true) ->
                "Hello! How are you?"

            command.contains("your name", true) ->
                "I am Jarvis, your personal assistant."

            command.contains("time", true) ->
                "The time is " + java.text.SimpleDateFormat(
                    "hh:mm a", Locale.getDefault()
                ).format(java.util.Date())

            command.contains("how are you", true) ->
                "I am doing great. How can I help you?"

            else ->
                "I heard you say $command. More AI features are coming soon."
        }

        result.text = answer
        tts.speak(answer, TextToSpeech.QUEUE_FLUSH, null, "reply")
    }

    override fun onDestroy() {
        speech?.destroy()
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}
