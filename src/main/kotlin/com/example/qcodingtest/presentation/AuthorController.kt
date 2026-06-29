package com.example.qcodingtest.presentation

import com.example.qcodingtest.application.AuthorService
import com.example.qcodingtest.application.dto.AuthorCommand
import com.example.qcodingtest.domain.author.Author
import com.example.qcodingtest.domain.book.BookView
import com.example.qcodingtest.presentation.api.AuthorsApi
import com.example.qcodingtest.presentation.model.AuthorBookResponse
import com.example.qcodingtest.presentation.model.AuthorRequest
import com.example.qcodingtest.presentation.model.AuthorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import com.example.qcodingtest.presentation.model.PublicationStatus as ApiPublicationStatus

/**
 * 著者 API。OpenAPI から生成した [AuthorsApi] を実装する。
 */
@RestController
class AuthorController(
    private val authorService: AuthorService,
) : AuthorsApi {
    override fun registerAuthor(authorRequest: AuthorRequest): ResponseEntity<AuthorResponse> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(authorService.register(authorRequest.toCommand()).toResponse())

    override fun updateAuthor(
        authorId: Long,
        authorRequest: AuthorRequest,
    ): ResponseEntity<AuthorResponse> = ResponseEntity.ok(authorService.update(authorId, authorRequest.toCommand()).toResponse())

    override fun getAuthorBooks(authorId: Long): ResponseEntity<List<AuthorBookResponse>> =
        ResponseEntity.ok(authorService.findBooks(authorId).map { it.toResponse() })
}

private fun AuthorRequest.toCommand(): AuthorCommand =
    AuthorCommand(
        name = name,
        birthDate = birthDate,
    )

private fun Author.toResponse(): AuthorResponse =
    AuthorResponse(
        // 保存済みの著者は必ず採番済みのため非 null。
        id = checkNotNull(id),
        name = name,
        birthDate = birthDate,
    )

private fun BookView.toResponse(): AuthorBookResponse =
    AuthorBookResponse(
        id = id,
        title = title,
        price = price,
        publicationStatus = ApiPublicationStatus.valueOf(publicationStatus.name),
    )
