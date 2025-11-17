package com.example.xml_importer.service

import com.example.xml_importer.entity.Product
import org.springframework.stereotype.Service
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import java.io.ByteArrayInputStream
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.xml.parsers.DocumentBuilderFactory

@Service
class XmlParserService {

    companion object {
        private const val MAX_FILE_SIZE = 10 * 1024 * 1024 // 10MB
        private val SUPPORTED_DATE_FORMATS = listOf(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy")
        )
    }

    data class ParseResult(
        val products: List<Product>,
        val errors: List<String> = emptyList(),
        val totalProcessed: Int = 0
    )

    fun parseProductsFromXml(xmlContent: ByteArray, fileSize: Long): ParseResult {
        val errors = mutableListOf<String>()
        val products = mutableListOf<Product>()

        // File size validation
        if (fileSize > MAX_FILE_SIZE) {
            errors.add("File size exceeds maximum limit of 10MB")
            return ParseResult(emptyList(), errors)
        }

        if (fileSize == 0L) {
            errors.add("File is empty")
            return ParseResult(emptyList(), errors)
        }

        var productNodes: NodeList? = null

        try {
            val dbFactory = DocumentBuilderFactory.newInstance()

            // Security: Disable external entity processing to prevent XXE attacks
            // These are the correct ways to prevent XXE attacks
            dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false)
            dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)

            // Alternative XXE protection
            dbFactory.isExpandEntityReferences = false
            dbFactory.isXIncludeAware = false

            val dBuilder = dbFactory.newDocumentBuilder()
            val inputStream = ByteArrayInputStream(xmlContent)
            val doc: Document = dBuilder.parse(inputStream)
            doc.documentElement.normalize()

            // Validate root element
            if (doc.documentElement.tagName != "products") {
                errors.add("Invalid XML structure: root element must be 'products'")
                return ParseResult(emptyList(), errors)
            }

            productNodes = doc.getElementsByTagName("product")

            if (productNodes.length == 0) {
                errors.add("No product elements found in XML file")
                return ParseResult(emptyList(), errors)
            }

            for (i in 0 until productNodes.length) {
                try {
                    val productElement = productNodes.item(i)
                    val product = parseProductElement(productElement, i + 1)
                    product?.let { products.add(it) }
                } catch (e: Exception) {
                    errors.add("Error parsing product at position ${i + 1}: ${e.message}")
                }
            }

        } catch (e: Exception) {
            errors.add("Error parsing XML file: ${e.message}")
        }

        return ParseResult(
            products,
            errors,
            totalProcessed = productNodes?.length ?: 0
        )
    }

    private fun parseProductElement(productElement: org.w3c.dom.Node, position: Int): Product? {
        try {
            val element = productElement as? org.w3c.dom.Element ?: return null

            // Required fields validation
            val name = getElementText(element, "name")?.takeIf { it.isNotBlank() }
                ?: throw IllegalArgumentException("Product name is required")

            val brand = getElementText(element, "brand")?.takeIf { it.isNotBlank() }
                ?: throw IllegalArgumentException("Brand is required")

            val priceText = getElementText(element, "price") ?: throw IllegalArgumentException("Price is required")
            val price = parsePrice(priceText) ?: throw IllegalArgumentException("Invalid price format: $priceText")

            val storageDateText = getElementText(element, "storageDate") ?: throw IllegalArgumentException("Storage date is required")
            val storageDate = parseDate(storageDateText) ?: throw IllegalArgumentException("Invalid date format: $storageDateText")

            // Optional fields
            val description = getElementText(element, "description")
            val category = getElementText(element, "category")
            val sku = getElementText(element, "sku")
            val quantityText = getElementText(element, "quantityInStock")
            val quantityInStock = quantityText?.toIntOrNull() ?: 0

            // Business logic validation
            if (price <= BigDecimal.ZERO) {
                throw IllegalArgumentException("Price must be greater than zero")
            }

            if (storageDate.isAfter(LocalDate.now())) {
                throw IllegalArgumentException("Storage date cannot be in the future")
            }

            if (name.length > 200) {
                throw IllegalArgumentException("Product name exceeds maximum length of 200 characters")
            }

            return Product(
                name = name.trim(),
                description = description?.trim(),
                brand = brand.trim(),
                storageDate = storageDate,
                price = price,
                category = category?.trim(),
                sku = sku?.trim(),
                quantityInStock = quantityInStock
            )
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse product at position $position: ${e.message}")
        }
    }

    private fun parsePrice(priceText: String): BigDecimal? {
        return try {
            // Remove any currency symbols and whitespace, handle different formats
            val cleanedPrice = priceText.replace("[^\\d.,]".toRegex(), "").trim()
            // Replace comma with dot for decimal separator if needed
            val normalizedPrice = cleanedPrice.replace(",", ".")
            BigDecimal(normalizedPrice)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseDate(dateText: String): LocalDate? {
        return SUPPORTED_DATE_FORMATS.firstNotNullOfOrNull { formatter ->
            try {
                LocalDate.parse(dateText.trim(), formatter)
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun getElementText(parent: org.w3c.dom.Element, tagName: String): String? {
        val elements = parent.getElementsByTagName(tagName)
        return if (elements.length > 0) {
            elements.item(0).textContent?.trim()
        } else {
            null
        }
    }
}