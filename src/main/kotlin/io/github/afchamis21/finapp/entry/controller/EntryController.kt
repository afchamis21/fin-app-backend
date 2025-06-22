package io.github.afchamis21.finapp.entry.controller

import io.github.afchamis21.finapp.auth.types.JwtAuth
import io.github.afchamis21.finapp.dashboard.service.DashboardService
import io.github.afchamis21.finapp.entry.dto.EntryDTO
import io.github.afchamis21.finapp.entry.dto.ProfitChartDTO
import io.github.afchamis21.finapp.entry.request.CreateEntryRequest
import io.github.afchamis21.finapp.entry.request.UpdateEntryRequest
import io.github.afchamis21.finapp.entry.service.EntryService
import io.github.afchamis21.finapp.http.response.Response
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@JwtAuth
@RestController
@RequestMapping("/entry")
class EntryController(
    private val entryService: EntryService,
    private val dashboardService: DashboardService
) {

    @PostMapping
    fun createEntry(@RequestBody @Valid req: CreateEntryRequest): Response<List<EntryDTO>> {
        return Response.build(entryService.create(req), HttpStatus.CREATED)
    }

    @PutMapping("/{id}")
    fun updateEntry(@RequestBody @Valid req: UpdateEntryRequest, @PathVariable id: Long): Response<EntryDTO> {
        return Response.ok(entryService.update(req, id))
    }

    @DeleteMapping("/{id}")
    fun deleteEntry(@PathVariable id: Long): Response<Unit> {
        return Response.ok(entryService.delete(id))
    }

    @GetMapping("/search")
    fun getEntries(
        @RequestParam start: LocalDate,
        @RequestParam end: LocalDate
    ): Response<List<EntryDTO>> {
        return Response.ok(entryService.search(start, end))
    }

    @GetMapping("/profitChartData")
    fun getProfitChartData(
        @RequestParam start: LocalDate,
        @RequestParam end: LocalDate
    ): Response<ProfitChartDTO> {
        return Response.ok(dashboardService.getProfitChartData(start, end))
    }
}