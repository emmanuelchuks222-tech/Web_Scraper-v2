# Book Crawler — M0: Parse one book page

Takes the URL of one book page on [books.toscrape.com](https://books.toscrape.com), downloads it, and prints the book's details as typed data.

## What you need

- A JDK, version 17 or newer, available on your `PATH` (check with `java -version`).
- An internet connection.

You do not need to install Gradle or Kotlin. The project includes the Gradle wrapper (`gradlew`), which downloads what it needs the first time you run it.

## Run it

From the project folder (`web-crawler`), pass one book URL.

**Windows (PowerShell or cmd)**

```
.\gradlew.bat run --args="https://books.toscrape.com/catalogue/a-light-in-the-attic_1000/index.html"
```

**macOS / Linux**

```
./gradlew run --args="https://books.toscrape.com/catalogue/a-light-in-the-attic_1000/index.html"
./gradlew run --args="https://books.toscrape.com/catalogue/tipping-the-velvet_999/index.html"

```

The first run takes longer because Gradle downloads itself and the dependencies. Keep the quotes around the URL.

### Example output

<!-- Replace with the real output from your own run before committing. -->

```
========== BOOK INFORMATION ==========
Name: A Light in the Attic
Category: Poetry
Price (incl. tax): £51.77
Price (excl. tax): £51.77
Availability: In stock (22 available)
Stock count: 29
Rating: 0/5
Review count: 0
Image URL: https://books.toscrape.com/media/cache/.../....jpg
Description: It's hard to imagine a world without A Light in the Attic. ...
======================================
```

### What happens when something is wrong

| Situation | Result |
|---|---|
| No URL, or more than one | Prints `Please provide exactly one book URL.` |
| The page can't be downloaded (bad URL, no internet) | Prints `Book crawler failed: ...` with the reason |
| A required field is missing or malformed on the page | Prints `Book crawler failed: Missing required field '<field>' on page: <url>` |
| The book has no description | The description is `null` and the program prints `Description: null`. It does not crash |

## How it works

```
User gives a book URL
        |
        v
BookPageFetcher   downloads the page with jsoup           -> Document
        |
        v
BookParser        extracts the ten fields, converts types -> BookInfo
        |
        v
Main              prints the BookInfo
```

| File | Job |
|---|---|
| `Main.kt` | Reads the URL argument, connects the steps, prints the result |
| `scraper/BookPageFetcher.kt` | Downloads the page and returns a jsoup `Document` |
| `scraper/BookParser.kt` | Finds each field in the HTML and converts it to the right type |
| `model/BookInfo.kt` | Data class holding the ten fields |
| `enums/BookRating.kt` | Converts the rating word on the page (`Three`) to a number (3) |

## Where each field comes from in the page's HTML

To check any row yourself: open a book page in your browser, right-click the value, choose **Inspect**, and compare with the "Where in the HTML" column.

| Field | Type | Where in the HTML | Selector used in `BookParser` |
|---|---|---|---|
| Name | `String` | `<h1>` inside the product block `<div class="... product_main">` | `.product_main h1` |
| Category | `String` | Third item of the breadcrumb: `<ul class="breadcrumb">` has Home, Books, **Category**, Title | `ul.breadcrumb li` at index 2 |
| Price incl. tax | `BigDecimal` | Product Information table, row labelled `Price (incl. tax)`, value like `£51.77` (the `£` is removed before converting) | `table.table-striped tr`, looked up by label |
| Price excl. tax | `BigDecimal` | Same table, row labelled `Price (excl. tax)` | `table.table-striped tr`, looked up by label |
| Availability text | `String` | Same table, row labelled `Availability`, e.g. `In stock (22 available)` | `table.table-striped tr`, looked up by label |
| Stock count | `Int` | The number in brackets inside the availability text. `0` if the text says out of stock | Regex `\((\d+)\s+available\)` |
| Rating | `BookRating` (1 to 5) | `<p class="star-rating Three">`. The number is the second CSS class, written as a word | `p.star-rating`, second class name, then `BookRating.fromWord` |
| Review count | `Int` | Same table, row labelled `Number of reviews` | `table.table-striped tr`, looked up by label |
| Image URL | `URI` (absolute) | `<img src="../../media/cache/...">` inside `<div id="product_gallery">`. The `src` is relative, so jsoup makes it a full URL | `#product_gallery img`, `absUrl("src")` |
| Description | `String?` | `<div id="product_description">` is only the heading. The text is in the `<p>` right after it | `#product_description + p`. If there is none, the value is `null` |

## Project layout

```
web-crawler/
├── build.gradle.kts          build setup (Kotlin, jsoup, JDK 17 toolchain)
├── settings.gradle.kts
├── gradlew / gradlew.bat     Gradle wrapper, use these to run
└── src/main/kotlin/org/example/
    ├── Main.kt
    ├── enums/BookRating.kt
    ├── model/BookInfo.kt
    └── scraper/
        ├── BookPageFetcher.kt
        └── BookParser.kt
```