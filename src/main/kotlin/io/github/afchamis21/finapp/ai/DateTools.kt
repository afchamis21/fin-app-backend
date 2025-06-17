package io.github.afchamis21.finapp.ai

import org.springframework.ai.tool.annotation.Tool
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class DateTools {

    @Tool(name = "GetToday", description = "Returns the current date on the system timezone")
    fun getToday(): LocalDate {
        return LocalDate.now()
    }


    @Tool(name = "GetCurrentYear", description = "Returns the current year on the system timezone")
    fun getCurrentYear(): Int {
        return LocalDate.now().year
    }
}