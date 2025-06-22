package io.github.afchamis21.finapp.http

import io.github.afchamis21.finapp.config.logger
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
        log.info("Request started: {} {}", request.method, request.requestURI)

        return super.preHandle(request, response, handler)
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        try {
            if (ex != null) {
                log.error(
                    "Unhandled exception during request processing for {} {}:",
                    request.method,
                    request.requestURI,
                    ex
                )
            }

            if (Context.getExceptions().isNotEmpty()) {
                log.warn(
                    "There were {} custom exceptions captured during the request execution:",
                    Context.getExceptions().size
                )
                Context.getExceptions().forEach { customEx ->
                    log.warn("Captured exception detail:", customEx)
                }
            }

            val duration = Duration.between(Context.getStart(), Instant.now())
            log.info(
                "Request finished: {} {} | Status: {} | Duration: {}ms",
                request.method,
                request.requestURI,
                response.status,
                duration.toMillis()
            )
        } finally {
            Context.clearContext()
        }

        Context.clearContext()
        super.afterCompletion(request, response, handler, ex)
    }
}