package com.example.qcodingtest.infrastructure.jooq

import com.example.qcodingtest.domain.author.Author
import com.example.qcodingtest.domain.author.AuthorRepository
import com.example.qcodingtest.domain.book.BookView
import com.example.qcodingtest.domain.book.PublicationStatus
import com.example.qcodingtest.jooq.tables.references.AUTHORS
import com.example.qcodingtest.jooq.tables.references.AUTHOR_BOOKS
import com.example.qcodingtest.jooq.tables.references.BOOKS
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
class JooqAuthorRepository(
    private val create: DSLContext,
) : AuthorRepository {
    override fun save(author: Author): Author = if (author.id == null) create(author) else update(author)

    private fun create(author: Author): Author {
        val authorId =
            create
                .insertInto(AUTHORS)
                .set(AUTHORS.NAME, author.name)
                .set(AUTHORS.BIRTH_DATE, author.birthDate)
                .returning(AUTHORS.ID)
                .fetchSingle()
                .id
        return author.copy(id = authorId)
    }

    private fun update(author: Author): Author {
        create
            .update(AUTHORS)
            .set(AUTHORS.NAME, author.name)
            .set(AUTHORS.BIRTH_DATE, author.birthDate)
            .set(AUTHORS.UPDATED_AT, DSL.currentOffsetDateTime())
            .where(AUTHORS.ID.eq(author.id))
            .execute()
        return author
    }

    override fun findById(id: Long): Optional<Author> =
        Optional.ofNullable(
            create
                .selectFrom(AUTHORS)
                .where(AUTHORS.ID.eq(id))
                .fetchOne()
                ?.let { Author(id = it.id, name = it.name, birthDate = it.birthDate) },
        )

    override fun existsAllByIds(ids: Set<Long>): Boolean = create.fetchCount(AUTHORS, AUTHORS.ID.`in`(ids)) == ids.size

    override fun findBooksById(id: Long): List<BookView> =
        create
            .select(BOOKS.ID, BOOKS.TITLE, BOOKS.PRICE, BOOKS.PUBLICATION_STATUS)
            .from(BOOKS)
            .join(AUTHOR_BOOKS)
            .on(AUTHOR_BOOKS.BOOK_ID.eq(BOOKS.ID))
            .where(AUTHOR_BOOKS.AUTHOR_ID.eq(id))
            .fetch()
            .map { row ->
                BookView(
                    id = requireNotNull(row[BOOKS.ID]),
                    title = requireNotNull(row[BOOKS.TITLE]),
                    price = requireNotNull(row[BOOKS.PRICE]),
                    publicationStatus = PublicationStatus.valueOf(requireNotNull(row[BOOKS.PUBLICATION_STATUS])),
                )
            }
}
