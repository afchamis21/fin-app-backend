package io.github.afchamis21.finapp.http.response

import io.github.afchamis21.finapp.context.Context
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity

class Response<T>(
    body: ResponseBody<T>,
    status: HttpStatusCode
) : ResponseEntity<ResponseBody<T>>(body, status) {

    companion object {
        fun <T> build(body: T, status: HttpStatus): Response<T> {
            return Response<T>(ResponseBody(body, Context.getMessages()), status)
        }

        fun <T> ok(body: T): Response<T> {
            return build(body, HttpStatus.OK)
        }
    }
}