package com.example.qcodingtest.infrastructure.jooq

import com.example.qcodingtest.domain.book.Book
import com.example.qcodingtest.domain.book.BookRepository
import com.example.qcodingtest.domain.book.BookWithAuthors
import com.example.qcodingtest.domain.book.PublicationStatus
import com.example.qcodingtest.jooq.tables.references.AUTHOR_BOOKS
import com.example.qcodingtest.jooq.tables.references.BOOKS
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
class JooqBookRepository(
    private val dsl: DSLContext,
) : BookRepository {
    override fun save(bookWithAuthors: BookWithAuthors): BookWithAuthors =
        if (bookWithAuthors.book.id == null) create(bookWithAuthors) else update(bookWithAuthors)

    override fun findById(id: Long): Optional<Book> =
        dsl
            .select(BOOKS.ID, BOOKS.TITLE, BOOKS.PRICE, BOOKS.PUBLICATION_STATUS)
            .from(BOOKS)
            .where(BOOKS.ID.eq(id))
            .fetchOptional()
            .map { record ->
                Book(
                    id = record[BOOKS.ID],
                    title = requireNotNull(record[BOOKS.TITLE]),
                    price = requireNotNull(record[BOOKS.PRICE]),
                    publicationStatus = PublicationStatus.valueOf(requireNotNull(record[BOOKS.PUBLICATION_STATUS])),
                )
            }

    private fun create(bookWithAuthors: BookWithAuthors): BookWithAuthors {
        val book = bookWithAuthors.book
        val bookId =
            dsl
                .insertInto(BOOKS)
                .set(BOOKS.TITLE, book.title)
                .set(BOOKS.PRICE, book.price)
                .set(BOOKS.PUBLICATION_STATUS, book.publicationStatus.name)
                .returning(BOOKS.ID)
                .fetchSingle()
                .id

        insertAuthorLinks(bookId, bookWithAuthors.authorIds)

        return bookWithAuthors.copy(book = book.copy(id = bookId))
    }

    private fun update(bookWithAuthors: BookWithAuthors): BookWithAuthors {
        val bookId = requireNotNull(bookWithAuthors.book.id) { "更新には永続化済みの書籍ID が必要です" }

        dsl
            .update(BOOKS)
            .set(BOOKS.TITLE, bookWithAuthors.book.title)
            .set(BOOKS.PRICE, bookWithAuthors.book.price)
            .set(BOOKS.PUBLICATION_STATUS, bookWithAuthors.book.publicationStatus.name)
            .set(BOOKS.UPDATED_AT, DSL.currentOffsetDateTime())
            .where(BOOKS.ID.eq(bookId))
            .execute()

        // PUT は全置換のため、著者リンクは差分更新せず一旦すべて削除してから貼り直す。
        dsl
            .deleteFrom(AUTHOR_BOOKS)
            .where(AUTHOR_BOOKS.BOOK_ID.eq(bookId))
            .execute()
        insertAuthorLinks(bookId, bookWithAuthors.authorIds)

        return bookWithAuthors
    }

    private fun insertAuthorLinks(
        bookId: Long?,
        authorIds: Set<Long>,
    ) {
        val linkRows = authorIds.map { authorId -> DSL.row(authorId, bookId) }
        dsl
            .insertInto(AUTHOR_BOOKS, AUTHOR_BOOKS.AUTHOR_ID, AUTHOR_BOOKS.BOOK_ID)
            .valuesOfRows(linkRows)
            .execute()
    }
}
