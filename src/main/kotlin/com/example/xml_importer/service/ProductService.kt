package com.example.xml_importer.service

import com.example.xml_importer.entity.Product
import com.example.xml_importer.repository.ProductRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class ProductService(private val productRepository: ProductRepository) {

    fun searchProducts(query: String, pageable: Pageable): Page<Product> {
        return if (query.isNotBlank()) {
            productRepository.searchProducts(query, pageable)
        } else {
            productRepository.findAll(pageable)
        }
    }

    fun getAllProducts(pageable: Pageable): Page<Product> {
        return productRepository.findAll(pageable)
    }

    fun getProductById(id: Long): Product? {
        return productRepository.findById(id).orElse(null)
    }
}