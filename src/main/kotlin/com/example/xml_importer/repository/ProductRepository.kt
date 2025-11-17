package com.example.xml_importer.repository

import com.example.xml_importer.entity.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun searchProducts(@Param("query") query: String, pageable: Pageable): Page<Product>

    fun findByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<Product>

    fun findByBrandContainingIgnoreCase(brand: String, pageable: Pageable): Page<Product>

    fun existsBySku(sku: String): Boolean
}