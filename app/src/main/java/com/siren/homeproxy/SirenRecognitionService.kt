package com.siren.homeproxy

import android.content.Intent
import android.speech.RecognitionService

/**
 * SirenRecognitionService
 *
 * Dummy [RecognitionService] implementation required by Android's
 * [android.service.voice.VoiceInteractionService] specification.
 *
 * Android requires every valid VoiceInteractionService to declare a corresponding
 * RecognitionService in its XML metadata. This dummy service provides no-op
 * audio listening handlers with zero background processing or microphone recording.
 */
class SirenRecognitionService : RecognitionService() {

    override fun onStartListening(recognizerIntent: Intent?, listener: Callback?) {
        // No-op: SIREN is a silent watchdog, not a voice transcriber
    }

    override fun onCancel(listener: Callback?) {
        // No-op
    }

    override fun onStopListening(listener: Callback?) {
        // No-op
    }
}
