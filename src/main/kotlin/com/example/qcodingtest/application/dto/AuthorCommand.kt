package com.example.qcodingtest.application.dto

import java.time.LocalDate

data class AuthorCommand(
    val name: String,
    val birthDate: LocalDate,
)
