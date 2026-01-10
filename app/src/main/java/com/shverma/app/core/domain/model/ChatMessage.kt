package com.shverma.app.core.domain.model

import com.shverma.app.core.domain.model.Role

data class ChatMessage(
    val id: String,
    val role: Role,
    val content: String,
    val timestamp: Long
)
