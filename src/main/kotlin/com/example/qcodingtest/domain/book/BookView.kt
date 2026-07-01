package com.example.qcodingtest.domain.book

/**
 * 書籍の参照専用モデル。リポジトリ層での取得結果を表す。
 */
data class BookView(
    val id: Long,
    val title: String,
    val price: Int,
    val publicationStatus: PublicationStatus,
)
