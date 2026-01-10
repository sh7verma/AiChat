package com.shverma.app.data.remote.interceptor

import com.shverma.aichat.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * OkHttp interceptor that adds an Authorization header to requests
 */
class AuthInterceptor(
    private val apiKeyProvider: () -> String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
//        require(BuildConfig.OPENAI_API_KEY.isNotBlank()) {
//            "OpenAI API key missing. Check local.properties and build.gradle."
//        }
        val request = chain.request()
            .newBuilder()
            .addHeader("Authorization", "Bearer ${apiKeyProvider()}")
            .build()

        return chain.proceed(request)
    }
}
