package io.github.afchamis21.finapp.auth.interceptor

import io.github.afchamis21.finapp.auth.service.JwtService
import io.github.afchamis21.finapp.auth.service.OneTimeCodeService
import io.github.afchamis21.finapp.auth.types.JwtAuth
import io.github.afchamis21.finapp.auth.types.NoAuth
import io.github.afchamis21.finapp.auth.types.OneTimeCodeAuth
import io.github.afchamis21.finapp.config.logger
import io.github.afchamis21.finapp.exceptions.HttpException
import io.github.afchamis21.finapp.http.Context
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthInterceptor(
    private val jwtService: JwtService,
    private val oneTimeCodeService: OneTimeCodeService
) :
    HandlerInterceptor {

    private val log = logger()

    private enum class AuthType { JWT, NO_AUTH, ONE_TIME_CODE }

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val handlerMethod = handler as? HandlerMethod ?: run {
            log.debug("Handler is not a HandlerMethod, allowing request to proceed (e.g., for static resources or 404).")
            return true
        }

        val authType = defineAuthType(handlerMethod)
        log.info("Determined auth type: $authType for handler: ${handlerMethod.beanType.simpleName}.${handlerMethod.method.name}")

        val result = when (authType) {
            AuthType.JWT -> handleJwt(request)
            AuthType.ONE_TIME_CODE -> handleOneTimeCode(request)
            AuthType.NO_AUTH -> {
                log.info("Endpoint requires no authentication. Access granted.")
                true
            }
        }
        log.info("Auth preHandle finished. Result: ${if (result) "Access Granted" else "Access Denied"}")
        return result
    }

    private fun handleOneTimeCode(request: HttpServletRequest): Boolean {
        log.info("Handling One-Time Code authentication.")
        val token = getOneTimeToken(request) ?: run {
            log.warn("One-Time Code authentication failed: 'code' parameter is missing.")
            throw HttpException(HttpStatus.UNAUTHORIZED)
        }

        log.info("Validating one-time code...")
        val entity = oneTimeCodeService.findByCode(token) ?: run {
            log.warn("One-Time Code authentication failed: Code not found in repository.")
            throw HttpException(HttpStatus.UNAUTHORIZED)
        }

        if (!oneTimeCodeService.validateCode(entity)) {
            log.warn("One-Time Code authentication failed: Code is invalid or expired for user ${entity.owner.id}.")
            oneTimeCodeService.delete(entity)
            throw HttpException(HttpStatus.UNAUTHORIZED)
        }

        Context.userId = entity.owner.id
        log.info("One-Time Code authentication successful for user ${entity.owner.id}.")

        oneTimeCodeService.delete(entity)
        return true
    }

    private fun getOneTimeToken(request: HttpServletRequest): String? {
        val code = request.getParameter("code")
        log.debug("Extracted one-time code from request parameter: ${if (code != null) "'$code'" else "null"}")
        return code
    }

    private fun handleJwt(request: HttpServletRequest): Boolean {
        log.info("Handling JWT authentication.")
        val token = getBearerToken(request) ?: run {
            log.warn("JWT authentication failed: Authorization header missing or malformed.")
            throw HttpException(HttpStatus.UNAUTHORIZED)
        }

        log.info("Validating JWT...")
        val claims = jwtService.validateToken(token, JwtService.TokenType.ACCESS) ?: run {
            log.warn("JWT validation failed: Token is invalid, expired, or signature is incorrect.")
            throw HttpException(HttpStatus.UNAUTHORIZED)
        }

        val userId = jwtService.getUserId(claims) ?: run {
            log.error("JWT validation failed: Token is valid but does not contain a userId claim.")
            throw HttpException(HttpStatus.UNAUTHORIZED)
        }

        Context.userId = userId
        log.info("JWT authentication successful for user $userId.")
        return true
    }

    private fun getBearerToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization")
        if (header == null || !header.uppercase().startsWith("BEARER ")) {
            log.debug("Authorization header is missing or does not start with 'Bearer '.")
            return null
        }

        return try {
            val token = header.substring(7)
            log.debug("Extracted Bearer token from header.")
            token
        } catch (ex: IndexOutOfBoundsException) {
            log.warn("Malformed Authorization header: 'Bearer ' keyword found but no token followed.")
            null
        }
    }

    private fun defineAuthType(handlerMethod: HandlerMethod): AuthType {
        log.debug("Defining auth type for method: ${handlerMethod.method.name}")
        return when {
            handlerMethod.hasMethodAnnotation(JwtAuth::class.java) -> AuthType.JWT
            handlerMethod.hasMethodAnnotation(NoAuth::class.java) -> AuthType.NO_AUTH
            handlerMethod.hasMethodAnnotation(OneTimeCodeAuth::class.java) -> AuthType.ONE_TIME_CODE

            handlerMethod.beanType.isAnnotationPresent(JwtAuth::class.java) -> AuthType.JWT
            handlerMethod.beanType.isAnnotationPresent(NoAuth::class.java) -> AuthType.NO_AUTH
            handlerMethod.beanType.isAnnotationPresent(OneTimeCodeAuth::class.java) -> AuthType.ONE_TIME_CODE

            else -> {
                log.debug("No specific auth annotation found, defaulting to JWT.")
                AuthType.JWT
            }
        }
    }
}