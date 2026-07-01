package com.example.qcodingtest.domain.author

import java.time.LocalDate

/**
 * 著者を表すドメインモデル。
 *
 * @property id 未永続化（採番前）は null。
 */
data class Author(
    val id: Long?,
    val name: String,
    val birthDate: LocalDate,
) {
    init {
        require(name.isNotEmpty()) { "著者名は空にできません" }
        require(name.length <= MAX_NAME_LENGTH) { "著者名は${MAX_NAME_LENGTH}文字以内である必要があります: ${name.length}" }
        require(!birthDate.isAfter(LocalDate.now())) { "生年月日は現在日以前である必要があります: $birthDate" }
    }

    companion object {
        // 上限値はドメイン上の絶対的な定義ではなく、異常入力を弾くための現実的なガードレール。
        const val MAX_NAME_LENGTH = 100
    }
}
