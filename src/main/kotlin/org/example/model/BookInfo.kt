package org.example.model


import org.example.enums.BookRating
import java.math.BigDecimal
import java.net.URI

data class BookInfo(
    val name: String,
    val category: String,
    val priceInclTax: BigDecimal,
    val priceExclTax: BigDecimal,
    val availabilityText: String,
    val stockCount: Int,
    val rating: BookRating,
    val reviewCount: Int,
    val imageUrl: URI,
    val description: String?


)