package com.mad.myfitnesstrackingapp.auth


import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val SERVER_IP = "10.0.2.2"
    private val LOGIN_URL = "http://$SERVER_IP/fitness_api/login.php"
    private val REGISTER_URL = "http://$SERVER_IP/fitness_api/register.php"

    // These StateFlows will notify the UI on success
    private val _loginSuccess = MutableStateFlow<Boolean>(false)
    val loginSuccess = _loginSuccess.asStateFlow()

    private val _registerSuccess = MutableStateFlow<Boolean>(false)
    val registerSuccess = _registerSuccess.asStateFlow()

    private val queue = Volley.newRequestQueue(application.applicationContext)

    fun loginUser(email: String, password: String) {
        val stringRequest = object : StringRequest(
            Request.Method.POST, LOGIN_URL,
            { response ->
                try {
                    Log.d("AuthViewModel", "Login Response: $response")
                    val jsonResponse = JSONObject(response)
                    val status = jsonResponse.getString("status")
                    if (status == "success") {
                        Toast.makeText(getApplication(), "Login Successful!", Toast.LENGTH_SHORT).show()
                        // Notify the UI that login was successful
                        viewModelScope.launch {
                            _loginSuccess.emit(true)
                        }
                    } else {
                        val message = jsonResponse.getString("message")
                        Toast.makeText(getApplication(), "Login Failed: $message", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(getApplication(), "Error parsing response.", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("AuthViewModel", "Volley Error: ${error.message}")
                Toast.makeText(getApplication(), "Network Error: ${error.message}", Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["email"] = email
                params["password"] = password
                return params
            }
        }
        queue.add(stringRequest)
    }

    fun registerUser(username: String, email: String, password: String) {
        val stringRequest = object : StringRequest(
            Request.Method.POST, REGISTER_URL,
            { response ->
                try {
                    Log.d("AuthViewModel", "Register Response: $response")
                    val jsonResponse = JSONObject(response)
                    val status = jsonResponse.getString("status")

                    if (status == "success") {
                        Toast.makeText(getApplication(), "Registration Successful! Please login.", Toast.LENGTH_LONG).show()
                        // Notify the UI that registration was successful
                        viewModelScope.launch {
                            _registerSuccess.emit(true)
                        }
                    } else {
                        val message = jsonResponse.getString("message")
                        Toast.makeText(getApplication(), "Registration Failed: $message", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(getApplication(), "Error parsing response.", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("AuthViewModel", "Volley Error: ${error.message}")
                Toast.makeText(getApplication(), "Network Error: ${error.message}", Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["username"] = username
                params["email"] = email
                params["password"] = password
                return params
            }
        }
        queue.add(stringRequest)
    }

    // Call this to reset the flow after navigation
    fun resetLoginFlow() {
        viewModelScope.launch {
            _loginSuccess.emit(false)
        }
    }
    fun resetRegisterFlow() {
        viewModelScope.launch {
            _registerSuccess.emit(false)
        }
    }
}
