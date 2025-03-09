package io.github.afchamis21.finapp.interceptor

import io.github.afchamis21.finapp.auth.types.AdminAuth
import io.github.afchamis21.finapp.auth.types.JwtAuth
import io.github.afchamis21.finapp.auth.types.NoAuth
import io.github.afchamis21.finapp.context.Context
import io.github.afchamis21.finapp.exceptions.HttpException
import io.github.afchamis21.finapp.service.ApiKeyService
import io.github.afchamis21.finapp.service.JwtService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthInterceptor(private val jwtService: JwtService, private val apiKeyService: ApiKeyService) :
    HandlerInterceptor {

    private enum class AuthType { JWT, NO_AUTH, ADMIN }

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val handlerMethod = handler as? HandlerMethod ?: return true // Permite falha para 404

        val authType = defineAuthType(handlerMethod)

        return when (authType) {
            AuthType.JWT -> handleJwt(request)
            AuthType.ADMIN -> handleAdmin(request)
            AuthType.NO_AUTH -> true
        }
    }

    private fun handleAdmin(request: HttpServletRequest): Boolean {
        val token = getBearerToken(request) ?: throw HttpException(HttpStatus.UNAUTHORIZED)
        if (!apiKeyService.validateKey(token)) {
            throw HttpException(HttpStatus.UNAUTHORIZED)
        }

        return true
    }

    private fun handleJwt(request: HttpServletRequest): Boolean {
        val token = getBearerToken(request) ?: throw HttpException(HttpStatus.UNAUTHORIZED, "Falha na autenticação")
        val claims =
            jwtService.validateToken(token, JwtService.TokenType.ACCESS) ?: throw HttpException(
                HttpStatus.UNAUTHORIZED,
                "Token inválido"
            )
        val userId =
            jwtService.getUserId(claims) ?: throw HttpException(HttpStatus.UNAUTHORIZED, "Usuário não identificado")
        Context.userId = userId
        return true
    }

    private fun getBearerToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization")
        if (header == null || !header.uppercase().contains("BEARER")) {
            return null
        }

        return try {
            header.split(" ")[1].trim()
        } catch (ex: IndexOutOfBoundsException) {
            null
        }
    }

    private fun defineAuthType(handlerMethod: HandlerMethod): AuthType {
        return when {
            handlerMethod.hasMethodAnnotation(JwtAuth::class.java) -> AuthType.JWT
            handlerMethod.hasMethodAnnotation(NoAuth::class.java) -> AuthType.NO_AUTH
            handlerMethod.hasMethodAnnotation(AdminAuth::class.java) -> AuthType.ADMIN

            handlerMethod.beanType.isAnnotationPresent(JwtAuth::class.java) -> AuthType.JWT
            handlerMethod.beanType.isAnnotationPresent(NoAuth::class.java) -> AuthType.NO_AUTH
            handlerMethod.beanType.isAnnotationPresent(AdminAuth::class.java) -> AuthType.ADMIN

            else -> AuthType.JWT
        }
    }
}