package com.example.antiphishingapp.feature.model

data class VoiceUiResult(
    val riskScore: Int,
    val suspiciousItems: List<SuspiciousItem>,
    val transcript: String
)
