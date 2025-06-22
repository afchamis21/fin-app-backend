package io.github.afchamis21.finapp.ai.tools

import io.github.afchamis21.finapp.ai.chat.repo.ChatContentRepository
import io.github.afchamis21.finapp.config.logger
import io.github.afchamis21.finapp.entry.request.CreateEntryRequest
import io.github.afchamis21.finapp.entry.service.EntryService
import io.github.afchamis21.finapp.exceptions.HttpException
import io.github.afchamis21.finapp.http.Context
import io.github.afchamis21.finapp.sse.model.SSE_EVENTS
import io.github.afchamis21.finapp.sse.service.SseService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.ai.tool.annotation.Tool
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class FinancialAssistantTools(
    private val entryService: EntryService,
    private val chatContentRepository: ChatContentRepository,
    private val applicationScope: CoroutineScope,
    private val sseService: SseService
) {
    private val log = logger()

    @Tool(
        name = "RegisterFinancialEntry",
        description = "Register an expense or gain financial entry given the necessary inputs"
    )
    fun addEntry(data: List<CreateEntryRequest.CreateEntryData>) {
        val userId = Context.userId ?: throw HttpException(HttpStatus.FORBIDDEN)
        log.info("Tool 'RegisterFinancialEntry' invoked for user $userId to create ${data.size} entries.")

        try {
            log.debug("Raw entry data for user $userId: {}", data)

            val entries = entryService.create(CreateEntryRequest(data))
            log.info("Successfully created ${entries.size} entries for user $userId. New IDs: ${entries.map { it.id }}")
            log.info("Dispatching background SSE notification for new entries for user $userId.")
            applicationScope.launch {
                sseService.send(
                    userId = userId,
                    name = SSE_EVENTS.NEW_ENTRIES,
                    data = entries
                )

                log.info("Background SSE event 'newEntries' sent successfully for user $userId.")
            }
        } catch (e: Exception) {
            log.error("Error creating entries from tool for user $userId!", e)
        }
    }
}