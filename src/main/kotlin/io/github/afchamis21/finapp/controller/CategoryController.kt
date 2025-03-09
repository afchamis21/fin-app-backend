package io.github.afchamis21.finapp.controller

import io.github.afchamis21.finapp.auth.types.JwtAuth
import io.github.afchamis21.finapp.http.dto.CategoryDTO
import io.github.afchamis21.finapp.http.request.category.CreateCategoryRequest
import io.github.afchamis21.finapp.http.request.category.UpdateCategoryRequest
import io.github.afchamis21.finapp.http.response.Response
import io.github.afchamis21.finapp.service.CategoryService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@JwtAuth
@RestController
@RequestMapping("/category")
class CategoryController(private val categoryService: CategoryService) {

    @PostMapping
    fun createCategory(@RequestBody @Valid req: CreateCategoryRequest): Response<CategoryDTO> {
        return Response.build(categoryService.createCategory(req), HttpStatus.CREATED)
    }

    @GetMapping("/list")
    fun getCategory(
        @RequestParam(
            required = false,
        ) active: Boolean?
    ): Response<List<CategoryDTO>> {
        return Response.ok(categoryService.getCategoriesByOwner(active))
    }

    @PutMapping("/{id}")
    fun updateCategory(@RequestBody req: UpdateCategoryRequest, @PathVariable id: Long): Response<CategoryDTO> {
        return Response.ok(categoryService.updateCategory(id, req))
    }

    @DeleteMapping("/{id}")
    fun deleteCategory(@PathVariable id: Long): Response<Unit> {
        return Response.ok(categoryService.deleteCategory(id))
    }

    @PatchMapping("/{id}")
    fun changeStatus(@PathVariable id: Long, @RequestParam(required = true) active: Boolean): Response<Unit> {
        return Response.ok(categoryService.updateStatus(id, active))
    }
}