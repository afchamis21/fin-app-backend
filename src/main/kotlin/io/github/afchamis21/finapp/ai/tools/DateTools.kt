package io.github.afchamis21.finapp.ai.tools

import io.github.afchamis21.finapp.config.logger
import org.springframework.ai.tool.annotation.Tool
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.ZoneId

@Component
class DateTools {

    private val log = logger()
    private val brazilZoneId = ZoneId.of("America/Sao_Paulo")

    @Tool(name = "GetToday", description = "Returns the current date on the system timezone")
    fun getToday(): LocalDate {
        log.debug("Tool 'GetToday' invoked.")
        val today = LocalDate.now(brazilZoneId)
        log.debug("Returning current date: {}", today)
        return today
    }


    @Tool(name = "GetCurrentYear", description = "Returns the current year on the system timezone")
    fun getCurrentYear(): Int {
        log.debug("Tool 'GetCurrentYear' invoked.")
        val currentYear = LocalDate.now(brazilZoneId).year
        log.debug("Returning current year: {}", currentYear)
        return currentYear
    }
}