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
import com.mad.myfitnesstrackingapp.networks.ApiConfig
import com.mad.myfitnesstrackingapp.util.SecurePrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val LOGIN_URL = ApiConfig.LOGIN_URL
    private val REGISTER_URL = ApiConfig.REGISTER_URL

    private val _loginSuccess = MutableStateFlow<Boolean>(false)
    val loginSuccess = _loginSuccess.asStateFlow()

    private val _registerSuccess = MutableStateFlow<Boolean>(false)
    val registerSuccess = _registerSuccess.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading = _isLoading.asStateFlow()


    private val _errorState = MutableStateFlow<Pair<Int, String>?>(null)
    val errorState = _errorState.asStateFlow()


    private val _email = MutableStateFlow<String?>(null)
    val email = _email.asStateFlow()

    private val _createdAt = MutableStateFlow<String?>(null)
    val createdAt = _createdAt.asStateFlow()


    private val _username = MutableStateFlow<String>("")
    val username = _username.asStateFlow()

    private val queue = VolleySingleton.getQueue(application.applicationContext)
    private val REQUEST_TAG = "AuthRequests"

    init {

        val prefs = application.getSharedPreferences("FitnessAppPrefs", Context.MODE_PRIVATE)
        _username.value = prefs.getString("username", "") ?: ""
        _email.value = prefs.getString("email", null)
        _createdAt.value = prefs.getString("created_at", null)
    }

    private fun storeAuthToken(token: String?, userId: Int, username: String, emailStr: String?, createdAtStr: String?) {
        try {
            // Store sensitive token in EncryptedSharedPreferences
            val encPrefs = SecurePrefs.getEncryptedSharedPreferences(getApplication())
            with(encPrefs.edit()) {
                putString("auth_token", token)
                putInt("user_id", userId)
                putString("username", username)
                putString("email", emailStr)
                putString("created_at", createdAtStr)
                apply()
            }

            // Also persist lightweight values in regular SharedPreferences for other parts of app
            val plainPrefs = getApplication<Application>().getSharedPreferences("FitnessAppPrefs", Context.MODE_PRIVATE)
            with(plainPrefs.edit()) {
                putInt("user_id", userId)
                putString("username", username)
                putString("email", emailStr)
                putString("created_at", createdAtStr)
                apply()
            }

            // Update flows (UI)
            _username.value = username
            _email.value = emailStr
            _createdAt.value = createdAtStr
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
                putString("email", "user@local")
                putString("created_at", "2024-01-01 00:00:00")
                apply()
            }
            // update flows
            _username.value = "User"
            _email.value = "user@local"
            _createdAt.value = "2024-01-01 00:00:00"

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
                        val emailResp = userObj.optString("email", null)
                        val createdAtResp = userObj.optString("created_at", null)
                        storeAuthToken(token, userId, username, emailResp, createdAtResp)

                        Toast.makeText(getApplication(), "Login Successful!", Toast.LENGTH_SHORT).show()
                        viewModelScope.launch { _loginSuccess.emit(true) }
                    } else {
                        val message = jsonResponse.optString("message", "Login failed")
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

        stringRequest.tag = REQUEST_TAG
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

        stringRequest.tag = REQUEST_TAG
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

    fun logout() {
        // clear encrypted + plain prefs
        try {
            val enc = SecurePrefs.getEncryptedSharedPreferences(getApplication())
            with(enc.edit()) {
                remove("auth_token")
                remove("user_id")
                remove("username")
                remove("email")
                remove("created_at")
                apply()
            }
        } catch (_: Exception) { }

        val plain = getApplication<Application>().getSharedPreferences("FitnessAppPrefs", Context.MODE_PRIVATE)
        with(plain.edit()) {
            clear()
            apply()
        }

        // reset flows
        _username.value = ""
        _email.value = null
        _createdAt.value = null
        viewModelScope.launch {
            _loginSuccess.emit(false)
            _isLoading.emit(false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        queue.cancelAll(REQUEST_TAG)
    }
}
