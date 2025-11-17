package com.example.xml_importer.service

import com.example.xml_importer.entity.Product
import com.example.xml_importer.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class FileUploadService(
    private val xmlParserService: XmlParserService,
    private val productRepository: ProductRepository
) {

    fun processXmlFile(file: MultipartFile): FileUploadResult {
        return try {
            // File type validation
            if (!isValidXmlFile(file)) {
                return FileUploadResult(
                    success = false,
                    message = "Invalid file type. Please upload an XML file.",
                    importedCount = 0,
                    totalProcessed = 0,
                    errors = listOf("File must be an XML file with .xml extension")
                )
            }

            val parseResult = xmlParserService.parseProductsFromXml(file.bytes, file.size)

            if (parseResult.products.isEmpty() && parseResult.errors.isEmpty()) {
                return FileUploadResult(
                    success = false,
                    message = "No valid products found in XML file",
                    importedCount = 0,
                    totalProcessed = parseResult.totalProcessed,
                    errors = listOf("The XML file doesn't contain any valid product data")
                )
            }

            // Check for duplicate SKUs
            val allErrors = parseResult.errors.toMutableList()
            val duplicateSkus = findDuplicateSkus(parseResult.products)
            if (duplicateSkus.isNotEmpty()) {
                allErrors.add("Duplicate SKUs found: ${duplicateSkus.joinToString(", ")}")
            }

            // Save valid products
            val savedProducts = if (parseResult.products.isNotEmpty()) {
                productRepository.saveAll(parseResult.products)
            } else {
                emptyList()
            }

            FileUploadResult(
                success = allErrors.isEmpty() && savedProducts.isNotEmpty(),
                message = buildResultMessage(savedProducts.size, parseResult, allErrors),
                importedCount = savedProducts.size,
                totalProcessed = parseResult.totalProcessed,
                errors = allErrors
            )
        } catch (e: Exception) {
            FileUploadResult(
                success = false,
                message = "Error processing file: ${e.message}",
                importedCount = 0,
                totalProcessed = 0,
                errors = listOf("System error: ${e.message}")
            )
        }
    }

    private fun isValidXmlFile(file: MultipartFile): Boolean {
        val originalFilename = file.originalFilename ?: return false
        return file.contentType?.contains("xml") == true ||
               originalFilename.endsWith(".xml", ignoreCase = true)
    }

    private fun findDuplicateSkus(products: List<Product>): List<String> {
        val skuSet = mutableSetOf<String>()
        val duplicates = mutableListOf<String>()

        products.forEach { product ->
            product.sku?.let { sku ->
                if (!skuSet.add(sku)) {
                    duplicates.add(sku)
                }
            }
        }
        return duplicates
    }

    private fun buildResultMessage(importedCount: Int, parseResult: XmlParserService.ParseResult, errors: List<String>): String {
        return when {
            errors.isNotEmpty() && importedCount > 0 ->
                "Partially successful: Imported $importedCount of ${parseResult.totalProcessed} products. ${errors.size} error(s) occurred."
            errors.isNotEmpty() ->
                "Import failed: ${errors.size} error(s) occurred. No products imported."
            else ->
                "Successfully imported $importedCount products"
        }
    }

    data class FileUploadResult(
        val success: Boolean,
        val message: String,
        val importedCount: Int = 0,
        val totalProcessed: Int = 0,
        val errors: List<String> = emptyList()
    )
}