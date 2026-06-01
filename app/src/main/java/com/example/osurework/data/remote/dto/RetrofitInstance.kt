package com.example.osurework.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "https://osu.ppy.sh/api/v2/"
    private const val AUTH_URL = "https://osu.ppy.sh/"

    // Twoje dane z osu! OAuth - wpisz tutaj
    const val CLIENT_ID = 
    const val CLIENT_SECRET = ""

    val authService: OsuAuthService by lazy {
        Retrofit.Builder()
            .baseUrl(AUTH_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OsuAuthService::class.java)
    }

    val apiService: OsuApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OsuApiService::class.java)
    }
}
