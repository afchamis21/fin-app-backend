package io.github.afchamis21.finapp.ai

import io.github.afchamis21.finapp.config.logger
import io.github.afchamis21.finapp.context.Context
import io.github.afchamis21.finapp.exceptions.HttpException
import io.github.afchamis21.finapp.http.request.entry.CreateEntryRequest
import io.github.afchamis21.finapp.repo.ChatContentRepository
import io.github.afchamis21.finapp.service.EntryService
import org.springframework.ai.tool.annotation.Tool
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class FinancialAssistantTools(
    private val entryService: EntryService,
    private val chatContentRepository: ChatContentRepository
) {
    private val log = logger()

    @Tool(
        name = "RegisterFinancialEntry",
        description = "Register an expense or gain financial entry given the necessary inputs"
    )
    fun addEntry(data: List<CreateEntryRequest.CreateEntryData>) {
        try {
            log.info("Creating entries from tool... Data [${data}]")

            entryService.create(CreateEntryRequest(data))

            chatContentRepository.deleteByUser(Context.userId ?: throw HttpException(HttpStatus.FORBIDDEN))
        } catch (e: Exception) {
            log.error("Error creating entries from tool!", e)
        }
    }

    @Tool(
        name = "RefreshCategories",
        description = "Re-loads all the categories used for registering new entries. Should be used if the user has created a new category during the chat conversation"
    )
    fun refreshCategories() {
        log.info("Refreshing categories from tool...")

        chatContentRepository.loadCategories(Context.userId ?: throw HttpException(HttpStatus.FORBIDDEN))
    }
}