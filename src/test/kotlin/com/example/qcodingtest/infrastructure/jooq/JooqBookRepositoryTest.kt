package com.example.qcodingtest.infrastructure.jooq

import com.example.qcodingtest.domain.book.Book
import com.example.qcodingtest.domain.book.BookRepository
import com.example.qcodingtest.domain.book.PublicationStatus
import com.example.qcodingtest.jooq.tables.references.AUTHORS
import com.example.qcodingtest.jooq.tables.references.AUTHOR_BOOKS
import com.example.qcodingtest.jooq.tables.references.BOOKS
import org.jooq.DSLContext
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jooq.test.autoconfigure.JooqTest
import org.springframework.context.annotation.Import
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@JooqTest
@Import(JooqBookRepository::class)
class JooqBookRepositoryTest
    @Autowired
    constructor(
        private val bookRepository: BookRepository,
        private val create: DSLContext,
    ) : AbstractRepositoryTest() {
        @Test
        fun `should persist the book with its author links and return it with a generated id`() {
            val authorId1 = insertAuthor("著者A")
            val authorId2 = insertAuthor("著者B")

            val created =
                bookRepository.save(
                    Book(
                        id = null,
                        title = "テスト駆動開発",
                        price = 2860,
                        publicationStatus = PublicationStatus.UNPUBLISHED,
                        authorIds = setOf(authorId1, authorId2),
                    ),
                )

            val bookId = assertNotNull(created.id, "ID が採番されること")
            assertEquals("テスト駆動開発", created.title)
            assertEquals(2860, created.price)
            assertEquals(PublicationStatus.UNPUBLISHED, created.publicationStatus)
            assertEquals(setOf(authorId1, authorId2), created.authorIds)

            val bookRecord = create.selectFrom(BOOKS).where(BOOKS.ID.eq(bookId)).fetchOne()
            assertNotNull(bookRecord, "bookが永続化されること")
            assertEquals("テスト駆動開発", bookRecord.title)
            assertEquals(2860, bookRecord.price)
            assertEquals(PublicationStatus.UNPUBLISHED.name, bookRecord.publicationStatus)

            val linkedAuthorIds =
                create
                    .select(AUTHOR_BOOKS.AUTHOR_ID)
                    .from(AUTHOR_BOOKS)
                    .where(AUTHOR_BOOKS.BOOK_ID.eq(bookId))
                    .fetch(AUTHOR_BOOKS.AUTHOR_ID)
            assertEquals(setOf(authorId1, authorId2), linkedAuthorIds.toSet())
        }

        @Test
        fun `should replace all book fields and author links when updated`() {
            val authorId1 = insertAuthor("著者A")
            val authorId2 = insertAuthor("著者B")
            val authorId3 = insertAuthor("著者C")

            val created =
                bookRepository.save(
                    Book(
                        id = null,
                        title = "旧タイトル",
                        price = 1000,
                        publicationStatus = PublicationStatus.UNPUBLISHED,
                        authorIds = setOf(authorId1, authorId2),
                    ),
                )
            val bookId = assertNotNull(created.id)

            val updated =
                bookRepository.save(
                    created.copy(
                        title = "新タイトル",
                        price = 3000,
                        publicationStatus = PublicationStatus.PUBLISHED,
                        authorIds = setOf(authorId2, authorId3),
                    ),
                )

            val bookRecord = create.selectFrom(BOOKS).where(BOOKS.ID.eq(bookId)).fetchOne()
            assertNotNull(bookRecord, "bookが残っていること")
            assertEquals("新タイトル", bookRecord.title)
            assertEquals(3000, bookRecord.price)
            assertEquals(PublicationStatus.PUBLISHED.name, bookRecord.publicationStatus)

            val linkedAuthorIds =
                create
                    .select(AUTHOR_BOOKS.AUTHOR_ID)
                    .from(AUTHOR_BOOKS)
                    .where(AUTHOR_BOOKS.BOOK_ID.eq(bookId))
                    .fetch(AUTHOR_BOOKS.AUTHOR_ID)
            assertEquals(setOf(authorId2, authorId3), linkedAuthorIds.toSet(), "著者リンクが新しい集合で置き換わること")
        }

        private fun insertAuthor(name: String): Long =
            create
                .insertInto(AUTHORS)
                .set(AUTHORS.NAME, name)
                .set(AUTHORS.BIRTH_DATE, LocalDate.of(1980, 1, 1))
                .returning(AUTHORS.ID)
                .fetchOne()!!
                .id!!
    }
