package com.shverma.app.di

import com.shverma.app.data.remote.interceptor.AuthInterceptor
import com.shverma.app.data.remote.openai.OpenAIApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val OPENAI_BASE_URL = "https://api.openai.com/"

    private const val OPENAI_API_KEY = "" // add you key here for testing, in production add this to local.properties and read from there

    @Provides
    @Singleton
    fun provideApiKeyProvider(): () -> String = {
        OPENAI_API_KEY
    }


    @Provides
    @Singleton
    fun provideAuthInterceptor(
        apiKeyProvider: () -> String
    ): AuthInterceptor = AuthInterceptor(apiKeyProvider)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenAIRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(OPENAI_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideOpenAIApi(
        retrofit: Retrofit
    ): OpenAIApiService =
        retrofit.create(OpenAIApiService::class.java)
}
