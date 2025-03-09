package io.github.afchamis21.finapp.cron

import io.github.afchamis21.finapp.service.RefreshTokenService
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@EnableScheduling
class ScheduledTasks(private val refreshTokenService: RefreshTokenService) {

    @Scheduled(cron = "0 0 */6 * * *")
    fun clearExpiredRefreshTokens() {
        refreshTokenService.deleteExpiredRefreshTokens()
    }
}