
package org.example.scraper

import org.jsoup.Jsoup

import org.jsoup.nodes.Document

object BookPageFetcher {

    fun fetch(url: String): Document {

        return Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (compatible; BookCrawler/1.0)")
            .timeout(10_000)
            .get()
    }
}


