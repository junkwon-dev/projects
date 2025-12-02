package com.jun.mail.controller.exceptionhandler

import com.jun.mail.application.exceptions.BadGatewayException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class CustomExceptionHandler {
    @ExceptionHandler(value = [BadGatewayException::class])
    fun onBadGatewayException(e: BadGatewayException): ResponseEntity<*> {
        return ResponseEntity(e.message, HttpStatus.BAD_GATEWAY)
    }
}