package io.github.afchamis21.finapp.config

import io.github.afchamis21.finapp.auth.interceptor.AuthInterceptor
import io.github.afchamis21.finapp.http.ContextInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class InterceptorConfig(
    private val contextInterceptor: ContextInterceptor,
    private val authInterceptor: AuthInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(contextInterceptor)
        registry.addInterceptor(authInterceptor)
    }
}