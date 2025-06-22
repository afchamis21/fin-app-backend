package io.github.afchamis21.finapp.entry.request

import java.math.BigDecimal
import java.time.LocalDate

data class UpdateEntryRequest(
    val value: BigDecimal?,
    val label: String?,
    val date: LocalDate?,
    val categories: List<Long>?
)


