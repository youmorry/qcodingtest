package com.example.qcodingtest.application

import com.example.qcodingtest.application.dto.AuthorCommand
import com.example.qcodingtest.domain.author.Author
import com.example.qcodingtest.domain.author.AuthorRepository
import com.example.qcodingtest.domain.book.BookView
import com.example.qcodingtest.domain.book.PublicationStatus
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import java.time.LocalDate
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AuthorServiceTest {
    private val authorRepository = mock(AuthorRepository::class.java)
    private val authorService = AuthorService(authorRepository)

    @Test
    fun `should persist a new author and return it with a generated id`() {
        val command = AuthorCommand(name = "夏目漱石", birthDate = LocalDate.of(1867, 2, 9))
        val input = Author(id = null, name = command.name, birthDate = command.birthDate)
        val saved = input.copy(id = 100L)
        given(authorRepository.save(input)).willReturn(saved)

        val result = authorService.register(command = command)

        assertEquals(saved, result)
    }

    @Test
    fun `should update an author and return the new state`() {
        val existing = Author(id = 10L, name = "旧名", birthDate = LocalDate.of(1900, 1, 1))
        given(authorRepository.findById(10L)).willReturn(Optional.of(existing))
        val command = AuthorCommand(name = "新名", birthDate = LocalDate.of(1950, 6, 15))
        val expected = Author(id = 10L, name = command.name, birthDate = command.birthDate)
        given(authorRepository.save(expected)).willReturn(expected)

        val result = authorService.update(authorId = 10L, command = command)

        assertEquals(expected, result)
    }

    @Test
    fun `should reject update when the author does not exist`() {
        given(authorRepository.findById(99L)).willReturn(Optional.empty())

        assertFailsWith<NotFoundException> {
            authorService.update(
                authorId = 99L,
                command = AuthorCommand(name = "新名", birthDate = LocalDate.of(1950, 6, 15)),
            )
        }
    }

    @Test
    fun `should return the books linked to an existing author`() {
        given(authorRepository.existsAllByIds(setOf(10L))).willReturn(true)
        val books = listOf(BookView(id = 1L, title = "吾輩は猫である", price = 1000, publicationStatus = PublicationStatus.PUBLISHED))
        given(authorRepository.findBooksById(10L)).willReturn(books)

        val result = authorService.findBooks(authorId = 10L)

        assertEquals(books, result)
    }

    @Test
    fun `should reject book retrieval when the author does not exist`() {
        given(authorRepository.existsAllByIds(setOf(99L))).willReturn(false)

        assertFailsWith<NotFoundException> {
            authorService.findBooks(authorId = 99L)
        }
        verify(authorRepository, never()).findBooksById(99L)
    }
}
