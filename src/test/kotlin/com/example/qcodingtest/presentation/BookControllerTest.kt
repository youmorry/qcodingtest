package com.example.qcodingtest.presentation

import com.example.qcodingtest.application.BookService
import com.example.qcodingtest.application.NotFoundException
import com.example.qcodingtest.application.dto.BookCommand
import com.example.qcodingtest.domain.BusinessRuleViolationException
import com.example.qcodingtest.domain.book.Book
import com.example.qcodingtest.domain.book.BookWithAuthors
import com.example.qcodingtest.domain.book.PublicationStatus
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(BookController::class)
class BookControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var bookService: BookService

    @Test
    @DisplayName("登録成功時は 201 とマッピング済みのボディを返す")
    fun `should return 201 with the mapped body when registration succeeds`() {
        val command = BookCommand("新刊", 1000, PublicationStatus.UNPUBLISHED, setOf(1L, 2L))
        val saved = BookWithAuthors(Book(100L, "新刊", 1000, PublicationStatus.UNPUBLISHED), setOf(1L, 2L))
        given(bookService.register(command)).willReturn(saved)

        mockMvc
            .perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"title":"新刊","price":1000,"publicationStatus":"UNPUBLISHED","authorIds":[1,2]}"""),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(100))
            .andExpect(jsonPath("$.title").value("新刊"))
            .andExpect(jsonPath("$.price").value(1000))
            .andExpect(jsonPath("$.publicationStatus").value("UNPUBLISHED"))
            .andExpect(jsonPath("$.authorIds.length()").value(2))
    }

    @Test
    @DisplayName("登録でサービスが NotFoundException をスローしたら 404 にマッピングする")
    fun `should map a NotFoundException from the service to 404 on registration`() {
        val command = BookCommand("新刊", 1000, PublicationStatus.UNPUBLISHED, setOf(1L))
        given(bookService.register(command)).willThrow(NotFoundException("not found"))

        mockMvc
            .perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"title":"新刊","price":1000,"publicationStatus":"UNPUBLISHED","authorIds":[1]}"""),
            ).andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("更新成功時は 200 とマッピング済みのボディを返す")
    fun `should return 200 with the mapped body when update succeeds`() {
        val command = BookCommand("改訂版", 2000, PublicationStatus.PUBLISHED, setOf(1L))
        val saved = BookWithAuthors(Book(1L, "改訂版", 2000, PublicationStatus.PUBLISHED), setOf(1L))
        given(bookService.update(1L, command)).willReturn(saved)

        mockMvc
            .perform(
                put("/api/books/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"title":"改訂版","price":2000,"publicationStatus":"PUBLISHED","authorIds":[1]}"""),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("改訂版"))
            .andExpect(jsonPath("$.price").value(2000))
            .andExpect(jsonPath("$.publicationStatus").value("PUBLISHED"))
            .andExpect(jsonPath("$.authorIds.length()").value(1))
    }

    @Test
    @DisplayName("更新でサービスが NotFoundException をスローしたら 404 にマッピングする")
    fun `should map a NotFoundException from the service to 404 on update`() {
        val command = BookCommand("改訂版", 2000, PublicationStatus.PUBLISHED, setOf(1L))
        given(bookService.update(99L, command)).willThrow(NotFoundException("not found"))

        mockMvc
            .perform(
                put("/api/books/99")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"title":"改訂版","price":2000,"publicationStatus":"PUBLISHED","authorIds":[1]}"""),
            ).andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("更新でサービスが BusinessRuleViolationException をスローしたら 422 にマッピングする")
    fun `should map a BusinessRuleViolationException from the service to 422 on update`() {
        val command = BookCommand("改訂版", 2000, PublicationStatus.PUBLISHED, setOf(1L))
        given(bookService.update(1L, command)).willThrow(BusinessRuleViolationException("business rule violation"))

        mockMvc
            .perform(
                put("/api/books/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"title":"改訂版","price":2000,"publicationStatus":"PUBLISHED","authorIds":[1]}"""),
            ).andExpect(status().isUnprocessableContent)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            // title 空（@Size min=1）
            """{"title":"","price":1000,"publicationStatus":"UNPUBLISHED","authorIds":[1]}""",
            // 価格が負数（@Min=0）
            """{"title":"本","price":-1,"publicationStatus":"UNPUBLISHED","authorIds":[1]}""",
            // 価格が上限超過（@Max=1000000）
            """{"title":"本","price":1000001,"publicationStatus":"UNPUBLISHED","authorIds":[1]}""",
            // 著者 0 人（@Size min=1）
            """{"title":"本","price":1000,"publicationStatus":"UNPUBLISHED","authorIds":[]}""",
        ],
    )
    @DisplayName("ボディの入力バリデーションに違反した場合は 400 を返す")
    fun `should return 400 and not call the service when input violates the schema constraints`(body: String) {
        mockMvc
            .perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest)

        verifyNoInteractions(bookService)
    }

    @Test
    @DisplayName("パスパラメータ bookId が数値でない場合は 400 を返す")
    fun `should return 400 when the path bookId is not a number`() {
        mockMvc
            .perform(
                put("/api/books/abc")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"title":"改訂版","price":2000,"publicationStatus":"PUBLISHED","authorIds":[1]}"""),
            ).andExpect(status().isBadRequest)

        verifyNoInteractions(bookService)
    }
}
