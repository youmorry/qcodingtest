package com.example.qcodingtest.presentation

import com.example.qcodingtest.application.BookService
import com.example.qcodingtest.application.dto.BookCommand
import com.example.qcodingtest.domain.book.BookWithAuthors
import com.example.qcodingtest.domain.book.PublicationStatus
import com.example.qcodingtest.presentation.api.BooksApi
import com.example.qcodingtest.presentation.model.BookRequest
import com.example.qcodingtest.presentation.model.BookResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import com.example.qcodingtest.presentation.model.PublicationStatus as ApiPublicationStatus

/**
 * 書籍 API。OpenAPI から生成した [BooksApi] を実装する。
 */
@RestController
class BookController(
    private val bookService: BookService,
) : BooksApi {
    override fun registerBook(bookRequest: BookRequest): ResponseEntity<BookResponse> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(bookService.register(bookRequest.toCommand()).toResponse())

    override fun updateBook(
        bookId: Long,
        bookRequest: BookRequest,
    ): ResponseEntity<BookResponse> = ResponseEntity.ok(bookService.update(bookId, bookRequest.toCommand()).toResponse())
}

private fun BookRequest.toCommand(): BookCommand =
    BookCommand(
        title = title,
        price = price,
        publicationStatus = PublicationStatus.valueOf(publicationStatus.name),
        authorIds = authorIds,
    )

private fun BookWithAuthors.toResponse(): BookResponse =
    BookResponse(
        // 保存済みの書籍は必ず採番済みのため非 null。
        id = requireNotNull(book.id),
        title = book.title,
        price = book.price,
        publicationStatus = ApiPublicationStatus.valueOf(book.publicationStatus.name),
        authorIds = authorIds,
    )
