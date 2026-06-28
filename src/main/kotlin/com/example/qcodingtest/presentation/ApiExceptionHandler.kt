package com.example.qcodingtest.presentation

import com.example.qcodingtest.application.NotFoundException
import com.example.qcodingtest.domain.BusinessRuleViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(e: NotFoundException): ProblemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message.orEmpty())

    @ExceptionHandler(BusinessRuleViolationException::class)
    fun handleBusinessRuleViolation(e: BusinessRuleViolationException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.message.orEmpty())
}
