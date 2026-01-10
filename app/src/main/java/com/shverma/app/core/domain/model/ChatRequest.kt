package com.shverma.app.core.domain.model

data class ChatRequest(
    val messages: List<ChatMessage>,
    val temperature: Float = 0.7f,
    val maxTokens: Int = 512
)