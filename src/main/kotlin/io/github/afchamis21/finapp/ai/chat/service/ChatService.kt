package io.github.afchamis21.finapp.ai.chat.service

import io.github.afchamis21.finapp.ai.chat.dto.ChatMessageDTO
import io.github.afchamis21.finapp.ai.chat.repo.ChatContentRepository
import io.github.afchamis21.finapp.ai.chat.request.ChatMessageRequest
import io.github.afchamis21.finapp.ai.tools.DateTools
import io.github.afchamis21.finapp.ai.tools.FinancialAssistantTools
import io.github.afchamis21.finapp.category.model.Category
import io.github.afchamis21.finapp.config.logger
import io.github.afchamis21.finapp.exceptions.HttpException
import io.github.afchamis21.finapp.http.Context
import io.github.afchamis21.finapp.user.model.User
import io.github.afchamis21.finapp.user.service.UserService
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.MessageType
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Year


@Service
class ChatService(
    private val userService: UserService,
    private val repository: ChatContentRepository,
    private val chatClient: ChatClient,
    private val financialAssistantTools: FinancialAssistantTools,
    private val dateTools: DateTools
) {
    private val log = logger()

    fun chat(request: ChatMessageRequest): ChatMessageDTO {
        val user = userService.findCurrentUser()
        log.info("Chat request initiated for user ${user.id}. Message: \"${request.message}\"")

        val history = getHistory(user)
        val categories = repository.findCategoriesByUser(user)

        log.info("Building prompt for user ${user.id} with ${history.size} history messages and ${categories.size} categories.")
        val responseSpec = financialChatClient(user, categories)

        log.info("Calling AI model for user ${user.id}...")
        val response = responseSpec
            .messages(history)
            .user(request.message)
            .call()

        var responseMessage = response.content() ?: run {
            log.error("AI model returned a null response content for user ${user.id}.")
            throw HttpException(HttpStatus.INTERNAL_SERVER_ERROR)
        }
        log.info("Received response from AI model for user ${user.id}.")
        log.debug("Raw AI response for user ${user.id}: \"$responseMessage\"")

        if (!responseMessage.contains("TOOL_RAN")) {
            log.info("No tool execution detected. Saving user and assistant messages to history for user ${user.id}.")
            repository.saveMessages(
                user = user,
                listOf(
                    UserMessage(request.message),
                    AssistantMessage(responseMessage)
                )
            )
        } else {
            log.info("Tool execution detected. Trimming 'TOOL_RAN' marker and skipping history save for user ${user.id}.")
            responseMessage = responseMessage.replace("TOOL_RAN", "").trim()
        }

        log.info("Returning final chat response to user ${user.id}.")
        return ChatMessageDTO(responseMessage, MessageType.ASSISTANT)
    }

    private fun financialChatClient(
        user: User,
        categories: List<Category>
    ): ChatClientRequestSpec {
        log.debug("Building ChatClientRequestSpec for user ${user.id}.")
        val categoryString = categories.joinToString(", ") {
            "(ID: ${it.id}, Label: ${it.label}, Type: ${it.type})"
        }
        log.debug("Category string for prompt: [$categoryString]")

        val currentYear = Year.now().value
        val currentMonth = LocalDate.now().month
        val locale = Context.locale
        log.debug("Prompt context set with: year={}, month={}, locale={}", currentYear, currentMonth, locale)

        return chatClient.prompt()
            .system(
                """
                    You are a helpful financial assistant named Alfred, assisting the user ${user.username}.
                    The current year is ${currentYear}. If the user does not specify a year for a financial entry, you MUST use the current year: ${currentYear}.
                    The current month is ${currentMonth}. If the user does not specify a month for a financial entry, you MUST use the current month: ${currentMonth}.
                    Unless spoken to in another language, your default locale is $locale
                    
                    The user has the following available categories:[${categoryString}].
                    You can only use categories from this list when registering financial entries and MUST use all categories applicable for an entry.
                    
                    If the list of categories is empty, explain that the user must create categories before continuing.
                    
                    IMPORTANT: Do NOT send JSON content directly to the user, only to TOOL INPUTS. Use MARKDOWN for responses to the user
                    
                    IMPORTANT: Before calling any tool or registering any financial entry (like an expense or income),
                    ALWAYS clearly summarize what will be registered, and ask the user for explicit confirmation (e.g., "Do you want to proceed?").
                    
                    Only call the tool AFTER the user responds positively (e.g., "yes", "go ahead", "confirm", etc.).
                    If the user does not confirm or says something ambiguous, do NOT proceed.
                    
                    TOOL INSTRUCTIONS: RegisterFinancialEntry
                    
                    To use the 'RegisterFinancialEntry' tool, you must provide a JSON array as the argument.
                    
                    Each object inside this array represents a single financial entry and should contain the details for that entry. Each object must have the following keys:
                    
                    value: The numeric value of the entry.
                    label: A string describing the entry.
                    date: The date in YYYY-MM-DD format.
                    categories: An array of numeric category IDs.
                    CORRECT TOOL INPUT FORMAT EXAMPLE:
                    
                    JSON
                    ```
                    [
                      {
                        "value": 150.00,
                        "label": "Groceries from supermarket",
                        "date": "${currentYear}-06-10",
                        "categories": [1, 5]
                      },
                      {
                        "value": 25.50,
                        "label": "Coffee with friend",
                        "date": "${currentYear}-06-09",
                        "categories": [3]
                      }
                    ]
                    ```
                    
                    If you detect derived entries such as taxes or extra charges, include them in the summary before asking for confirmation.
                    Your goal is to ensure the user always understands and approves what will be saved.
                    
                    If a tool has been executed, always add this EXACT SAME string to the END of your response: "TOOL_RAN"
                    END IMPORTANT
                    """.trimIndent()
            )
            .tools(
                financialAssistantTools,
                dateTools
            )
    }

    fun getHistory(user: User = userService.findCurrentUser()): MutableList<Message> {
        log.debug("Fetching chat history for user ${user.id}.")
        val messages = repository.findMessagesByUser(user).toMutableList()
        log.debug("Found ${messages.size} history messages for user ${user.id}.")
        return messages
    }

    fun resetChat(user: User = userService.findCurrentUser()) {
        log.info("Resetting chat history for user ${user.id}. All messages will be deleted.")
        repository.deleteByUser(user)
        log.info("Chat history for user ${user.id} has been successfully deleted.")
    }

    fun fetchChat(): List<ChatMessageDTO> {
        val userId = Context.userId ?: throw HttpException(HttpStatus.FORBIDDEN)
        log.debug("Fetching chat DTOs for user $userId.")
        val messages = repository.findMessagesByUser(userId)
        log.debug("Found ${messages.size} messages to map to DTOs for user $userId.")
        return messages.map { ChatMessageDTO(it.text, it.messageType) }
    }
}