package com.example.qcodingtest.domain.book

/** 書籍の永続化を担うリポジトリ。 */
interface BookRepository {
    /** upsert（新規登録または更新）する。 */
    fun save(book: Book): Book
}
