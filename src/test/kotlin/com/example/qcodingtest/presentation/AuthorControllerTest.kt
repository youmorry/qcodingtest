package com.example.qcodingtest.presentation

import com.example.qcodingtest.application.AuthorService
import com.example.qcodingtest.application.NotFoundException
import com.example.qcodingtest.application.dto.AuthorCommand
import com.example.qcodingtest.domain.author.Author
import com.example.qcodingtest.domain.book.BookView
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate

@WebMvcTest(AuthorController::class)
class AuthorControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var authorService: AuthorService

    @Test
    @DisplayName("登録成功時は 201 とマッピング済みのボディを返す")
    fun `should return 201 with the mapped body when registration succeeds`() {
        val command = AuthorCommand("夏目漱石", LocalDate.of(1867, 2, 9))
        val saved = Author(100L, "夏目漱石", LocalDate.of(1867, 2, 9))
        given(authorService.register(command)).willReturn(saved)

        mockMvc
            .perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"夏目漱石","birthDate":"1867-02-09"}"""),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(100))
            .andExpect(jsonPath("$.name").value("夏目漱石"))
            .andExpect(jsonPath("$.birthDate").value("1867-02-09"))
    }

    @Test
    @DisplayName("更新成功時は 200 とマッピング済みのボディを返す")
    fun `should return 200 with the mapped body when update succeeds`() {
        val command = AuthorCommand("森鴎外", LocalDate.of(1862, 2, 17))
        val saved = Author(1L, "森鴎外", LocalDate.of(1862, 2, 17))
        given(authorService.update(1L, command)).willReturn(saved)

        mockMvc
            .perform(
                put("/api/authors/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"森鴎外","birthDate":"1862-02-17"}"""),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("森鴎外"))
            .andExpect(jsonPath("$.birthDate").value("1862-02-17"))
    }

    @Test
    @DisplayName("更新でサービスが NotFoundException をスローしたら 404 を返す")
    fun `should map a NotFoundException from the service to 404 on update`() {
        val command = AuthorCommand("森鴎外", LocalDate.of(1862, 2, 17))
        given(authorService.update(99L, command)).willThrow(NotFoundException("not found"))

        mockMvc
            .perform(
                put("/api/authors/99")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"森鴎外","birthDate":"1862-02-17"}"""),
            ).andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("書籍取得で成功時は 200 とマッピング済みのボディを返す")
    fun `should return 200 with the mapped book list when fetching author books succeeds`() {
        given(authorService.findBooks(1L)).willReturn(
            listOf(BookView(10L, "こころ", 800, PublicationStatus.PUBLISHED)),
        )

        mockMvc
            .perform(get("/api/authors/1/books"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(10))
            .andExpect(jsonPath("$[0].title").value("こころ"))
            .andExpect(jsonPath("$[0].price").value(800))
            .andExpect(jsonPath("$[0].publicationStatus").value("PUBLISHED"))
    }

    @Test
    @DisplayName("書籍取得でサービスが NotFoundException をスローしたら 404 を返す")
    fun `should map a NotFoundException from the service to 404 when fetching author books`() {
        given(authorService.findBooks(99L)).willThrow(NotFoundException("not found"))

        mockMvc
            .perform(get("/api/authors/99/books"))
            .andExpect(status().isNotFound)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            // name 空（@Size min=1）
            """{"name":"","birthDate":"1867-02-09"}""",
            // birthDate が日付として解釈できない
            """{"name":"夏目漱石","birthDate":"not-a-date"}""",
            // birthDate が未来日（@PastOrPresent）
            """{"name":"夏目漱石","birthDate":"2999-12-31"}""",
        ],
    )
    @DisplayName("ボディの入力バリデーションに違反した場合は 400 を返す")
    fun `should return 400 and not call the service when input violates the schema constraints`(body: String) {
        mockMvc
            .perform(post("/api/authors").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest)

        verifyNoInteractions(authorService)
    }

    @Test
    @DisplayName("パスパラメータ authorId が数値でない場合は 400 を返す")
    fun `should return 400 when the path authorId is not a number`() {
        mockMvc
            .perform(
                put("/api/authors/abc")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"森鴎外","birthDate":"1862-02-17"}"""),
            ).andExpect(status().isBadRequest)

        verifyNoInteractions(authorService)
    }
}
