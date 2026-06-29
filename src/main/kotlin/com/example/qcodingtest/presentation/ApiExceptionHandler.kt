package com.example.qcodingtest.presentation

import com.example.qcodingtest.application.NotFoundException
import com.example.qcodingtest.domain.BusinessRuleViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

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

    /**
     * 入力値の制約違反（400）を処理する。
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "入力値が不正です").apply {
            setProperty(
                "errors",
                e.bindingResult.fieldErrors.groupBy({ it.field }, { it.defaultMessage ?: "invalid" }),
            )
        }

    /**
     * リクエストボディを解釈できないケース（400）を処理する。
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableBody(e: HttpMessageNotReadableException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "リクエストボディを解釈できません")

    /**
     * パスパラメータの型不一致（400）を処理する。
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(e: MethodArgumentTypeMismatchException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "パラメータ '${e.name}' の形式が不正です")
}
