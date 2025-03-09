package io.github.afchamis21.finapp.controller

import io.github.afchamis21.finapp.auth.types.JwtAuth
import io.github.afchamis21.finapp.auth.types.NoAuth
import io.github.afchamis21.finapp.http.dto.LoginDTO
import io.github.afchamis21.finapp.http.request.auth.LoginRequest
import io.github.afchamis21.finapp.http.request.auth.RefreshRequest
import io.github.afchamis21.finapp.http.response.Response
import io.github.afchamis21.finapp.service.AuthService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private var authService: AuthService
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
}