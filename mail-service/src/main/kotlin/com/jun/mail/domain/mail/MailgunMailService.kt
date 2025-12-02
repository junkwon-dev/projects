package com.jun.mail.domain.mail

import com.jun.mail.application.exceptions.BadGatewayException
import com.jun.mail.domain.entity.MailSentLog
import com.jun.mail.domain.entity.MailServiceType
import com.jun.mail.infrastructure.MailSentLogRepository
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service("mailgun")
class MailgunMailService(
    private val mailSentLogRepository: MailSentLogRepository
): MailService{
    private val logger = LoggerFactory.getLogger(MailgunMailService::class.java)

    @CircuitBreaker(name = "mailgun")
    override fun sendMail(userId: Long, from: String, to: String, content: String) {
        mailSentLogRepository.save(
            MailSentLog(
                id = null,
                mailService = MailServiceType.MAILGUN
            )
        )
        return
    }

    private fun fallbackSendMail(
        userId: Long, from: String, to: String, content: String, error: Throwable
    ) {
        logger.warn("mailgun mail service in trouble, ${error.message}")
        throw BadGatewayException("메일건 메일 서비스 장애가 발생했습니다.")
    }
}