package com.example.qcodingtest.application

import com.example.qcodingtest.application.dto.BookCommand
import com.example.qcodingtest.domain.BusinessRuleViolationException
import com.example.qcodingtest.domain.author.AuthorRepository
import com.example.qcodingtest.domain.book.Book
import com.example.qcodingtest.domain.book.BookRepository
import com.example.qcodingtest.domain.book.BookWithAuthors
import com.example.qcodingtest.domain.book.PublicationStatus
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BookServiceTest {
    private val bookRepository = mock(BookRepository::class.java)
    private val authorRepository = mock(AuthorRepository::class.java)
    private val bookService = BookService(bookRepository, authorRepository)

    private fun updateCommand(publicationStatus: PublicationStatus) =
        BookCommand(title = "改訂版", price = 2000, publicationStatus = publicationStatus, authorIds = setOf(1L))

    @Test
    @DisplayName("書籍を新規登録し、採番済みIDを付与して返す")
    fun `should persist a new book and return it with a generated id`() {
        given(authorRepository.existsAllByIds(setOf(1L))).willReturn(true)
        val command = BookCommand(title = "新刊", price = 1000, publicationStatus = PublicationStatus.UNPUBLISHED, authorIds = setOf(1L))
        val input =
            BookWithAuthors(
                book = Book(id = null, title = command.title, price = command.price, publicationStatus = command.publicationStatus),
                authorIds = command.authorIds,
            )
        val saved = input.copy(book = input.book.copy(id = 100L))
        given(bookRepository.save(input)).willReturn(saved)

        val result = bookService.register(command = command)

        assertEquals(saved, result)
    }

    @Test
    @DisplayName("指定された著者が存在しない場合は登録を弾き、リポジトリを呼ばない")
    fun `should reject registration when a referenced author does not exist`() {
        given(authorRepository.existsAllByIds(setOf(1L, 2L))).willReturn(false)

        assertFailsWith<NotFoundException> {
            bookService.register(
                BookCommand(
                    title = "新刊",
                    price = 1000,
                    publicationStatus = PublicationStatus.UNPUBLISHED,
                    authorIds = setOf(1L, 2L),
                ),
            )
        }
        verifyNoInteractions(bookRepository)
    }

    @Test
    @DisplayName("書籍を更新し、更新後の状態を返す")
    fun `should update a book and return the new state`() {
        val existing = Book(id = 10L, title = "旧", price = 1000, publicationStatus = PublicationStatus.UNPUBLISHED)
        given(bookRepository.findById(10L)).willReturn(Optional.of(existing))
        given(authorRepository.existsAllByIds(setOf(1L))).willReturn(true)
        val command = updateCommand(PublicationStatus.PUBLISHED)
        val updated =
            BookWithAuthors(
                book = Book(id = 10L, title = command.title, price = command.price, publicationStatus = command.publicationStatus),
                authorIds = setOf(1L),
            )
        given(bookRepository.save(updated)).willReturn(updated)

        val result = bookService.update(bookId = 10L, command = command)

        assertEquals(updated, result)
    }

    @Test
    @DisplayName("更新対象の書籍が存在しない場合は弾く")
    fun `should reject update when the book does not exist`() {
        given(bookRepository.findById(99L)).willReturn(Optional.empty())

        assertFailsWith<NotFoundException> {
            bookService.update(bookId = 99L, command = updateCommand(PublicationStatus.PUBLISHED))
        }
        verifyNoInteractions(authorRepository)
    }

    @Test
    @DisplayName("更新時に指定された著者が存在しない場合は弾く")
    fun `should reject update when a referenced author does not exist`() {
        val existing = Book(id = 10L, title = "旧", price = 1000, publicationStatus = PublicationStatus.UNPUBLISHED)
        given(bookRepository.findById(10L)).willReturn(Optional.of(existing))
        given(authorRepository.existsAllByIds(setOf(1L))).willReturn(false)

        assertFailsWith<NotFoundException> {
            bookService.update(bookId = 10L, command = updateCommand(PublicationStatus.PUBLISHED))
        }
    }

    @Test
    @DisplayName("出版済みの書籍を未出版へ戻す更新は弾く")
    fun `should reject update that turns a published book back to unpublished`() {
        val existing = Book(id = 10L, title = "旧", price = 1000, publicationStatus = PublicationStatus.PUBLISHED)
        given(bookRepository.findById(10L)).willReturn(Optional.of(existing))
        given(authorRepository.existsAllByIds(setOf(1L))).willReturn(true)

        assertFailsWith<BusinessRuleViolationException> {
            bookService.update(bookId = 10L, command = updateCommand(PublicationStatus.UNPUBLISHED))
        }
    }
}
