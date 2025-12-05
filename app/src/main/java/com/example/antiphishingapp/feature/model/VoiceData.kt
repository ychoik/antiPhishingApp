package com.example.antiphishingapp.feature.model

data class VoiceAnalysisResponse(
    val transcription: Transcription,
    val phishing_analysis: PhishingAnalysis
)

data class Transcription(
    val text: String,
    val confidence: Double,
    val speaker: String?,
    val stt_result: SttResult
)

data class SttResult(
    val result: String,
    val message: String,
    val token: String,
    val progress: Int
)

data class PhishingAnalysis(
    val immediate: Immediate,
    val comprehensive: Comprehensive,
    val warning_message: String?
)

data class Immediate(
    val level: Int,
    val probability: Double,
    val phishing_type: String?,
    val keywords: List<String>,
    val method: String
)

data class Comprehensive(
    val is_phishing: Boolean,
    val confidence: Double,
    val method: String,
    val analyzed_length: Int
)
