package com.example.qcodingtest.infrastructure.jooq

import com.example.qcodingtest.domain.book.Book
import com.example.qcodingtest.domain.book.BookRepository
import com.example.qcodingtest.domain.book.BookWithAuthors
import com.example.qcodingtest.domain.book.PublicationStatus
import com.example.qcodingtest.jooq.tables.references.AUTHORS
import com.example.qcodingtest.jooq.tables.references.AUTHOR_BOOKS
import com.example.qcodingtest.jooq.tables.references.BOOKS
import org.jooq.DSLContext
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jooq.test.autoconfigure.JooqTest
import org.springframework.context.annotation.Import
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@JooqTest
@Import(JooqBookRepository::class)
class JooqBookRepositoryTest
    @Autowired
    constructor(
        private val bookRepository: BookRepository,
        private val create: DSLContext,
    ) : AbstractRepositoryTest() {
        @Test
        @DisplayName("書籍を著者リンクごと永続化し、採番済みIDを付与して返す")
        fun `should persist the book with its author links and return it with a generated id`() {
            val authorId1 = insertAuthor("著者A")
            val authorId2 = insertAuthor("著者B")

            val created =
                bookRepository.save(
                    BookWithAuthors(
                        book = Book(id = null, title = "テスト駆動開発", price = 2860, publicationStatus = PublicationStatus.UNPUBLISHED),
                        authorIds = setOf(authorId1, authorId2),
                    ),
                )

            val bookId = assertNotNull(created.book.id, "ID が採番されること")
            assertEquals("テスト駆動開発", created.book.title)
            assertEquals(2860, created.book.price)
            assertEquals(PublicationStatus.UNPUBLISHED, created.book.publicationStatus)
            assertEquals(setOf(authorId1, authorId2), created.authorIds)

            val bookRecord = assertNotNull(create.selectFrom(BOOKS).where(BOOKS.ID.eq(bookId)).fetchOne())
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
        @DisplayName("更新時に書籍の全項目と著者リンクを置き換える")
        fun `should replace all book fields and author links when updated`() {
            val authorId1 = insertAuthor("著者A")
            val authorId2 = insertAuthor("著者B")
            val authorId3 = insertAuthor("著者C")

            val created =
                bookRepository.save(
                    BookWithAuthors(
                        book = Book(id = null, title = "旧タイトル", price = 1000, publicationStatus = PublicationStatus.UNPUBLISHED),
                        authorIds = setOf(authorId1, authorId2),
                    ),
                )
            val bookId = assertNotNull(created.book.id)

            bookRepository.save(
                created.copy(
                    book = created.book.copy(title = "新タイトル", price = 3000, publicationStatus = PublicationStatus.PUBLISHED),
                    authorIds = setOf(authorId2, authorId3),
                ),
            )

            val bookRecord = assertNotNull(create.selectFrom(BOOKS).where(BOOKS.ID.eq(bookId)).fetchOne())
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

        @Test
        @DisplayName("存在する書籍を返す")
        fun `should return the book without authors when it exists`() {
            val authorId = insertAuthor("著者A")
            val created =
                bookRepository.save(
                    BookWithAuthors(
                        book = Book(id = null, title = "対象書籍", price = 1500, publicationStatus = PublicationStatus.PUBLISHED),
                        authorIds = setOf(authorId),
                    ),
                )
            val bookId = assertNotNull(created.book.id)
            val result = bookRepository.findById(bookId)

            assertTrue(result.isPresent)
            assertEquals(created.book, result.get())
        }

        @Test
        @DisplayName("存在しない書籍IDの取得は空で返す")
        fun `should return empty when the book does not exist`() {
            val result = bookRepository.findById(-1L)

            assertTrue(result.isEmpty)
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
