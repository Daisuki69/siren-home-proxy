package com.siren.homeproxy

import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log

/**
 * SirenRoleActivity
 *
 * Transparent, zero-UI helper activity that requests [RoleManager.ROLE_ASSISTANT]
 * on behalf of SIREN when triggered by QIEZKA via [SirenConfig.ACTION_REQUEST_ASSISTANT].
 */
class SirenRoleActivity : Activity() {

    companion object {
        private const val TAG = "SirenRoleActivity"
        private const val REQUEST_CODE_ROLE_ASSISTANT = 2001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setWindowAnimations(0)
        window.setBackgroundDrawable(null)
        super.onCreate(savedInstanceState)
        requestAssistantRole()
    }

    private fun requestAssistantRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_ASSISTANT)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_ASSISTANT)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_ASSISTANT)
                    startActivityForResult(intent, REQUEST_CODE_ROLE_ASSISTANT)
                    return
                } else {
                    Log.i(TAG, "SIREN already holds ROLE_ASSISTANT.")
                    finish()
                    return
                }
            }
        }

        // Fallback for older Android releases or if RoleManager unavailable
        try {
            val fallback = Intent(Settings.ACTION_VOICE_INPUT_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(fallback)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch voice input settings: ${e.message}")
        }
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ROLE_ASSISTANT) {
            Log.i(TAG, "Role request finished with result: $resultCode")
            finish()
        }
    }
}
