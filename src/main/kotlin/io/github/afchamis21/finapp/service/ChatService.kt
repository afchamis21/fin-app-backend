package io.github.afchamis21.finapp.service

import io.github.afchamis21.finapp.ai.DateTools
import io.github.afchamis21.finapp.ai.FinancialAssistantTools
import io.github.afchamis21.finapp.context.Context
import io.github.afchamis21.finapp.domain.user.User
import io.github.afchamis21.finapp.exceptions.HttpException
import io.github.afchamis21.finapp.http.dto.ChatMessageDTO
import io.github.afchamis21.finapp.http.request.chat.ChatRequest
import io.github.afchamis21.finapp.repo.ChatContentRepository
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.MessageType
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.Year

@Service
class ChatService(
    private val chatClient: ChatClient,
    private val repository: ChatContentRepository,
    private val userService: UserService,
    private val dateTools: DateTools,
    private val financialAssistantTools: FinancialAssistantTools
) {
    fun chat(request: ChatRequest): ChatMessageDTO {
        val user = userService.findCurrentUser()
        val history = getHistory(user)

        val categories = repository.findCategoriesByUser(user)
        val categoryString = categories.joinToString(", ") {
            "(ID: ${it.id}, Label: ${it.label}, Type: ${it.type})"
        }
        val currentYear = Year.now().value

        val response = chatClient.prompt()
            .system(
                """
                You are a helpful financial assistant named Alfred, assisting the user ${user.username}.
                The current year is ${currentYear}. If the user does not specify a year for a financial entry, you MUST use the current year: ${currentYear}.
                
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
            .messages(history)
            .user(request.message)
            .call()

        var responseMessage = response.content() ?: throw HttpException(HttpStatus.INTERNAL_SERVER_ERROR)

        if (!responseMessage.contains("TOOL_RAN")) {
            repository.saveMessages(
                user = user,
                listOf(
                    UserMessage(request.message),
                    AssistantMessage(responseMessage)
                )
            )
        } else {
            responseMessage = responseMessage.replace("TOOL_RAN", "").trim()
        }

        return ChatMessageDTO(responseMessage, MessageType.ASSISTANT)
    }

    fun getHistory(user: User = userService.findCurrentUser()): MutableList<Message> {
        return repository.findMessagesByUser(user).toMutableList()
    }

    fun resetChat(user: User = userService.findCurrentUser()) {
        repository.deleteByUser(user)
    }

    fun fetchChat(): List<ChatMessageDTO> {
        val userId = Context.userId ?: throw HttpException(HttpStatus.FORBIDDEN)

        val messages = repository.findMessagesByUser(userId)

        return messages.map { ChatMessageDTO(it.text, it.messageType) }
    }
}