package com.shverma.app.data.repository

import com.shverma.app.core.domain.llm.ChatClient
import com.shverma.app.core.domain.model.ChatChunk
import com.shverma.app.core.domain.model.ChatRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ChatRepository @Inject constructor(
    private val chatClient: ChatClient
) {

    fun streamChat(
        request: ChatRequest
    ): Flow<ChatChunk> {
        return chatClient.streamChat(request)
    }
}
