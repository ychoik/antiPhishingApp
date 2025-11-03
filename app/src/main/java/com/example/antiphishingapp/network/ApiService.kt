package com.example.antiphishingapp.network

import com.example.antiphishingapp.feature.model.AnalysisResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // ✅ 서버 상태 확인 (GET /healthz)
    @GET("healthz")
    fun checkHealth(): Call<String>

    // ✅ 단일 이미지 업로드 (POST /upload-image)
    @Multipart
    @POST("upload-image")
    fun uploadImage(
        @Part file: MultipartBody.Part
    ): Call<ResponseBody>

    // ✅ 여러 이미지 업로드 (POST /upload-images)
    @Multipart
    @POST("upload-images")
    fun uploadMultipleImages(
        @Part files: List<MultipartBody.Part>
    ): Call<ResponseBody>

    // ✅ 음성 파일 업로드 (POST /api/transcribe/upload)
    @Multipart
    @POST("api/transcribe/upload")
    fun uploadAudioFile(
        @Part file: MultipartBody.Part
    ): Call<ResponseBody>

    // ✅ 음성 변환 상태 조회 (GET /api/transcribe/status/{token})
    @GET("api/transcribe/status/{token}")
    fun getTranscribeStatus(
        @Path("token") token: String
    ): Call<ResponseBody>

    // ✅ 문서 분석 (POST /process-request)
    @Multipart
    @POST("process-request")
    fun processRequest(
        @Part file: MultipartBody.Part
    ): Call<AnalysisResponse> // ✅ 여기만 변경됨
}
