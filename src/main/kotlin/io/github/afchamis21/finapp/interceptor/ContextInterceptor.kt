package io.github.afchamis21.finapp.interceptor

import io.github.afchamis21.finapp.config.logger
import io.github.afchamis21.finapp.context.Context
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Duration
import java.time.Instant

@Component
class ContextInterceptor : HandlerInterceptor {
    private val log = logger()

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        Context.init()
        log.info("Starting request to [{}]", request.requestURI)

        return super.preHandle(request, response, handler)
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        if (Context.getExceptions().isNotEmpty()) {
            Context.getExceptions().forEach { e ->
                log.error("Exception raised during {}:", request.requestURI, e)
            }
        }

        val duration = Duration.between(Context.getStart(), Instant.now())
        log.info("Request to {} finished in [{}] ms!", request.requestURI, duration.toMillis())

        Context.clearContext()
        super.afterCompletion(request, response, handler, ex)
    }
}