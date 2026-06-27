package com.example.qcodingtest.domain.book

/** 書籍の出版状況。enum 名を DB の publication_status(text) と一致させ、.name で相互変換する。 */
enum class PublicationStatus {
    UNPUBLISHED,
    PUBLISHED,
    ;

    /**
     * この状態から [next] への遷移を許可するか。
     *
     * 出版済み(PUBLISHED)から未出版(UNPUBLISHED)へは戻せない。それ以外（同一・未出版→出版済み）は許可する。
     */
    fun canTransitionTo(next: PublicationStatus): Boolean = !(this == PUBLISHED && next == UNPUBLISHED)
}
