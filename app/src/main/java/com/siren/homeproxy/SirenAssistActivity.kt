package com.siren.homeproxy

import android.app.Activity
import android.os.Bundle

/**
 * SirenAssistActivity
 *
 * Ultra-fast zero-UI interceptor activity that swallows long-press assist and
 * voice command gestures (e.g. from Home button, Power button, or headset hook).
 *
 * Invariant: Finishes immediately in < 1ms with window animations disabled,
 * preventing Google App from launching and redirecting to ManageAssistActivity.
 */
class SirenAssistActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setWindowAnimations(0)
        window.setBackgroundDrawable(null)
        super.onCreate(savedInstanceState)
        finish()
    }

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        finish()
    }
}
