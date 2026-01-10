package com.shverma.app.data.remote.openai

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Retrofit API interface for OpenAI
 */
interface OpenAIApiService {

    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun streamChat(
        @Body request: OpenAIChatRequest
    ): Response<ResponseBody>

}