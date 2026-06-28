package com.example.qcodingtest.domain.book

import com.example.qcodingtest.domain.BusinessRuleViolationException
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BookTest {
    private val published =
        Book(
            id = 1L,
            title = "既刊書",
            price = 1000,
            publicationStatus = PublicationStatus.PUBLISHED,
        )

    @Test
    fun `should apply new values and keep the persisted id`() {
        val updated = published.applyUpdate(title = "改訂版", price = 1200, publicationStatus = PublicationStatus.PUBLISHED)

        assertEquals(published.copy(title = "改訂版", price = 1200), updated)
    }

    @Test
    fun `should reject transition from published to unpublished`() {
        assertFailsWith<BusinessRuleViolationException> {
            published.applyUpdate(title = published.title, price = published.price, publicationStatus = PublicationStatus.UNPUBLISHED)
        }
    }

    @Test
    fun `should allow transition from unpublished to published`() {
        val unpublished = Book(id = 2L, title = "未刊書", price = 500, publicationStatus = PublicationStatus.UNPUBLISHED)

        val updated =
            unpublished.applyUpdate(
                title = unpublished.title,
                price = unpublished.price,
                publicationStatus = PublicationStatus.PUBLISHED,
            )

        assertEquals(PublicationStatus.PUBLISHED, updated.publicationStatus)
    }

    @Test
    fun `should enforce structural invariants on construction`() {
        assertFailsWith<IllegalArgumentException> {
            published.copy(price = -1)
        }
    }
}
