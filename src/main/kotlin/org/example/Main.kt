package org.example

import org.example.scraper.BookPageFetcher
import org.example.scraper.BookParser
import org.example.model.BookInfo

fun main(args: Array<String>) {

    // Check that the user provided a URL.
    if (args.size != 1) {
        println("Please provide exactly one book URL.")
        return
    }

    val url = args[0]

    try {

        // Step 1: Download the webpage.
        val document = BookPageFetcher.fetch(url)

        // Step 2: Extract the book information.
        val book = BookParser.parse(document)

        // Step 3: Print the extracted information.
        println("========== BOOK INFORMATION ==========")

        println("Name: ${book.name}")
        println("Category: ${book.category}")

        println("Price (incl. tax): £${book.priceInclTax}")
        println("Price (excl. tax): £${book.priceExclTax}")

        println("Availability: ${book.availabilityText}")
        println("Stock count: ${book.stockCount}")

        // BookRating is an enum, so retrieve its numerical score.
        println("Rating: ${book.rating.score}/5")

        println("Review count: ${book.reviewCount}")
        println("Image URL: ${book.imageUrl}")

        // A book with no description has null here, so this prints "null".
        println("Description: ${book.description}")

        println("======================================")

    } catch (exception: Exception) {

        System.err.println(
            "Book crawler failed: ${exception.message}"
        )
    }
}