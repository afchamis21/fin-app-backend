package io.github.afchamis21.finapp.ai.chat.controller

import io.github.afchamis21.finapp.ai.chat.dto.ChatMessageDTO
import io.github.afchamis21.finapp.ai.chat.request.ChatMessageRequest
import io.github.afchamis21.finapp.ai.chat.service.ChatService
import io.github.afchamis21.finapp.http.response.Response
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/chat")
class ChatController(val chatService: ChatService) {

    @PostMapping("")
    fun ask(@RequestBody request: ChatMessageRequest): Response<ChatMessageDTO> {
        return Response.ok(chatService.chat(request))
    }

    @GetMapping("")
    fun history(): Response<List<ChatMessageDTO>> {
        return Response.ok(chatService.fetchChat())
    }

    @DeleteMapping("")
    fun reset(): Response<Unit> {
        return Response.ok(chatService.resetChat())
    }
}