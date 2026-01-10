package com.shverma.app.core.streaming

import com.google.gson.Gson
import com.shverma.app.core.domain.model.ChatChunk
import com.shverma.app.data.remote.openai.OpenAIStreamResponse

class SseParser {

    private val gson = Gson()

    fun parseLine(line: String): ChatChunk? {
        val trimmed = line.trim()

        // Ignore non-data lines
        if (!trimmed.startsWith("data:")) return null

        val data = trimmed.removePrefix("data:").trim()

        // Stream finished
        if (data == "[DONE]") {
            return ChatChunk(isFinished = true)
        }

        return try {
            val response =
                gson.fromJson(data, OpenAIStreamResponse::class.java)

            val delta =
                response.choices.firstOrNull()
                    ?.delta
                    ?.content

            if (delta.isNullOrEmpty()) null
            else ChatChunk(delta = delta)

        } catch (e: Exception) {
            null // Never crash on malformed chunk
        }
    }
}
