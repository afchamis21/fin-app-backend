package io.github.afchamis21.finapp.entry.request

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDate

data class CreateEntryRequest(
    @field:NotNull(message = "Dados de entrada são obrigatórios") @Valid val data: List<CreateEntryData>
) {
    data class CreateEntryData(
        @field:NotNull(message = "O valor é obrigatório") val value: BigDecimal,
        @field:NotBlank(message = "O título é obrigatório") val label: String,
        @field:NotNull(message = "A data é obrigatória") val date: LocalDate,
        @field:NotNull(message = "Categorias não informadas") val categories: List<Long>
    )
}


