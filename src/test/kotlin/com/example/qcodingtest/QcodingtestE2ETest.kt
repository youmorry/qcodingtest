package com.example.qcodingtest

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.RequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

/**
 * 代表的なパターンでの E2E テスト。
 *
 * [AbstractIntegrationTest] を継承し、Controller → Service → Repository → 実 DB の全層を
 * MockMvc 経由で検証する。`@Transactional` により各テスト後に自動ロールバックし、共有コンテナを汚さない
 * （MockMvc は同一スレッドで動くためトランザクションが伝播する、HTTP経由のテストだとトランザクションが伝播しない）
 * ）。
 */
@AutoConfigureMockMvc
@Transactional
class QcodingtestE2ETest
    @Autowired
    constructor(
        private val mockMvc: MockMvc,
        private val objectMapper: ObjectMapper,
    ) : AbstractIntegrationTest() {
        @Test
        @DisplayName("著者登録→書籍登録→更新→著者の書籍一覧、までの正常系を全層で通す")
        fun `should register an author, register a book for them, update it, and list it under the author`() {
            val authorId =
                createAndExtractId(
                    post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"夏目漱石","birthDate":"1867-02-09"}"""),
                )

            val bookId =
                createAndExtractId(
                    post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """{"title":"こころ","price":800,"publicationStatus":"UNPUBLISHED","authorIds":[$authorId]}""",
                        ),
                )

            mockMvc
                .perform(
                    put("/api/books/$bookId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """{"title":"こころ","price":800,"publicationStatus":"PUBLISHED","authorIds":[$authorId]}""",
                        ),
                ).andExpect(status().isOk)

            mockMvc
                .perform(get("/api/authors/$authorId/books"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(bookId))
                .andExpect(jsonPath("$[0].title").value("こころ"))
                .andExpect(jsonPath("$[0].price").value(800))
                .andExpect(jsonPath("$[0].publicationStatus").value("PUBLISHED"))
        }

        @Test
        @DisplayName("出版済みの書籍を未出版へ戻す更新は 422 で拒否する")
        fun `should reject reverting a published book to unpublished`() {
            val authorId =
                createAndExtractId(
                    post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"森鴎外","birthDate":"1862-02-17"}"""),
                )

            val bookId =
                createAndExtractId(
                    post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """{"title":"舞姫","price":1200,"publicationStatus":"PUBLISHED","authorIds":[$authorId]}""",
                        ),
                )

            mockMvc
                .perform(
                    put("/api/books/$bookId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """{"title":"舞姫","price":1200,"publicationStatus":"UNPUBLISHED","authorIds":[$authorId]}""",
                        ),
                ).andExpect(status().isUnprocessableContent)
        }

        @Test
        @DisplayName("存在しない著者IDで書籍を登録すると 404 を返す")
        fun `should return 404 when registering a book with a non-existent author`() {
            mockMvc
                .perform(
                    post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """{"title":"幻の本","price":1000,"publicationStatus":"UNPUBLISHED","authorIds":[999999]}""",
                        ),
                ).andExpect(status().isNotFound)
        }

        /** 作成系リクエストを実行し、201 を確認したうえでレスポンスボディの採番 `id` を返す。 */
        private fun createAndExtractId(request: RequestBuilder): Long {
            val body =
                mockMvc
                    .perform(request)
                    .andExpect(status().isCreated)
                    .andReturn()
                    .response.contentAsString
            return objectMapper.readTree(body).get("id").asLong()
        }
    }
