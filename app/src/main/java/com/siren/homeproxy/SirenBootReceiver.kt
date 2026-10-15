package com.siren.homeproxy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * SirenBootReceiver
 *
 * Direct-boot aware broadcast receiver that wakes QIEZKA on device boot or power-on.
 */
class SirenBootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SirenBoot"
        private val BOOT_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON"
        )
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        if (intent.action !in BOOT_ACTIONS) return

        Log.i(TAG, "Boot detected [${intent.action}] — waking target: ${SirenConfig.TARGET_PACKAGE}")

        try {
            // 1. Send broadcast
            val reviveIntent = Intent(SirenConfig.REVIVE_ACTION).apply {
                setPackage(SirenConfig.TARGET_PACKAGE)
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            }
            context.sendBroadcast(reviveIntent)

            // 2. Cold-start ping
            context.contentResolver.call(
                SirenConfig.TARGET_PROVIDER_URI,
                "ping",
                null,
                null
            )
            Log.i(TAG, "Target revival broadcast and IPC ping sent on boot.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed waking target on boot: ${e.message}")
        }
    }
}
