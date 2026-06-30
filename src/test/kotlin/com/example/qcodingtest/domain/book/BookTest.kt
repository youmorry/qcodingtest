package com.example.qcodingtest.domain.book

import com.example.qcodingtest.domain.BusinessRuleViolationException
import org.junit.jupiter.api.DisplayName
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
    @DisplayName("IDを保持したまま新しい値を適用する")
    fun `should apply new values and keep the persisted id`() {
        val updated = published.applyUpdate(title = "改訂版", price = 1200, publicationStatus = PublicationStatus.PUBLISHED)

        assertEquals(published.copy(title = "改訂版", price = 1200), updated)
    }

    @Test
    @DisplayName("出版済みから未出版への遷移を弾く")
    fun `should reject transition from published to unpublished`() {
        assertFailsWith<BusinessRuleViolationException> {
            published.applyUpdate(title = published.title, price = published.price, publicationStatus = PublicationStatus.UNPUBLISHED)
        }
    }
}
