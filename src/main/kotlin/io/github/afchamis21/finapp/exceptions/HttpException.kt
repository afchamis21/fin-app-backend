package io.github.afchamis21.finapp.exceptions

import io.github.afchamis21.finapp.http.Context
import org.springframework.http.HttpStatus

class HttpException(
    val status: HttpStatus,
    message: String? = null
) : RuntimeException(message) {
    init {
        message?.let { Context.addMessage(it) }
    }
}