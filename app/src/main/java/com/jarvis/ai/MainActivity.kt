package com.jarvis.ai

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.*
import android.graphics.Color
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : Activity(),
    TextToSpeech.OnInitListener,
    RecognitionListener {

    private var tts: TextToSpeech? = null
    private var speech: SpeechRecognizer? = null

    private lateinit var status: TextView
    private lateinit var result: TextView
    private lateinit var button: Button
    private lateinit var languageButton: Button

    private var hindiMode = false
    private val micPermission = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setPadding(25, 40, 25, 40)
        layout.setBackgroundColor(Color.rgb(5, 12, 30))

        val title = TextView(this)
        title.text = "J.A.R.V.I.S"
        title.textSize = 38f
        title.setTextColor(Color.CYAN)
        title.gravity = Gravity.CENTER

        status = TextView(this)
        status.text = "SYSTEM ONLINE"
        status.textSize = 19f
        status.setTextColor(Color.GREEN)
        status.gravity = Gravity.CENTER

        result = TextView(this)
        result.text = "Hello! I am JARVIS."
        result.textSize = 21f
        result.setTextColor(Color.WHITE)
        result.gravity = Gravity.CENTER

        languageButton = Button(this)
        languageButton.text = "Language: English"
        languageButton.setOnClickListener {
            hindiMode = !hindiMode
            languageButton.text =
                if (hindiMode) "Language: Hindi"
                else "Language: English"

            status.text = "Language changed"
        }

        button = Button(this)
        button.text = "🎙 TALK TO JARVIS"
        button.textSize = 18f
        button.setOnClickListener {
            startListening()
        }

        layout.addView(title)
        layout.addView(status)
        layout.addView(result)
        layout.addView(languageButton)
        layout.addView(button)

        setContentView(layout)
    }

    override fun onInit(code: Int) {
        if (code == TextToSpeech.SUCCESS) {
            val languageResult =
                tts?.setLanguage(Locale("en", "IN"))

            if (languageResult == TextToSpeech.LANG_MISSING_DATA ||
                languageResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                status.text = "TTS language unavailable"
            } else {
                status.text = "SYSTEM ONLINE"
                speak("Hello, I am Jarvis. How can I help you?")
            }
        } else {
            status.text = "Voice output unavailable"
        }
    }

    private fun speak(message: String) {
        tts?.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "jarvis_reply"
        )
    }

    private fun startListening() {

        if (checkSelfPermission(
                android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                micPermission
            )
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.text = "Speech recognition unavailable"
            return
        }

        try {
            speech?.destroy()
            speech = SpeechRecognizer.createSpeechRecognizer(this)
            speech?.setRecognitionListener(this)

            val intent = Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            )

            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                if (hindiMode) "hi-IN" else "en-IN"
            )

            intent.putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                true
            )

            intent.putExtra(
                RecognizerIntent.EXTRA_PREFER_OFFLINE,
                false
            )

            status.text = "Starting microphone..."

            speech?.startListening(intent)

        } catch (e: Exception) {
            status.text = "Unable to start voice service"
            result.text = e.message ?: "Unknown error"
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        status.text = "Listening..."
    }

    override fun onBeginningOfSpeech() {
        status.text = "Speak now..."
    }

    override fun onRmsChanged(rmsdB: Float) {}

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        status.text = "Processing..."
    }

    override fun onResults(results: Bundle?) {
        val words = results?.getStringArrayList(
            SpeechRecognizer.RESULTS_RECOGNITION
        )

        val command = words?.firstOrNull() ?: ""

        result.text = command
        status.text = "Command received"

        if (command.isNotBlank()) {
            reply(command)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {}

    override fun onEvent(eventType: Int, params: Bundle?) {}

    override fun onError(error: Int) {

        val message = when (error) {
            1 -> "Network timeout"
            2 -> "Network connection error"
            3 -> "Audio recording error"
            4 -> "Google speech server error"
            5 -> "Voice service client error"
            6 -> "Please speak again"
            7 -> "No speech detected"
            8 -> "Voice service busy"
            9 -> "Microphone permission missing"
            10 -> "Too many requests"
            11 -> "Speech service disconnected. Tap again."
            12 -> "Language not supported"
            13 -> "Language unavailable"
            else -> "Voice error: $error"
        }

        status.text = message
        result.text = message

        if (error == 11) {
            speech?.destroy()
            speech = null
        }
    }

    private fun reply(command: String) {

        val text = command.lowercase(Locale.ROOT)
        val appPackage = when {
            text.contains("youtube") || text.contains("यूट्यूब") ->
                "com.google.android.youtube"
            text.contains("whatsapp") || text.contains("व्हाट्सएप") ->
                "com.whatsapp"
            text.contains("chrome") || text.contains("क्रोम") ->
                "com.android.chrome"
            else -> null
        }

        if (appPackage != null) {
            val launchIntent = packageManager.getLaunchIntentForPackage(appPackage)
            if (launchIntent != null) {
                startActivity(launchIntent)
                val appName = when (appPackage) {
                    "com.google.android.youtube" -> "YouTube"
                    "com.whatsapp" -> "WhatsApp"
                    else -> "Chrome"
                }
                val message = if (hindiMode)
                    "$appName खोल रहा हूँ।"
                else
                    "Opening $appName."
                result.text = message
                status.text = "JARVIS ONLINE"
                speak(message)
                return
            }
        }
        val answer = when {
            text.contains("hello") ||
            text.contains("नमस्ते") ||
            text.contains("हेलो") ->
                if (hindiMode) "नमस्ते! मैं आपकी कैसे मदद कर सकता हूँ?"
                else "Hello! How can I help you?"

            text.contains("your name") ||
            text.contains("तुम्हारा नाम") ->
                if (hindiMode) "मेरा नाम जार्विस है।"
                else "I am Jarvis, your personal assistant."

            text.contains("time") ||
text.contains("समय") ||
text.contains("टाइम") ->
                (if (hindiMode) "अभी समय है " else "The time is ") +
    SimpleDateFormat(
        "hh:mm a",
        Locale.getDefault()
    ).format(Date())

            text.contains("date") ||
            text.contains("तारीख") ->
                SimpleDateFormat(
                    "dd MMMM yyyy",
                    Locale.getDefault()
                ).format(Date())

            text.contains("how are you") ||
            text.contains("कैसे हो") ->
                if (hindiMode) "मैं बिल्कुल ठीक हूँ!"
                else "I am doing great!"

            text.contains("help") ||
            text.contains("मदद") ->
                if (hindiMode)
                    "आप मुझसे समय, तारीख और अपना नाम पूछ सकते हैं।"
                else
                    "You can ask me the time, date or my name."

            else ->
                if (hindiMode)
                    "मैंने सुना: $command"
                else
                    "I heard you say: $command"
        }

        result.text = answer
        status.text = "JARVIS ONLINE"
        speak(answer)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == micPermission) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                startListening()
            } else {
                status.text = "Microphone permission denied"
            }
        }
    }

    override fun onDestroy() {
        speech?.destroy()
        speech = null

        tts?.stop()
        tts?.shutdown()
        tts = null

        super.onDestroy()
    }
}
