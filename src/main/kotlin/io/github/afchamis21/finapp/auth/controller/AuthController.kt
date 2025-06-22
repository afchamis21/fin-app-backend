package io.github.afchamis21.finapp.auth.controller

import io.github.afchamis21.finapp.auth.dto.LoginDTO
import io.github.afchamis21.finapp.auth.request.LoginRequest
import io.github.afchamis21.finapp.auth.request.RefreshRequest
import io.github.afchamis21.finapp.auth.service.AuthService
import io.github.afchamis21.finapp.auth.service.OneTimeCodeService
import io.github.afchamis21.finapp.auth.types.JwtAuth
import io.github.afchamis21.finapp.auth.types.NoAuth
import io.github.afchamis21.finapp.http.response.Response
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val oneTimeCodeService: OneTimeCodeService
) {

    @NoAuth
    @PostMapping("/login")
    fun login(@RequestBody @Valid req: LoginRequest): Response<LoginDTO> {
        return Response.ok(authService.login(req))
    }

    @NoAuth
    @PostMapping("/refresh")
    fun login(@RequestBody @Valid req: RefreshRequest): Response<LoginDTO> {
        return Response.ok(authService.refresh(req))
    }

    @JwtAuth
    @PostMapping("/logout")
    fun logout(): Response<Unit> {
        return Response.ok(authService.logout())
    }

    @JwtAuth
    @PostMapping("/code")
    fun oneTimeCode(): Response<LoginDTO> {
        return Response.ok(oneTimeCodeService.generateOneTimeCode())
    }
}