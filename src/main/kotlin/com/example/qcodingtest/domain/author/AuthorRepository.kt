package com.example.qcodingtest.domain.author

import com.example.qcodingtest.domain.book.BookView
import java.util.Optional

/** 著者の永続化を担うリポジトリ。 */
interface AuthorRepository {
    /** upsert（新規登録または更新）する。 */
    fun save(author: Author): Author

    /** 指定 ID の著者を返す。 */
    fun findById(id: Long): Optional<Author>

    /** 指定著者 ID に紐づく書籍一覧を返す。 */
    fun findBooksById(id: Long): List<BookView>
}
