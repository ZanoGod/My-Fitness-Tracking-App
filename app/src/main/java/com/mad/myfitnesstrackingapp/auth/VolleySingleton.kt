package com.mad.myfitnesstrackingapp.auth

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley


object VolleySingleton {
    @Volatile
    private var instance: RequestQueue? = null

    fun getQueue(context: Context): RequestQueue {
        return instance ?: synchronized(this) {
            instance ?: Volley.newRequestQueue(context.applicationContext).also { instance = it }
        }
    }
}

