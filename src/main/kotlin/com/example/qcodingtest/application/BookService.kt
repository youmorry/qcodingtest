package com.example.qcodingtest.application

import com.example.qcodingtest.application.dto.BookCommand
import com.example.qcodingtest.domain.author.AuthorRepository
import com.example.qcodingtest.domain.book.Book
import com.example.qcodingtest.domain.book.BookRepository
import com.example.qcodingtest.domain.book.BookWithAuthors
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 書籍のアプリケーションサービス。
 */
@Service
class BookService(
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository,
) {
    /**
     * 書籍を登録する。
     *
     * @throws NotFoundException 指定された著者が存在しない場合
     */
    @Transactional
    fun register(command: BookCommand): BookWithAuthors {
        ensureAuthorsExist(command.authorIds)
        val bookWithAuthors =
            BookWithAuthors(
                book =
                    Book(
                        id = null,
                        title = command.title,
                        price = command.price,
                        publicationStatus = command.publicationStatus,
                    ),
                authorIds = command.authorIds,
            )
        return bookRepository.save(bookWithAuthors)
    }

    /**
     * 書籍を更新する（PUT による全置換）。
     *
     * @throws NotFoundException 書籍または指定された著者が存在しない場合
     * @throws BusinessRuleViolationException 出版状況の不正な遷移の場合
     */
    @Transactional
    fun update(
        bookId: Long,
        command: BookCommand,
    ): BookWithAuthors {
        val existing =
            bookRepository
                .findById(bookId)
                .orElseThrow { NotFoundException("書籍が見つかりません: id=$bookId") }
        ensureAuthorsExist(command.authorIds)
        val book = existing.applyUpdate(command.title, command.price, command.publicationStatus)
        return bookRepository.save(BookWithAuthors(book, command.authorIds))
    }

    private fun ensureAuthorsExist(authorIds: Set<Long>) {
        if (!authorRepository.existsAllByIds(authorIds)) {
            throw NotFoundException("指定された著者が見つかりません: $authorIds")
        }
    }
}
