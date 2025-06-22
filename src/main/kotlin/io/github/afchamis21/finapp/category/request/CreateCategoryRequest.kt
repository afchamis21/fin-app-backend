package io.github.afchamis21.finapp.category.request

import io.github.afchamis21.finapp.category.model.CategoryType
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class CreateCategoryRequest(
    @field:NotBlank(message = "O nome da Categoria é obrigatório") var label: String,
    @field:NotBlank(message = "A cor da Categoria é obrigatório") var color: String,
    @field:NotNull(message = "O tipo da Categoria é obrigatório") var type: CategoryType,
    @field:Min(
        value = 1,
        message = "O valor da meta deve ser no mínimo 1, ou não deve ser informado!"
    ) var goal: BigDecimal?
)
