package com.shverma.app.di

import com.shverma.app.core.domain.llm.ChatClient
import com.shverma.app.data.remote.openai.ChatClientImpl
import com.shverma.app.data.remote.openai.OpenAIApiService
import com.shverma.app.data.repository.ChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideChatClient(
        apiService: OpenAIApiService
    ): ChatClient = ChatClientImpl(apiService)

    @Provides
    @Singleton
    fun provideChatRepository(
        chatClient: ChatClient
    ): ChatRepository = ChatRepository(chatClient)
}
