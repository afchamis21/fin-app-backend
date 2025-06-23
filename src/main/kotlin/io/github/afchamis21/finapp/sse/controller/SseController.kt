package io.github.afchamis21.finapp.sse.controller

import io.github.afchamis21.finapp.auth.types.OneTimeCodeAuth
import io.github.afchamis21.finapp.http.response.Response
import io.github.afchamis21.finapp.sse.service.SseService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/sse")
class SseController(private val sseService: SseService) {

    @OneTimeCodeAuth
    @PostMapping("/register")
    fun register(): Response<SseEmitter> {
        return Response.ok(sseService.register())
    }
}
