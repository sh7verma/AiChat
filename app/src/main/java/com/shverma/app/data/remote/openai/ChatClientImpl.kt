package com.shverma.app.data.remote.openai

import com.shverma.app.core.domain.llm.ChatClient
import com.shverma.app.core.domain.model.ChatChunk
import com.shverma.app.core.domain.model.ChatRequest
import com.shverma.app.core.streaming.SseParser
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import okhttp3.ResponseBody
import okio.BufferedSource
import javax.inject.Inject

class ChatClientImpl @Inject constructor(
    val api: OpenAIApiService
) : ChatClient {

    private val parser = SseParser()

    override fun streamChat(request: ChatRequest): Flow<ChatChunk> = flow {

        val payload = OpenAIChatRequest.fromDomain(request)

        val response = api.streamChat(payload)

        if (!response.isSuccessful) {
            throw RuntimeException("HTTP ${response.code()}")
        }

        val body: ResponseBody =
            response.body() ?: throw RuntimeException("Empty response")

        val source: BufferedSource = body.source()

        while (currentCoroutineContext().isActive && !source.exhausted()) {
            val line = source.readUtf8Line() ?: continue
            val chunk = parser.parseLine(line)
            if (chunk != null) {
                emit(chunk)
                if (chunk.isFinished) return@flow
            }
        }
    }
}
