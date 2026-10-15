package com.siren.homeproxy

import android.app.assist.AssistContent
import android.app.assist.AssistStructure
import android.content.Context
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService
import android.util.Log

/**
 * SirenVoiceInteractionSessionService
 *
 * Mandatory companion session service for [SirenVoiceInteractionService].
 *
 * Zero-UI Invariant: If a student triggers an assist gesture (e.g. corner swipe or
 * long-press power button), the session immediately calls [hide] and suppresses
 * all visual overlays, preserving native UX with zero UI pops.
 */
class SirenVoiceInteractionSessionService : VoiceInteractionSessionService() {

    override fun onNewSession(args: Bundle?): VoiceInteractionSession {
        return SirenVoiceSession(this)
    }

    private class SirenVoiceSession(context: Context) : VoiceInteractionSession(context) {

        companion object {
            private const val TAG = "SirenVoiceSession"
        }

        override fun onCreate() {
            super.onCreate()
            // Ensure no window is drawn or shown
            window?.window?.setWindowAnimations(0)
            window?.window?.setBackgroundDrawable(null)
        }

        override fun onHandleAssist(
            data: Bundle?,
            structure: AssistStructure?,
            content: AssistContent?
        ) {
            super.onHandleAssist(data, structure, content)
            Log.d(TAG, "Assist gesture intercepted — hiding session immediately (Zero UI).")
            hide()
        }

        override fun onShow(args: Bundle?, showFlags: Int) {
            super.onShow(args, showFlags)
            hide()
        }
    }
}
