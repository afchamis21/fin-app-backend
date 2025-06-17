package io.github.afchamis21.finapp.controller

import io.github.afchamis21.finapp.http.dto.ChatMessageDTO
import io.github.afchamis21.finapp.http.request.chat.ChatRequest
import io.github.afchamis21.finapp.http.response.Response
import io.github.afchamis21.finapp.service.ChatService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/chat")
class ChatController(val chatService: ChatService) {

    @PostMapping("")
    fun ask(@RequestBody request: ChatRequest): Response<ChatMessageDTO> {
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