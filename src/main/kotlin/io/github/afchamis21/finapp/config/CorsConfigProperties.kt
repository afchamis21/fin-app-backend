package io.github.afchamis21.finapp.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration


@Configuration
@ConfigurationProperties(prefix = "cors")
data class CorsConfigProperties(
    var allowedUrls: List<String> = emptyList()
) {
}
