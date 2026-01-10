package com.shverma.app.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

/**
 * OkHttp interceptor that adds an Authorization header to requests
 */
class AuthInterceptor(
    private val apiKeyProvider: () -> String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val apiKey = apiKeyProvider()
        if (apiKey.isBlank()) {
            throw IOException("OpenAI API key is missing. Please add your API key to the app.")
        }
        val request = chain.request()
            .newBuilder()
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        return chain.proceed(request)
    }
}
