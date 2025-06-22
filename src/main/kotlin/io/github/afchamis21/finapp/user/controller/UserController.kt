package io.github.afchamis21.finapp.user.controller

import io.github.afchamis21.finapp.auth.types.JwtAuth
import io.github.afchamis21.finapp.auth.types.NoAuth
import io.github.afchamis21.finapp.http.response.Response
import io.github.afchamis21.finapp.user.dto.UserDTO
import io.github.afchamis21.finapp.user.request.RegisterUserRequest
import io.github.afchamis21.finapp.user.request.UpdateUserRequest
import io.github.afchamis21.finapp.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
class UserController(private val userService: UserService) {

    @NoAuth
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