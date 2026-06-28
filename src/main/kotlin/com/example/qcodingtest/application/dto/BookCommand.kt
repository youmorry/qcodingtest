package com.example.qcodingtest.application.dto

import com.example.qcodingtest.domain.book.PublicationStatus

data class BookCommand(
    val title: String,
    val price: Int,
    val publicationStatus: PublicationStatus,
    val authorIds: Set<Long>,
)
