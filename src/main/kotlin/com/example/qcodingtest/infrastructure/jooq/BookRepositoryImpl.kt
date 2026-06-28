package com.example.qcodingtest.infrastructure.jooq

import com.example.qcodingtest.domain.book.Book
import com.example.qcodingtest.domain.book.BookRepository
import com.example.qcodingtest.jooq.tables.references.AUTHOR_BOOKS
import com.example.qcodingtest.jooq.tables.references.BOOKS
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository

@Repository
class BookRepositoryImpl(
    private val create: DSLContext,
) : BookRepository {
    override fun create(book: Book): Book {
        val bookId =
            create
                .insertInto(BOOKS)
                .set(BOOKS.TITLE, book.title)
                .set(BOOKS.PRICE, book.price)
                .set(BOOKS.PUBLICATION_STATUS, book.publicationStatus.name)
                .returning(BOOKS.ID)
                .fetchSingle()
                .id

        val linkRows = book.authorIds.map { authorId -> DSL.row(authorId, bookId) }
        create
            .insertInto(AUTHOR_BOOKS, AUTHOR_BOOKS.AUTHOR_ID, AUTHOR_BOOKS.BOOK_ID)
            .valuesOfRows(linkRows)
            .execute()

        return book.copy(id = bookId)
    }

    override fun update(book: Book) {
        val bookId = requireNotNull(book.id) { "更新には永続化済みの書籍ID が必要です" }

        create
            .update(BOOKS)
            .set(BOOKS.TITLE, book.title)
            .set(BOOKS.PRICE, book.price)
            .set(BOOKS.PUBLICATION_STATUS, book.publicationStatus.name)
            .set(BOOKS.UPDATED_AT, DSL.currentOffsetDateTime())
            .where(BOOKS.ID.eq(bookId))
            .execute()

        // PUT は全置換のため、著者リンクは差分更新せず一旦すべて削除してから貼り直す。
        create
            .deleteFrom(AUTHOR_BOOKS)
            .where(AUTHOR_BOOKS.BOOK_ID.eq(bookId))
            .execute()

        val linkRows = book.authorIds.map { authorId -> DSL.row(authorId, bookId) }
        create
            .insertInto(AUTHOR_BOOKS, AUTHOR_BOOKS.AUTHOR_ID, AUTHOR_BOOKS.BOOK_ID)
            .valuesOfRows(linkRows)
            .execute()
    }
}
