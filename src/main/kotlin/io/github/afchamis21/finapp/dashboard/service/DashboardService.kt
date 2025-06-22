package io.github.afchamis21.finapp.dashboard.service

import io.github.afchamis21.finapp.category.model.CategoryType
import io.github.afchamis21.finapp.config.logger
import io.github.afchamis21.finapp.entry.dto.ProfitChartDTO
import io.github.afchamis21.finapp.entry.repo.EntryJpaRepository
import io.github.afchamis21.finapp.exceptions.HttpException
import io.github.afchamis21.finapp.http.Context
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

@Service
class DashboardService(
    private val entryJpaRepository: EntryJpaRepository
) {
    private val log = logger()

    fun getProfitChartData(start: LocalDate, end: LocalDate): ProfitChartDTO {
        val userId = Context.userId ?: throw HttpException(HttpStatus.FORBIDDEN)
        log.info("Generating profit chart data for user $userId from $start to $end.")

        val entries = entryJpaRepository.findAllByOwnerIdOrderByDateAsc(userId)
        log.info("Fetched ${entries.size} total financial entries for user $userId to process.")

        val data = mutableMapOf<String, BigDecimal>()
        var lastLabel: String? = null

        log.debug("Starting first loop to calculate monthly running totals from entries...")
        entries.forEach { entry ->
            val label = formatLabel(entry.date)
            log.trace("Processing entry {} (date: {}) for month-label '{}'.", entry.id, entry.date, label)

            val previousValue = data[lastLabel] ?: BigDecimal.ZERO
            val currentValue = data.getOrPut(label) { previousValue }

            for (category in entry.categories) {
                val value = when (category.type) {
                    CategoryType.IN -> entry.value
                    CategoryType.OUT -> entry.value.negate()
                }

                data[label] = currentValue + value
                lastLabel = label
                break
            }
        }
        log.debug("Finished first loop. Intermediate monthly data: {}", data)

        var lastValue = BigDecimal.ZERO
        log.debug("Starting second loop to find starting balance before {}...", start)
        for ((label, value) in data) {
            val date = yearMonthFromLabel(label).atDay(1)
            if (date.isBefore(start)) {
                lastValue = value
            } else {
                break
            }
        }
        log.info("Calculated starting balance (value of last month before $start) as: $lastValue")

        val result = mutableMapOf<String, BigDecimal>()
        var currentDate = start

        log.debug("Starting third loop to build final result map from {} to {}...", start, end)
        while (!currentDate.isAfter(end)) {
            val label = formatLabel(currentDate)

            lastValue = data[label] ?: lastValue
            result[label] = lastValue

            currentDate = currentDate.plusMonths(1)
        }
        log.info("Finished generating chart data. Result contains ${result.size} data points.")
        log.debug("Final chart data: {}", result)

        return ProfitChartDTO(result)
    }

    private fun formatLabel(date: LocalDate): String =
        "${date.monthValue.toString().padStart(2, '0')}/${date.year}"

    private fun yearMonthFromLabel(label: String): YearMonth {
        val (month, year) = label.split("/").map { it.toInt() }
        return YearMonth.of(year, month)
    }
}