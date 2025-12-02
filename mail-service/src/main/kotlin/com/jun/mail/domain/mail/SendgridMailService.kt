package com.jun.mail.domain.mail

import com.jun.mail.application.exceptions.BadGatewayException
import com.jun.mail.domain.entity.MailSentLog
import com.jun.mail.domain.entity.MailServiceType
import com.jun.mail.infrastructure.MailSentLogRepository
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service("sendgrid")
class SendgridMailService(
    private val mailSentLogRepository: MailSentLogRepository
): MailService{
    private val logger = LoggerFactory.getLogger(SendgridMailService::class.java)

    @CircuitBreaker(name = "sendgrid")
    override fun sendMail(userId: Long, from: String, to: String, content: String) {
        mailSentLogRepository.save(
            MailSentLog(
                id = null,
                mailService = MailServiceType.SENDGRID
            )
        )
        return
    }

    private fun fallbackSendMail(
        userId: Long, from: String, to: String, content: String, error: Throwable
    ) {
        logger.warn("sendgrid mail service in trouble, ${error.message}")
        throw BadGatewayException("샌드그리드 메일 서비스 장애가 발생했습니다.")
    }
}