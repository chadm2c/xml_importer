package com.example.xml_importer.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "products", indexes = [Index(name = "idx_product_name", columnList = "name")])
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 200)
    val name: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(nullable = false, length = 100)
    val brand: String,

    @Column(name = "storage_date", nullable = false)
    val storageDate: LocalDate,

    @Column(nullable = false, precision = 10, scale = 2)
    val price: BigDecimal,

    @Column(length = 50)
    val category: String? = null,

    @Column(name = "sku", unique = true, length = 100)
    val sku: String? = null,

    @Column(name = "quantity_in_stock")
    val quantityInStock: Int = 0,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)