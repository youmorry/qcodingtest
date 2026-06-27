package com.example.qcodingtest.domain.book

import com.example.qcodingtest.domain.BusinessRuleViolationException

/**
 * 書籍を表すドメインモデル。著者は ID 参照で保持する（書籍と著者は多対多）。
 *
 * 構造的な不変条件（タイトル長・価格・著者数）はインスタンス生成時に強制し、不正な状態の [Book] を
 * 構築できないようにする。出版状況の遷移ルールは更新時の振る舞いとして別途扱う。
 *
 * @property id 未永続化（採番前）は null。
 * @property authorIds 同一著者の重複に意味はないため [Set] とする（中間テーブルの複合主キーとも整合する）。
 */
data class Book(
    val id: Long?,
    val title: String,
    val price: Int,
    val publicationStatus: PublicationStatus,
    val authorIds: Set<Long>,
) {
    init {
        require(title.length <= MAX_TITLE_LENGTH) { "タイトルは${MAX_TITLE_LENGTH}文字以内である必要があります: ${title.length}" }
        require(price in 0..MAX_PRICE) { "価格は0以上${MAX_PRICE}以下である必要があります: $price" }
        require(authorIds.isNotEmpty()) { "書籍は最低1人の著者を持つ必要があります" }
        require(authorIds.size <= MAX_AUTHOR_COUNT) { "著者は${MAX_AUTHOR_COUNT}人以内である必要があります: ${authorIds.size}" }
    }

    /** 既存書籍へ新しい値を適用する。出版状況の不正な遷移は [BusinessRuleViolationException]。 */
    fun applyUpdate(
        title: String,
        price: Int,
        publicationStatus: PublicationStatus,
        authorIds: Set<Long>,
    ): Book {
        if (!this.publicationStatus.canTransitionTo(publicationStatus)) {
            throw BusinessRuleViolationException("出版済みの書籍を未出版に戻すことはできません")
        }
        return copy(title = title, price = price, publicationStatus = publicationStatus, authorIds = authorIds)
    }

    companion object {
        // 上限値はドメイン上の絶対的な定義ではなく、異常入力を弾くための現実的なガードレール。
        const val MAX_TITLE_LENGTH = 100
        const val MAX_PRICE = 1_000_000
        const val MAX_AUTHOR_COUNT = 100
    }
}
