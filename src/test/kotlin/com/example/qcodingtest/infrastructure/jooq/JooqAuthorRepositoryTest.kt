package com.example.qcodingtest.infrastructure.jooq

import com.example.qcodingtest.domain.author.Author
import com.example.qcodingtest.domain.author.AuthorRepository
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@JooqTest
@Import(JooqAuthorRepository::class)
class JooqAuthorRepositoryTest
    @Autowired
    constructor(
        private val authorRepository: AuthorRepository,
        private val create: DSLContext,
    ) : AbstractRepositoryTest() {
        @Test
        @DisplayName("著者を永続化し、採番済みIDを付与して返す")
        fun `should persist the author and return it with a generated id`() {
            val birthDate = LocalDate.of(1990, 5, 20)

            val created = authorRepository.save(Author(id = null, name = "テスト太郎", birthDate = birthDate))

            val authorId = assertNotNull(created.id, "ID が採番されること")
            assertEquals("テスト太郎", created.name)
            assertEquals(birthDate, created.birthDate)

            val record = assertNotNull(create.selectFrom(AUTHORS).where(AUTHORS.ID.eq(authorId)).fetchOne())
            assertEquals("テスト太郎", record.name)
            assertEquals(birthDate, record.birthDate)
        }

        @Test
        @DisplayName("更新時に著者の全項目を置き換える")
        fun `should replace all author fields when updated`() {
            val created =
                authorRepository.save(Author(id = null, name = "旧名前", birthDate = LocalDate.of(1980, 1, 1)))
            val authorId = assertNotNull(created.id)

            authorRepository.save(created.copy(name = "新名前", birthDate = LocalDate.of(1985, 6, 15)))

            val record = create.selectFrom(AUTHORS).where(AUTHORS.ID.eq(authorId)).fetchOne()
            assertNotNull(record)
            assertEquals("新名前", record.name)
            assertEquals(LocalDate.of(1985, 6, 15), record.birthDate)
        }

        @Test
        @DisplayName("存在する著者を返す")
        fun `should return the author when it exists`() {
            val created =
                authorRepository.save(Author(id = null, name = "検索対象", birthDate = LocalDate.of(1975, 3, 10)))
            val authorId = assertNotNull(created.id)

            val result = authorRepository.findById(authorId)

            assertTrue(result.isPresent)
            assertEquals(created.copy(id = authorId), result.get())
        }

        @Test
        @DisplayName("存在しない著者IDの取得は空で返す")
        fun `should return empty when the author does not exist`() {
            val result = authorRepository.findById(-1L)

            assertTrue(result.isEmpty)
        }

        @Test
        @DisplayName("指定した著者がすべて存在すれば true を返す")
        fun `should return true when all given authors exist`() {
            val id1 = saveAuthor("著者1")
            val id2 = saveAuthor("著者2")

            assertTrue(authorRepository.existsAllByIds(setOf(id1, id2)))
        }

        @Test
        @DisplayName("指定した著者のいずれかが存在しなければ false を返す")
        fun `should return false when any given author is missing`() {
            val id1 = saveAuthor("著者1")

            assertFalse(authorRepository.existsAllByIds(setOf(id1, -1L)))
        }

        @Test
        @DisplayName("著者に紐づく書籍を返す")
        fun `should return books linked to the author`() {
            val authorId = saveAuthor("著者A")
            val bookId = insertBook("テスト書籍")
            create
                .insertInto(AUTHOR_BOOKS)
                .set(AUTHOR_BOOKS.AUTHOR_ID, authorId)
                .set(AUTHOR_BOOKS.BOOK_ID, bookId)
                .execute()

            val books = authorRepository.findBooksById(authorId)

            assertEquals(1, books.size)
            assertEquals(bookId, books[0].id)
            assertEquals("テスト書籍", books[0].title)
            assertEquals(1500, books[0].price)
            assertEquals(PublicationStatus.PUBLISHED, books[0].publicationStatus)
        }

        @Test
        @DisplayName("著者に紐づく書籍がなければ空リストを返す")
        fun `should return empty list when the author has no books`() {
            val authorId = saveAuthor("著者B")

            val books = authorRepository.findBooksById(authorId)

            assertTrue(books.isEmpty())
        }

        private fun saveAuthor(name: String): Long =
            authorRepository.save(Author(id = null, name = name, birthDate = LocalDate.of(1980, 1, 1))).id!!

        private fun insertBook(
            title: String,
            price: Int = 1500,
            status: String = "PUBLISHED",
        ): Long =
            create
                .insertInto(BOOKS)
                .set(BOOKS.TITLE, title)
                .set(BOOKS.PRICE, price)
                .set(BOOKS.PUBLICATION_STATUS, status)
                .returning(BOOKS.ID)
                .fetchSingle()
                .id!!
    }
