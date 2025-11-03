package com.example.antiphishingapp.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "https://test-nas3.onrender.com/"  // ✅ 서버 기본 URL

    // ✅ OkHttpClient 설정
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // 연결 타임아웃
            .readTimeout(30, TimeUnit.SECONDS)    // 읽기 타임아웃
            .writeTimeout(30, TimeUnit.SECONDS)   // 쓰기 타임아웃
            .build()
    }

    // ✅ Retrofit 인스턴스 생성
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ✅ ApiService 인스턴스 생성 (앱 어디서든 호출 가능)
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
