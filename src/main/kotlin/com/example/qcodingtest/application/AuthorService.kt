package com.example.qcodingtest.application

import com.example.qcodingtest.application.dto.AuthorCommand
import com.example.qcodingtest.domain.author.Author
import com.example.qcodingtest.domain.author.AuthorRepository
import com.example.qcodingtest.domain.book.BookView
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 著者のアプリケーションサービス。
 */
@Service
class AuthorService(
    private val authorRepository: AuthorRepository,
) {
    /**
     * 著者を登録する。
     */
    @Transactional
    fun register(command: AuthorCommand): Author =
        authorRepository.save(
            Author(
                id = null,
                name = command.name,
                birthDate = command.birthDate,
            ),
        )

    /**
     * 著者を更新する（PUT による全置換）。
     *
     * @throws NotFoundException 著者が存在しない場合
     */
    @Transactional
    fun update(
        authorId: Long,
        command: AuthorCommand,
    ): Author {
        val existing =
            authorRepository
                .findById(authorId)
                .orElseThrow { NotFoundException("著者が見つかりません: id=$authorId") }
        return authorRepository.save(
            existing.copy(name = command.name, birthDate = command.birthDate),
        )
    }

    /**
     * 指定著者に紐づく書籍一覧を取得する。
     *
     * @throws NotFoundException 著者が存在しない場合
     */
    @Transactional(readOnly = true)
    fun findBooks(authorId: Long): List<BookView> {
        if (!authorRepository.existsAllByIds(setOf(authorId))) {
            throw NotFoundException("著者が見つかりません: id=$authorId")
        }
        return authorRepository.findBooksById(authorId)
    }
}
