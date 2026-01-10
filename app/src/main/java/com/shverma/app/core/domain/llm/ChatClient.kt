package com.shverma.app.core.domain.llm

import com.shverma.app.core.domain.model.ChatChunk
import com.shverma.app.core.domain.model.ChatRequest
import kotlinx.coroutines.flow.Flow

interface ChatClient {
    fun streamChat(request: ChatRequest): Flow<ChatChunk>
}