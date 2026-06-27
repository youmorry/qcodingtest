package com.example.qcodingtest.domain.book

/** 書籍の永続化を担うリポジトリ。 */
interface BookRepository {
    /** 書籍を新規登録し、著者との紐付けもあわせて永続化して、採番された ID を持つ [Book] を返す。 */
    fun create(book: Book): Book

    /** 既存の書籍を全置換（PUT）で更新する。著者リンクも与えられた集合で置き換える。 */
    fun update(book: Book)
}
