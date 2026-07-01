package com.example.qcodingtest.presentation

import com.example.qcodingtest.application.NotFoundException
import com.example.qcodingtest.domain.BusinessRuleViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * 独自例外をステータスへ対応付けるのみとする。入力形式違反（400）は Spring 標準の例外解決に委ね、
 * ボディは `spring.mvc.problemdetails.enabled` による標準の ProblemDetail 表現に任せる。
 */
@RestControllerAdvice
class ApiExceptionHandler {
    /**
     * リソースが見つからないケース（404）を処理する。
     */
    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(e: NotFoundException): ProblemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message.orEmpty())

    /**
     * ビジネスルール違反（422）を処理する。
     */
    @ExceptionHandler(BusinessRuleViolationException::class)
    fun handleBusinessRuleViolation(e: BusinessRuleViolationException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.message.orEmpty())
}
