package com.mad.myfitnesstrackingapp.auth

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.mad.myfitnesstrackingapp.util.SecurePrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * AuthViewModel
 * - stores JWT token (if server returns it) into EncryptedSharedPreferences via SecurePrefs
 * - exposes loginSuccess / registerSuccess / isLoading
 * - exposes errorState as Pair(statusCode:Int, message:String) for UI to respond to
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val SERVER_IP = "192.168.0.75"
    private val LOGIN_URL = "http://$SERVER_IP/fitness_api/login.php"
    private val REGISTER_URL = "http://$SERVER_IP/fitness_api/register.php"

    private val _loginSuccess = MutableStateFlow<Boolean>(false)
    val loginSuccess = _loginSuccess.asStateFlow()

    private val _registerSuccess = MutableStateFlow<Boolean>(false)
    val registerSuccess = _registerSuccess.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading = _isLoading.asStateFlow()

    // errorState holds Pair(statusCode, message). statusCode = -1 when not available.
    private val _errorState = MutableStateFlow<Pair<Int, String>?>(null)
    val errorState = _errorState.asStateFlow()

    private val queue = Volley.newRequestQueue(application.applicationContext)

    private fun storeAuthToken(token: String?, userId: Int, username: String) {
        try {
            val prefs = SecurePrefs.getEncryptedSharedPreferences(getApplication())
            with(prefs.edit()) {
                putString("auth_token", token)
                putInt("user_id", userId)
                putString("username", username)
                apply()
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Failed to store token: ${e.message}")
        }
    }

    // clear the error after UI consumed it
    fun clearError() {
        viewModelScope.launch { _errorState.emit(null) }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch { _isLoading.emit(true) }

        // offline demo shortcut
        if (email == "user" && password == "1234") {
            val sharedPreferences = getApplication<Application>().getSharedPreferences("FitnessAppPrefs", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putInt("user_id", -1)
                putString("username", "User")
                apply()
            }
            Toast.makeText(getApplication(), "Offline Login Successful!", Toast.LENGTH_SHORT).show()
            viewModelScope.launch {
                _loginSuccess.emit(true)
                _isLoading.emit(false)
            }
            return
        }

        val stringRequest = object : StringRequest(
            Request.Method.POST, LOGIN_URL,
            { response ->
                try {
                    Log.d("AuthViewModel", "Login Response: $response")
                    val jsonResponse = JSONObject(response)
                    val status = jsonResponse.optString("status", "error")
                    if (status == "success") {
                        val token = jsonResponse.optString("token", null)
                        val userObj = jsonResponse.getJSONObject("user")
                        val userId = userObj.getInt("user_id")
                        val username = userObj.getString("username")
                        storeAuthToken(token, userId, username)

                        Toast.makeText(getApplication(), "Login Successful!", Toast.LENGTH_SHORT).show()
                        viewModelScope.launch { _loginSuccess.emit(true) }
                    } else {
                        val message = jsonResponse.optString("message", "Login failed")
                        // present validation/server message with no http code (-1)
                        viewModelScope.launch { _errorState.emit(Pair(-1, message)) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    viewModelScope.launch { _errorState.emit(Pair(-1, "Error parsing response.")) }
                } finally {
                    viewModelScope.launch { _isLoading.emit(false) }
                }
            },
            { error ->
                val statusCode = error.networkResponse?.statusCode ?: -1
                val body = error.networkResponse?.data?.let { String(it) }
                Log.e("AuthViewModel", "Volley Error: code=$statusCode body=$body msg=${error.message}")

                var userMessage = error.message ?: "Network error"
                if (body != null) {
                    try {
                        val errJson = JSONObject(body)
                        userMessage = errJson.optString("message", userMessage)
                    } catch (_: Exception) { /* not JSON */ }
                }
                viewModelScope.launch {
                    _errorState.emit(Pair(statusCode, userMessage))
                    _isLoading.emit(false)
                }
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["email"] = email
                params["password"] = password
                return params
            }
        }

        stringRequest.retryPolicy = DefaultRetryPolicy(
            10_000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        queue.add(stringRequest)
    }

    fun registerUser(username: String, email: String, password: String) {
        viewModelScope.launch { _isLoading.emit(true) }

        val stringRequest = object : StringRequest(
            Request.Method.POST, REGISTER_URL,
            { response ->
                try {
                    Log.d("AuthViewModel", "Register Response: $response")
                    val jsonResponse = JSONObject(response)
                    val status = jsonResponse.optString("status", "error")

                    if (status == "success") {
                        Toast.makeText(getApplication(), "Registration Successful! Please login.", Toast.LENGTH_LONG).show()
                        viewModelScope.launch { _registerSuccess.emit(true) }
                    } else {
                        val message = jsonResponse.optString("message", "Registration failed")
                        viewModelScope.launch { _errorState.emit(Pair(-1, message)) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    viewModelScope.launch { _errorState.emit(Pair(-1, "Error parsing response.")) }
                } finally {
                    viewModelScope.launch { _isLoading.emit(false) }
                }
            },
            { error ->
                val statusCode = error.networkResponse?.statusCode ?: -1
                val body = error.networkResponse?.data?.let { String(it) }
                Log.e("AuthViewModel", "Volley Error: code=$statusCode body=$body msg=${error.message}")

                var userMessage = error.message ?: "Network error"
                if (body != null) {
                    try {
                        val errJson = JSONObject(body)
                        userMessage = errJson.optString("message", userMessage)
                    } catch (_: Exception) { /* not JSON */ }
                }
                viewModelScope.launch {
                    _errorState.emit(Pair(statusCode, userMessage))
                    _isLoading.emit(false)
                }
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["username"] = username
                params["email"] = email
                params["password"] = password
                return params
            }
        }

        stringRequest.retryPolicy = DefaultRetryPolicy(
            10_000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        queue.add(stringRequest)
    }

    fun resetLoginFlow() {
        viewModelScope.launch {
            _loginSuccess.emit(false)
            _isLoading.emit(false)
        }
    }
    fun resetRegisterFlow() {
        viewModelScope.launch {
            _registerSuccess.emit(false)
            _isLoading.emit(false)
        }
    }
}
