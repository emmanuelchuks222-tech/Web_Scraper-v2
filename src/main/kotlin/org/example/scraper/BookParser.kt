package org.example.scraper

import org.example.enums.BookRating
import org.example.model.BookInfo
import org.jsoup.nodes.Document
import java.math.BigDecimal
import java.net.URI

object BookParser {

    private val stockCountPattern =
        Regex("""\((\d+)\s+available\)""", RegexOption.IGNORE_CASE)

    fun parse(document: Document): BookInfo {

        val pageUrl = document.location()

        val productMain = document.selectFirst(".product_main")
            ?: missingField("product section", pageUrl)

        val name = requiredField(
            productMain.selectFirst("h1")?.text(),
            "name",
            pageUrl
        )

        val category = requiredField(
            document.select("ul.breadcrumb li").getOrNull(2)?.text(),
            "category",
            pageUrl
        )

.
        val productDetails = document.select("table.table-striped tr")
            .mapNotNull { row ->
                val cells = row.children()

                if (cells.size >= 2) {
                    cells[0].text() to cells[1].text()
                } else {
                    null
                }
            }
            .toMap()

        val priceInclTax = parsePrice(
            requiredField(
                productDetails["Price (incl. tax)"],
                "price including tax",
                pageUrl
            ),
            "price including tax",
            pageUrl
        )

        val priceExclTax = parsePrice(
            requiredField(
                productDetails["Price (excl. tax)"],
                "price excluding tax",
                pageUrl
            ),
            "price excluding tax",
            pageUrl
        )

        val availabilityText = requiredField(
            productDetails["Availability"],
            "availability text",
            pageUrl
        )

        val stockCount = parseStockCount(availabilityText, pageUrl)

        val ratingWord = requiredField(
            productMain.selectFirst("p.star-rating")
                ?.classNames()
                ?.firstOrNull { it != "star-rating" },
            "rating",
            pageUrl
        )

        val rating = try {
            BookRating.fromWord(ratingWord)
        } catch (exception: IllegalArgumentException) {
            throw IllegalArgumentException(
                "Invalid rating '$ratingWord' on page: $pageUrl",
                exception
            )
        }

        val reviewCount = parseInt(
            requiredField(
                productDetails["Number of reviews"],
                "review count",
                pageUrl
            ),
            "review count",
            pageUrl
        )


        val imageSource = requiredField(
            document.selectFirst("#product_gallery img")?.absUrl("src"),
            "image URL",
            pageUrl
        )

        val imageUrl = try {
            URI.create(imageSource)
        } catch (exception: IllegalArgumentException) {
            throw IllegalArgumentException(
                "Invalid image URL '$imageSource' on page: $pageUrl",
                exception
            )
        }

        if (!imageUrl.isAbsolute) {
            throw IllegalArgumentException(
                "Image URL is not absolute: $imageSource on page: $pageUrl"
            )
        }

.
        val description = document
            .selectFirst("#product_description + p")
            ?.text()
            ?.takeIf { it.isNotBlank() }

        return BookInfo(
            name = name,
            category = category,
            priceInclTax = priceInclTax,
            priceExclTax = priceExclTax,
            availabilityText = availabilityText,
            stockCount = stockCount,
            rating = rating,
            reviewCount = reviewCount,
            imageUrl = imageUrl,
            description = description
        )
    }

    private fun requiredField(
        value: String?,
        fieldName: String,
        pageUrl: String
    ): String {
        return value?.takeIf { it.isNotBlank() }
            ?: missingField(fieldName, pageUrl)
    }

    private fun missingField(
        fieldName: String,
        pageUrl: String
    ): Nothing {
        throw IllegalArgumentException(
            "Missing required field '$fieldName' on page: $pageUrl"
        )
    }

    private fun parsePrice(
        value: String,
        fieldName: String,
        pageUrl: String
    ): BigDecimal {

        val cleanPrice = value.removePrefix("£").trim()

        return try {
            BigDecimal(cleanPrice)
        } catch (exception: NumberFormatException) {
            throw IllegalArgumentException(
                "Invalid $fieldName '$value' on page: $pageUrl",
                exception
            )
        }
    }

    private fun parseInt(
        value: String,
        fieldName: String,
        pageUrl: String
    ): Int {
        return value.toIntOrNull()
            ?: throw IllegalArgumentException(
                "Invalid $fieldName '$value' on page: $pageUrl"
            )
    }

    private fun parseStockCount(
        availabilityText: String,
        pageUrl: String
    ): Int {

        val stockCount = stockCountPattern
            .find(availabilityText)
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()

        if (stockCount != null) {
            return stockCount
        }

        if (availabilityText.contains("out of stock", ignoreCase = true)) {
            return 0
        }

        throw IllegalArgumentException(
            "Could not determine stock count from '$availabilityText' on page: $pageUrl"
        )
    }
}