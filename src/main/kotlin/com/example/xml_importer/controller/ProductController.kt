package com.example.xml_importer.controller

import com.example.xml_importer.service.FileUploadService
import com.example.xml_importer.service.ProductService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Controller
class ProductController(
    private val fileUploadService: FileUploadService,
    private val productService: ProductService
) {

    @GetMapping("/")
    fun index(model: Model): String {
        model.addAttribute("message", "Upload Product XML File")
        return "index"
    }

    @PostMapping("/upload")
    @ResponseBody
    fun handleFileUpload(@RequestParam("file") file: MultipartFile): Map<String, Any> {
        val result = fileUploadService.processXmlFile(file)
        return mapOf(
            "success" to result.success,
            "message" to result.message,
            "importedCount" to result.importedCount,
            "totalProcessed" to result.totalProcessed,
            "errors" to result.errors
        )
    }

    @GetMapping("/products")
    fun showProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "") search: String,
        model: Model
    ): String {
        val pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "createdAt")
        )

        val productPage = if (search.isNotBlank()) {
            productService.searchProducts(search, pageable)
        } else {
            productService.getAllProducts(pageable)
        }

        model.addAttribute("products", productPage.content)
        model.addAttribute("currentPage", page)
        model.addAttribute("totalPages", productPage.totalPages)
        model.addAttribute("totalItems", productPage.totalElements)
        model.addAttribute("search", search)
        model.addAttribute("hasProducts", productPage.hasContent())

        return "products"
    }
}