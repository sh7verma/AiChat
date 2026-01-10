package com.shverma.app.data.remote.openai

import com.google.gson.annotations.SerializedName
import com.shverma.app.core.domain.model.ChatRequest
import com.shverma.app.core.domain.model.Role

/**
 * OpenAI chat completion request payload
 */
data class OpenAIChatRequest(
    val model: String,
    val messages: List<OpenAIMessage>,
    val temperature: Float,
    @SerializedName("max_tokens")
    val maxTokens: Int,
    val stream: Boolean = true
) {
    companion object {

        /**
         * Maps domain ChatRequest → OpenAI network request
         */
        fun fromDomain(
            request: ChatRequest,
            model: String = "gpt-4o-mini"
        ): OpenAIChatRequest {
            return OpenAIChatRequest(
                model = model,
                messages = request.messages.map {
                    OpenAIMessage(
                        role = when (it.role) {
                            Role.SYSTEM -> "system"
                            Role.USER -> "user"
                            Role.ASSISTANT -> "assistant"
                        },
                        content = it.content
                    )
                },
                temperature = request.temperature,
                maxTokens = request.maxTokens,
                stream = true
            )
        }
    }
}

/**
 * OpenAI message object
 */
data class OpenAIMessage(
    val role: String,
    val content: String
)



/**
 * Streaming SSE event example:
 * data: {"choices":[{"delta":{"content":"Hel"}}]}
 * data: [DONE]
 */
data class OpenAIStreamResponse(
    val choices: List<Choice> = emptyList()
) {

    data class Choice(
        val delta: Delta = Delta()
    )

    data class Delta(
        val content: String? = null
    )
}