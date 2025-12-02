package com.jun.mail.application

import com.jun.mail.application.exceptions.BadGatewayException
import com.jun.mail.domain.command.SendMailCommand
import com.jun.mail.domain.entity.MailSentLog
import com.jun.mail.domain.mail.MailService
import com.jun.mail.infrastructure.FeatureFlagConfigRepository
import com.jun.mail.infrastructure.MailSentLogRepository
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import org.slf4j.LoggerFactory
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.retry.support.RetrySynchronizationManager
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException

@Component
class MailingApplication(
    private val featureFlagConfigRepository: FeatureFlagConfigRepository,
    private val mailServices: Map<String, MailService>,
) {
    private val logger = LoggerFactory.getLogger(MailingApplication::class.java)

    @Retryable(
        maxAttempts = 5,
        backoff = Backoff(delay = 0),
        recover = "recoverSendMail",
        include = [BadGatewayException::class, CallNotPermittedException::class]
    )
    fun sendMail(sendMailCommand: SendMailCommand) {
        // feature flag 기능
        val featureFlagConfig = featureFlagConfigRepository.findFeatureFlagConfigByFeatureAndIsActive(
            feature = "MAIL_SERVICE", isActive = true
        )

        val retryContext = RetrySynchronizationManager.getContext()
        val retryCount = retryContext?.retryCount ?: 0
        if (retryCount > 0) {
            mailServices.values.forEach {
                try {
                    it.sendMail(
                        userId = sendMailCommand.userId,
                        from = sendMailCommand.from,
                        to = sendMailCommand.to,
                        content = sendMailCommand.content,
                    )
                    return
                } catch (e: Exception) {
                    logger.error("Failed to send mail with service: ${it.javaClass.name}", e)
                }
            }
        }

        if (featureFlagConfig != null) {
            // userId를 기반으로 나머지 연산
            val slot = ((sendMailCommand.userId) % featureFlagConfig.options.size).toInt()

            // ["sendgrid", "sendgrid", "mailgun", "directsend"]
            val key = featureFlagConfig.options[slot]

            mailServices[key]?.sendMail(
                userId = sendMailCommand.userId,
                from = sendMailCommand.from,
                to = sendMailCommand.to,
                content = sendMailCommand.content,
            )
        } else {
            mailServices.values.forEach {
                try {
                    it.sendMail(
                        userId = sendMailCommand.userId,
                        from = sendMailCommand.from,
                        to = sendMailCommand.to,
                        content = sendMailCommand.content,
                    )
                    return
                } catch (e: Exception) {
                    logger.error("Failed to send mail with service: ${it.javaClass.name}", e)
                }
            }
        }
    }

    @Recover
    fun recoverSendMail(sendMailCommand: SendMailCommand) {
        logger.error("Every mail services are in trouble")
        throw BadGatewayException("모든 메일 서비스가 장애 상태입니다. 잠시 후 재시도 바랍니다.")
    }

    @Retryable(maxAttempts = 5)
    fun receiveMail(sendMailCommand: SendMailCommand) {
        // feature flag 기능
        val featureFlagConfig = featureFlagConfigRepository.findFeatureFlagConfigByFeatureAndIsActive(
            feature = "RECEIVE_MAIL_SERVICE", isActive = true
        )

        val retryContext = RetrySynchronizationManager.getContext()
        val retryCount = retryContext?.retryCount ?: 0

        if (retryCount > 0) {
            mailServices.values.forEach {
                try {
                    it.sendMail(
                        userId = sendMailCommand.userId,
                        from = sendMailCommand.from,
                        to = sendMailCommand.to,
                        content = sendMailCommand.content,
                    )
                    return
                } catch (e: Exception) {
                    logger.error("Failed to receive mail with service: ${it.javaClass.name}", e)
                }
            }
        }

        if (featureFlagConfig != null) {
            // userId를 기반으로 나머지 연산
            val slot = ((sendMailCommand.userId) % featureFlagConfig.options.size).toInt()

            // ["sendgrid", "sendgrid", "mailgun", "directsend"]
            val key = featureFlagConfig.options[slot]

            mailServices[key]?.sendMail(
                userId = sendMailCommand.userId,
                from = sendMailCommand.from,
                to = sendMailCommand.to,
                content = sendMailCommand.content,
            )
        } else {
            mailServices.values.forEach {
                try {
                    it.sendMail(
                        userId = sendMailCommand.userId,
                        from = sendMailCommand.from,
                        to = sendMailCommand.to,
                        content = sendMailCommand.content,
                    )
                    return
                } catch (e: Exception) {
                    logger.error("Failed to receive mail with service: ${it.javaClass.name}", e)
                }
            }
        }
    }
}