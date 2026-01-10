package com.shverma.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shverma.app.core.domain.model.ChatMessage
import com.shverma.app.core.domain.model.ChatRequest
import com.shverma.app.core.domain.model.Role
import com.shverma.app.data.repository.ChatRepository
import com.shverma.app.utils.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject


data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val streamingText: String = "",
    val isStreaming: Boolean = false,
    val error: String? = null,
    val streamingSessionId: String = ""
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    val uiEvent = Channel<UiEvent>()
    private var streamingJob: Job? = null

    fun sendMessage(
        text: String,
        temperature: Float = 0.7f,
        maxTokens: Int = 512
    ) {
        if (text.isBlank()) return
        streamingJob?.cancel()

        val sessionId = UUID.randomUUID().toString()

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = Role.USER,
            content = text,
            timestamp = System.currentTimeMillis()
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                streamingText = "",
                isStreaming = true,
                error = null,
                streamingSessionId = sessionId
            )
        }

        val request = ChatRequest(
            messages = _uiState.value.messages,
            temperature = temperature,
            maxTokens = maxTokens
        )

        streamingJob = viewModelScope.launch {
            try {
                chatRepository.streamChat(request).collect { chunk ->
                    if (chunk.isFinished) {
                        _uiState.update { it.copy(isStreaming = false) }
                    } else {
                        _uiState.update {
                            it.copy(streamingText = it.streamingText + chunk.delta)
                        }
                    }
                }
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(isStreaming = false, error = t.message)
                }
            }
        }
    }

    fun cancelStreaming() {
        streamingJob?.cancel()
        _uiState.update { it.copy(isStreaming = false) }
    }

    fun commitAssistantMessage() {
        val text = _uiState.value.streamingText
        if (text.isBlank()) return

        val assistant = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = Role.ASSISTANT,
            content = text,
            timestamp = System.currentTimeMillis()
        )

        _uiState.update {
            it.copy(
                messages = it.messages + assistant,
                streamingText = ""
            )
        }
    }
}
