package com.example.qcodingtest.domain.book

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PublicationStatusTest {
    @Test
    @DisplayName("出版済みから未出版へは遷移できない")
    fun `published cannot transition back to unpublished`() {
        assertFalse(PublicationStatus.PUBLISHED.canTransitionTo(PublicationStatus.UNPUBLISHED))
    }

    @Test
    @DisplayName("未出版から出版済みへは遷移できる")
    fun `unpublished can transition to published`() {
        assertTrue(PublicationStatus.UNPUBLISHED.canTransitionTo(PublicationStatus.PUBLISHED))
    }

    @Test
    @DisplayName("同一状態への遷移は許可する")
    fun `same-status transitions are allowed`() {
        assertTrue(PublicationStatus.UNPUBLISHED.canTransitionTo(PublicationStatus.UNPUBLISHED))
        assertTrue(PublicationStatus.PUBLISHED.canTransitionTo(PublicationStatus.PUBLISHED))
    }
}
