package com.siren.homeproxy

import android.net.Uri

object SirenConfig {

    /**
     * App that SIREN Watchdog monitors and keeps alive 24/7.
     */
    const val TARGET_PACKAGE = "com.uncode.app"

    /**
     * SIREN's own package name.
     */
    const val SIREN_PACKAGE = "com.siren.homeproxy"

    /**
     * Broadcast action to revive QIEZKA Headless Core without waking full UI.
     */
    const val REVIVE_ACTION = "$TARGET_PACKAGE.ACTION_REVIVE"

    /**
     * QIEZKA's silent ContentProvider authority.
     */
    const val TARGET_PROVIDER_AUTHORITY = "$TARGET_PACKAGE.provider"

    val TARGET_PROVIDER_URI: Uri = Uri.parse("content://$TARGET_PROVIDER_AUTHORITY")

    /**
     * SIREN's reciprocal ContentProvider authority for QIEZKA to monitor.
     */
    const val SIREN_PROVIDER_AUTHORITY = "$SIREN_PACKAGE.provider"

    val SIREN_PROVIDER_URI: Uri = Uri.parse("content://$SIREN_PROVIDER_AUTHORITY")

    /**
     * Action to launch the transparent Assistant role request flow.
     */
    const val ACTION_REQUEST_ASSISTANT = "$SIREN_PACKAGE.ACTION_REQUEST_ASSISTANT"
}
