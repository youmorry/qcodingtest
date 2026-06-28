package com.example.qcodingtest.domain.book

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BookWithAuthorsTest {
    private val book =
        Book(id = 1L, title = "テスト書籍", price = 1000, publicationStatus = PublicationStatus.UNPUBLISHED)

    @Test
    fun `should hold the book and its authors`() {
        val bookWithAuthors = BookWithAuthors(book = book, authorIds = setOf(10L, 20L))

        assertEquals(book, bookWithAuthors.book)
        assertEquals(setOf(10L, 20L), bookWithAuthors.authorIds)
    }

    @Test
    fun `should reject a book without any author`() {
        assertFailsWith<IllegalArgumentException> {
            BookWithAuthors(book = book, authorIds = emptySet())
        }
    }
}
