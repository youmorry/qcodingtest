package com.example.qcodingtest.domain.book

/**
 * 書籍とその著者（ID 参照）を束ねた集約。書籍の登録・更新で永続化する単位。
 */
data class BookWithAuthors(
    val book: Book,
    val authorIds: Set<Long>,
) {
    init {
        require(authorIds.isNotEmpty()) { "書籍は最低1人の著者を持つ必要があります" }
        require(authorIds.size <= MAX_AUTHOR_COUNT) { "著者は${MAX_AUTHOR_COUNT}人以内である必要があります: ${authorIds.size}" }
    }

    companion object {
        // 上限値はドメイン上の絶対的な定義ではなく、異常入力を弾くための現実的なガードレール。
        const val MAX_AUTHOR_COUNT = 100
    }
}
