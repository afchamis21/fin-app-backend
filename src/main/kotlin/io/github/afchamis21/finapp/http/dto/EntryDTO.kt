package io.github.afchamis21.finapp.http.dto

import io.github.afchamis21.finapp.domain.entry.Entry
import java.math.BigDecimal
import java.time.LocalDate

data class EntryDTO(
    val id: Long?,
    val value: BigDecimal,
    val label: String,
    val date: LocalDate,

    val categories: List<CategoryDTO>?
)

fun Entry.toDTO(): EntryDTO = EntryDTO(
    id = this.id,
    value = this.value,
    label = this.label,
    date = this.date,
    categories = this.categories.map { it.toDTO() }
)