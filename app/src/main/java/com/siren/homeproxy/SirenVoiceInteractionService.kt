package com.siren.homeproxy

import android.content.ContentProviderClient
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.service.voice.VoiceInteractionService
import android.util.Log

/**
 * SirenVoiceInteractionService
 *
 * 24/7 Digital Assistant Watchdog bound by Android's [VoiceInteractionManagerService]
 * with BIND_IMPORTANT when SIREN is selected as the Default Digital Assistant (ROLE_ASSISTANT).
 *
 * Maintains a kernel [IBinder.DeathRecipient] link on QIEZKA's ContentProvider
 * (com.uncode.app.provider). When QIEZKA's process dies or is killed:
 * 1. The Linux kernel notifies [binderDied] in microseconds.
 * 2. SIREN fires ACTION_REVIVE with FLAG_INCLUDE_STOPPED_PACKAGES.
 * 3. SIREN queries SirenContentProvider in < 50ms, forcing the OS to cold-start QIEZKA.
 */
class SirenVoiceInteractionService : VoiceInteractionService() {

    companion object {
        private const val TAG = "SirenVoiceService"
        private const val RELINK_DELAY_MS = 500L
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var providerClient: ContentProviderClient? = null
    private var linkedBinder: IBinder? = null
    private var isServiceActive = false

    private val deathRecipient = object : IBinder.DeathRecipient {
        override fun binderDied() {
            Log.w(TAG, "Kernel death notification received: QIEZKA process died!")
            cleanupDeathLink()
            resurrectQiezka()
            scheduleRelink()
        }
    }

    override fun onReady() {
        super.onReady()
        isServiceActive = true
        Log.i(TAG, "SirenVoiceInteractionService onReady — Watchdog initialized 24/7.")
        // Immediately verify QIEZKA status and establish kernel death recipient
        resurrectQiezka()
        scheduleRelink()
    }

    override fun onShutdown() {
        isServiceActive = false
        cleanupDeathLink()
        mainHandler.removeCallbacksAndMessages(null)
        Log.i(TAG, "SirenVoiceInteractionService onShutdown.")
        super.onShutdown()
    }

    /**
     * Wakes QIEZKA's headless core immediately via dual resurrection channels:
     * 1. Broadcast ACTION_REVIVE with FLAG_INCLUDE_STOPPED_PACKAGES
     * 2. Direct ContentProvider call to force instant process cold-start (< 50ms)
     */
    fun resurrectQiezka() {
        try {
            // 1. Broadcast wake-up
            val reviveIntent = Intent(SirenConfig.REVIVE_ACTION).apply {
                setPackage(SirenConfig.TARGET_PACKAGE)
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            }
            sendBroadcast(reviveIntent)
            Log.d(TAG, "Dispatched ACTION_REVIVE broadcast to QIEZKA.")

            // 2. Direct ContentProvider IPC cold-start
            val bundle = contentResolver.call(
                SirenConfig.TARGET_PROVIDER_URI,
                "ping",
                null,
                null
            )
            Log.d(TAG, "Pinged SirenContentProvider successfully: $bundle")
        } catch (e: Exception) {
            Log.w(TAG, "QIEZKA cold-start ping attempt: ${e.message}")
        }
    }

    /**
     * Attempts to acquire QIEZKA's provider binder and attach the kernel DeathRecipient.
     */
    private fun tryLinkToDeath(): Boolean {
        if (!isServiceActive) return false
        cleanupDeathLink()

        try {
            val client = contentResolver.acquireUnstableContentProviderClient(
                SirenConfig.TARGET_PROVIDER_AUTHORITY
            ) ?: run {
                Log.d(TAG, "Target provider client currently unavailable.")
                return false
            }

            val binder = getRemoteBinder(client)

            if (binder != null && binder.isBinderAlive) {
                binder.linkToDeath(deathRecipient, 0)
                providerClient = client
                linkedBinder = binder
                Log.i(TAG, "Successfully linked kernel DeathRecipient to QIEZKA provider binder.")
                return true
            } else {
                client.close()
            }
        } catch (e: Exception) {
            Log.d(TAG, "Failed linking DeathRecipient to QIEZKA: ${e.message}")
        }
        return false
    }

    /**
     * Extracts the underlying IBinder from ContentProviderClient via reflection if needed.
     */
    private fun getRemoteBinder(client: ContentProviderClient): IBinder? {
        return try {
            val field = client.javaClass.getDeclaredField("mContentProvider")
            field.isAccessible = true
            val provider = field.get(client)
            if (provider is IBinder) {
                provider
            } else if (provider is android.os.IInterface) {
                provider.asBinder()
            } else {
                provider?.javaClass?.getMethod("asBinder")?.invoke(provider) as? IBinder
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun scheduleRelink() {
        if (!isServiceActive) return
        mainHandler.removeCallbacksAndMessages(null)
        mainHandler.postDelayed({
            if (isServiceActive && (linkedBinder == null || !linkedBinder!!.isBinderAlive)) {
                val success = tryLinkToDeath()
                if (!success) {
                    resurrectQiezka()
                    scheduleRelink()
                }
            }
        }, RELINK_DELAY_MS)
    }

    private fun cleanupDeathLink() {
        try {
            linkedBinder?.unlinkToDeath(deathRecipient, 0)
        } catch (_: Exception) {}
        linkedBinder = null

        try {
            providerClient?.close()
        } catch (_: Exception) {}
        providerClient = null
    }
}
