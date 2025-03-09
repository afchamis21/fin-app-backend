package io.github.afchamis21.finapp.controller

import io.github.afchamis21.finapp.auth.types.AdminAuth
import io.github.afchamis21.finapp.auth.types.JwtAuth
import io.github.afchamis21.finapp.http.dto.UserDTO
import io.github.afchamis21.finapp.http.request.user.RegisterUserRequest
import io.github.afchamis21.finapp.http.request.user.UpdateUserRequest
import io.github.afchamis21.finapp.http.response.Response
import io.github.afchamis21.finapp.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
class UserController(private val userService: UserService) {

    @AdminAuth
    @PostMapping
    fun registerUser(@RequestBody @Valid req: RegisterUserRequest): Response<UserDTO> {
        return Response.build(userService.register(req), HttpStatus.CREATED)
    }

    @JwtAuth
    @GetMapping
    fun getUser(): Response<UserDTO> {
        return Response.ok(userService.getCurrentUser())
    }

    @JwtAuth
    @PutMapping
    fun updateUser(@RequestBody @Valid req: UpdateUserRequest): Response<UserDTO> {
        return Response.ok(userService.update(req))
    }
}