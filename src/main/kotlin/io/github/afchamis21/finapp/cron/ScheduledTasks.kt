package io.github.afchamis21.finapp.cron

import io.github.afchamis21.finapp.auth.service.RefreshTokenService
import io.github.afchamis21.finapp.config.logger
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@EnableScheduling
class ScheduledTasks(private val refreshTokenService: RefreshTokenService) {

    private val log = logger()

    /**
     * This scheduled task runs periodically to clean up expired refresh tokens from the database.
     * The cron expression means it runs at the start of the minute, at the start of the hour,
     * every 6 hours. This corresponds to 00:00, 06:00, 12:00, and 18:00 every day.
     */
    @Scheduled(cron = "0 0 */6 * * *")
    fun clearExpiredRefreshTokens() {
        log.info("Scheduled task 'clearExpiredRefreshTokens' triggered by cron expression.")
        try {
            refreshTokenService.deleteExpiredRefreshTokens()
        } catch (e: Exception) {
            log.error("An error occurred during the 'clearExpiredRefreshTokens' scheduled task.", e)
        } finally {
            log.info("Scheduled task 'clearExpiredRefreshTokens' finished.")
        }
    }
}