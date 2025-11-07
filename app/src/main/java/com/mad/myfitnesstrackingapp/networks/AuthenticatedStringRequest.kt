package com.mad.myfitnesstrackingapp.networks

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.mad.myfitnesstrackingapp.util.SecurePrefs

/**
 * A StringRequest which automatically attaches the Bearer token from EncryptedSharedPreferences.
 * It also sets a default retry policy (10s), but you can override that after instantiation.
 */


class AuthenticatedStringRequest(
    private val context: Context,
    method: Int,
    url: String,
    listener: Response.Listener<String>,
    errorListener: Response.ErrorListener
) : StringRequest(method, url, listener, errorListener) {

    init {
        // Default retry policy - 10s timeout
        retryPolicy = DefaultRetryPolicy(
            10_000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
    }

    override fun getHeaders(): MutableMap<String, String> {
        val headers = HashMap<String, String>()
        try {
            val prefs = SecurePrefs.getEncryptedSharedPreferences(context)
            val token = prefs.getString("auth_token", null)
            if (!token.isNullOrBlank()) {
                headers["Authorization"] = "Bearer $token"
            }
        } catch (e: Exception) {
            Log.e("AuthReq", "Failed to read token: ${e.message}")
        }

        headers["Accept"] = "application/json"
        return headers
    }
}
