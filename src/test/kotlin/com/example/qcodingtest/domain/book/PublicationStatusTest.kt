package com.example.qcodingtest.domain.book

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PublicationStatusTest {
    @Test
    fun `published cannot transition back to unpublished`() {
        assertFalse(PublicationStatus.PUBLISHED.canTransitionTo(PublicationStatus.UNPUBLISHED))
    }

    @Test
    fun `unpublished can transition to published`() {
        assertTrue(PublicationStatus.UNPUBLISHED.canTransitionTo(PublicationStatus.PUBLISHED))
    }

    @Test
    fun `same-status transitions are allowed`() {
        assertTrue(PublicationStatus.UNPUBLISHED.canTransitionTo(PublicationStatus.UNPUBLISHED))
        assertTrue(PublicationStatus.PUBLISHED.canTransitionTo(PublicationStatus.PUBLISHED))
    }
}
