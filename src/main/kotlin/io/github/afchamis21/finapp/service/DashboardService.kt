package io.github.afchamis21.finapp.service

import io.github.afchamis21.finapp.context.Context
import io.github.afchamis21.finapp.domain.category.CategoryType
import io.github.afchamis21.finapp.exceptions.HttpException
import io.github.afchamis21.finapp.http.dto.ProfitChartDTO
import io.github.afchamis21.finapp.repo.jpa.EntryJpaRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

@Service
class DashboardService(
    private val entryJpaRepository: EntryJpaRepository
) {
    fun getProfitChartData(start: LocalDate, end: LocalDate): ProfitChartDTO {
        val userId = Context.userId ?: throw HttpException(HttpStatus.FORBIDDEN)

        val entries = entryJpaRepository.findAllByOwnerIdOrderByDateAsc(userId)

        val data = mutableMapOf<String, BigDecimal>()
        var lastLabel: String? = null

        entries.forEach { entry ->
            val label = formatLabel(entry.date)

            val previousValue = data[lastLabel] ?: BigDecimal.ZERO
            val currentValue = data.getOrPut(label) { previousValue }

            for (category in entry.categories) {
                val value = when (category.type) {
                    CategoryType.IN -> entry.value
                    CategoryType.OUT -> entry.value.negate()
                }

                data[label] = currentValue + value
                lastLabel = label
            }
        }

        var lastValue = BigDecimal.ZERO
        for ((label, value) in data) {
            val date = yearMonthFromLabel(label).atDay(1)
            if (date.isBefore(start)) {
                lastValue = value
            } else {
                break
            }
        }

        val result = mutableMapOf<String, BigDecimal>()
        var currentDate = start

        while (!currentDate.isAfter(end)) {
            val label = formatLabel(currentDate)

            lastValue = data[label] ?: lastValue
            result[label] = lastValue

            currentDate = currentDate.plusMonths(1)
        }

        return ProfitChartDTO(result)
    }

    private fun formatLabel(date: LocalDate): String =
        "${date.monthValue.toString().padStart(2, '0')}/${date.year}"

    private fun yearMonthFromLabel(label: String): YearMonth {
        val (month, year) = label.split("/").map { it.toInt() }
        return YearMonth.of(year, month)
    }
}
