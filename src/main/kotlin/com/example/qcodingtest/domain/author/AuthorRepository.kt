package com.example.qcodingtest.domain.author

import com.example.qcodingtest.domain.book.BookView
import java.util.Optional

/** 著者の永続化を担うリポジトリ。 */
interface AuthorRepository {
    /** 著者を新規登録し、採番された ID を持つ [Author] を返す。 */
    fun create(author: Author): Author

    /** 既存の著者を全置換（PUT）で更新する。 */
    fun update(author: Author)

    /** 指定 ID の著者を返す。 */
    fun findById(id: Long): Optional<Author>

    /** 指定著者 ID に紐づく書籍一覧を返す。 */
    fun findBooksById(id: Long): List<BookView>
}
