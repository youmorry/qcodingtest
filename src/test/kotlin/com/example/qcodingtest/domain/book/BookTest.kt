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
            authorIds = setOf(10L),
        )

    @Test
    fun `applyUpdate applies new values and keeps the persisted id`() {
        val updated =
            published.applyUpdate(
                title = "改訂版",
                price = 1200,
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = setOf(10L, 20L),
            )

        assertEquals(
            Book(
                id = 1L,
                title = "改訂版",
                price = 1200,
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = setOf(10L, 20L),
            ),
            updated,
        )
    }

    @Test
    fun `applyUpdate rejects transition from published to unpublished`() {
        assertFailsWith<BusinessRuleViolationException> {
            published.applyUpdate(
                title = published.title,
                price = published.price,
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = published.authorIds,
            )
        }
    }

    @Test
    fun `applyUpdate still enforces structural invariants`() {
        assertFailsWith<IllegalArgumentException> {
            published.applyUpdate(
                title = published.title,
                price = -1,
                publicationStatus = published.publicationStatus,
                authorIds = published.authorIds,
            )
        }
    }
}
