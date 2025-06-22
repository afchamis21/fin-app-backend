package io.github.afchamis21.finapp.exceptions

import io.github.afchamis21.finapp.http.Context
import io.github.afchamis21.finapp.http.response.Response
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class ExceptionController {
    private fun preHandle(ex: Exception) {
        Context.addException(ex)
    }

    @ExceptionHandler(HttpException::class)
    fun handleHttpException(ex: HttpException): Response<Unit> {
        preHandle(ex)
        return Response.build(Unit, ex.status)
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleResourceNotFound(ex: NoResourceFoundException): Response<Unit> {
        preHandle(ex)
        return Response.build(Unit, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(ex: HttpMessageNotReadableException): Response<Unit> {
        preHandle(ex)
        return Response.build(Unit, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationError(ex: MethodArgumentNotValidException): Response<Unit> {
        preHandle(ex)
        ex.fieldErrors.forEach {
            it.defaultMessage?.let { message -> Context.addMessage(message) }
        }

        return Response.build(Unit, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): Response<Unit> {
        preHandle(ex)
        return Response.build(Unit, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}