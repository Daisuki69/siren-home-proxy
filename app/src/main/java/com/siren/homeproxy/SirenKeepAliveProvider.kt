package com.siren.homeproxy

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log

/**
 * SirenKeepAliveProvider
 *
 * Lightweight reciprocal ContentProvider that responds to IPC inquiries from QIEZKA
 * (com.uncode.app) via authority "com.siren.homeproxy.provider".
 *
 * Serves as the target for QIEZKA's [IBinder.DeathRecipient]. If SIREN ever dies,
 * QIEZKA immediately queries this provider to cold-start SIREN back to life.
 */
class SirenKeepAliveProvider : ContentProvider() {

    companion object {
        private const val TAG = "SirenKeepAliveProvider"
    }

    override fun onCreate(): Boolean {
        Log.i(TAG, "SirenKeepAliveProvider initialized.")
        return true
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        if ("ping" == method) {
            val response = Bundle().apply {
                putBoolean("alive", true)
                putString("package", SirenConfig.SIREN_PACKAGE)
            }
            return response
        }
        return super.call(method, arg, extras)
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0
}
