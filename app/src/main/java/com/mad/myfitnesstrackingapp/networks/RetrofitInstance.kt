package com.mad.myfitnesstrackingapp.networks



import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // IMPORTANT:
    // This is the URL for your XAMPP server as seen from the Android Emulator.
    // DO NOT use "localhost", use "10.0.2.2" instead.
    // "fitness_app" is the folder you created in htdocs.
    private const val BASE_URL = "http://10.0.2.2/fitness_app/"

    // Create a lazy-initialized Retrofit instance
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // Add Gson converter to parse JSON
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
