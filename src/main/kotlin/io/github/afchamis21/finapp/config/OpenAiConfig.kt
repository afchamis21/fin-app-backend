package io.github.afchamis21.finapp.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenAiConfig {

    @Bean
    fun chatClient(builder: ChatClient.Builder): ChatClient {
        return builder.build()
    }
}