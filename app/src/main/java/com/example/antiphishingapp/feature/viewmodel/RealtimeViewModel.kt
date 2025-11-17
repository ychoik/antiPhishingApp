package com.example.antiphishingapp.feature.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.antiphishingapp.feature.model.RealtimeMessage
import com.example.antiphishingapp.feature.repository.RealtimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest

class RealtimeViewModel : ViewModel() {

    private val repository = RealtimeRepository()

    // UI에서 관찰할 최신 서버 메시지
    private val _latestMessage = MutableStateFlow<RealtimeMessage?>(null)
    val latestMessage: StateFlow<RealtimeMessage?> = _latestMessage

    // 세션 시작: WebSocket 연결 + 메시지 수집 시작
    fun startSession() {
        // 1) WebSocket 연결
        repository.connect()

        // 2) 서버 메시지 스트림 수집
        viewModelScope.launch(Dispatchers.IO) {
            repository.incomingMessages.collectLatest { msg ->
                _latestMessage.value = msg
            }
        }
    }

    // PCM chunk 전송 (바이너리)
    fun sendPcmChunk(chunk: okio.ByteString) {
        repository.sendPcm(chunk)
    }

    fun stopSession() {
        repository.disconnect()
    }

    override fun onCleared() {
        super.onCleared()
        repository.disconnect()
    }

    // UI에서 알림 카드 닫을 때 메시지 초기화용
    fun clear() {
        _latestMessage.value = null
    }
}