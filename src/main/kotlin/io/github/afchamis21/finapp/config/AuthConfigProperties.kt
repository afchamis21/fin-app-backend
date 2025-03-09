package io.github.afchamis21.finapp.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.time.temporal.ChronoUnit

/**
 * Configuration class for JWT properties related to user and service authentication.
 */
@Configuration
@ConfigurationProperties(prefix = "auth.jwt")
data class AuthConfigProperties(
    /**
     * JWT properties for user authentication.
     */
    var user: JwtProperties = JwtProperties()
) {
    /**
     * Nested class representing JWT properties.
     */
    data class JwtProperties(
        /**
         * Configuration for access tokens.
         */
        var accessToken: TokenConfig = TokenConfig(),

        /**
         * Configuration for refresh tokens.
         */
        var refreshToken: TokenConfig = TokenConfig()
    ) {
        /**
         * Nested class representing token configuration.
         */
        data class TokenConfig(
            /**
             * Duration of the token validity period.
             */
            var duration: Long = 0,

            /**
             * Unit of time for the token validity period (e.g., days, hours).
             */
            var unit: ChronoUnit = ChronoUnit.SECONDS,

            /**
             * Encryption key used for securing the token.
             */
            var encryptionKey: String = ""
        )
    }
}
