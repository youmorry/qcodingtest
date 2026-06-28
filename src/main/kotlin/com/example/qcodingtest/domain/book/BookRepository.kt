package com.example.qcodingtest.domain.book

import java.util.Optional

/** 書籍の永続化を担うリポジトリ。 */
interface BookRepository {
    /** 書籍とその著者リンクを upsert（新規登録または更新）する。 */
    fun save(bookWithAuthors: BookWithAuthors): BookWithAuthors

    /** 指定 ID の書籍（著者は含まない）を返す。 */
    fun findById(id: Long): Optional<Book>
}
