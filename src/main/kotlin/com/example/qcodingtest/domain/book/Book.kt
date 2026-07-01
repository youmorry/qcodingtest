package com.example.qcodingtest.domain.book

import com.example.qcodingtest.domain.BusinessRuleViolationException

/**
 * 書籍を表すドメインモデル。
 *
 * @property id 未永続化（採番前）は null。
 */
data class Book(
    val id: Long?,
    val title: String,
    val price: Int,
    val publicationStatus: PublicationStatus,
) {
    init {
        require(title.isNotEmpty()) { "タイトルは空にできません" }
        require(title.length <= MAX_TITLE_LENGTH) { "タイトルは${MAX_TITLE_LENGTH}文字以内である必要があります: ${title.length}" }
        require(price in 0..MAX_PRICE) { "価格は0以上${MAX_PRICE}以下である必要があります: $price" }
    }

    /**
     * 既存書籍へ新しい値を適用した [Book] を返す。
     *
     * @throws BusinessRuleViolationException 出版状況の不正な遷移の場合
     */
    fun applyUpdate(
        title: String,
        price: Int,
        publicationStatus: PublicationStatus,
    ): Book {
        if (!this.publicationStatus.canTransitionTo(publicationStatus)) {
            throw BusinessRuleViolationException("出版済みの書籍を未出版に戻すことはできません")
        }
        return copy(title = title, price = price, publicationStatus = publicationStatus)
    }

    companion object {
        // 上限値はドメイン上の絶対的な定義ではなく、異常入力を弾くための現実的なガードレール。
        const val MAX_TITLE_LENGTH = 100
        const val MAX_PRICE = 1_000_000
    }
}
