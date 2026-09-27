package com.example.healthmonitor.data.network

import android.content.Context
import com.example.healthmonitor.data.auth.AuthManager
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    private const val BASE_URL = "http://192.168.1.175:8000/"

    fun init(appContext: Context) {
        AuthManager.init(appContext)
    }

    val api: HealthApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val request = chain.request()
            val path = request.url.encodedPath

            if (path.contains("/api/patients/login/") ||
                path.contains("/api/patients/register/")) {
                return@Interceptor chain.proceed(request)
            }

            val token = AuthManager.getInstance().getAuthHeader()
            val newRequest = if (token != null) {
                request.newBuilder()
                    .addHeader("Authorization", token)
                    .build()
            } else {
                request
            }

            chain.proceed(newRequest)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val gson = GsonBuilder().setLenient().create()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(HealthApi::class.java)
    }
}