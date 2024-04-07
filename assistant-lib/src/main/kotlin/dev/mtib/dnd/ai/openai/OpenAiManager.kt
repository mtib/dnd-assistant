package dev.mtib.dnd.ai.openai

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatResponseFormat
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.completion.CompletionRequest
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import dev.mtib.dnd.ai.storage.config.Configuration
import dev.mtib.dnd.ai.storage.config.Configuration.DndConfigRc.OpenAiModel
import dev.mtib.dnd.ai.storage.config.Configuration.DndConfigRc.OpenAiToken
import java.io.Closeable
import kotlin.time.Duration.Companion.seconds

class OpenAiManager private constructor(
    private val token: OpenAiToken,
    private val model: OpenAiModel,
) : Closeable {
    private val client by lazy {
        OpenAI(
            token.token,
            timeout = Timeout(socket = 60.seconds),
            logging = LoggingConfig(logLevel = LogLevel.None)
        )
    }
    companion object {
        fun fromConfiguration(config: Configuration.DndConfigRc) = OpenAiManager(
            token = config.openAiToken,
            model = config.openAiModel,
        )
    }

    private fun List<String>.toCompletionRequest(): ChatCompletionRequest = ChatCompletionRequest(
        model = ModelId(model.model),
        messages = this.map {
            ChatMessage(
                role = ChatRole.User,
                content = it,
            )
        },
        n = 1,
        user = "dnd-assistant-cli",
        maxTokens = 1024,
    )

    private fun List<String>.toJsonCompletionRequest(): ChatCompletionRequest = this.toCompletionRequest().let {
        ChatCompletionRequest(
            model = it.model,
            messages = it.messages,
            n = it.n,
            user = it.user,
            maxTokens = it.maxTokens,
            responseFormat = ChatResponseFormat.JsonObject,
        )
    }

    suspend fun simpleResponse(prompt: String): String {
        return client.chatCompletion(listOf(prompt).toCompletionRequest()).choices.first().message.content!!
    }

    override fun close() {
        client.close()
    }
}