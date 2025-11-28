package com.jun.mail.domain.circuitbreaker

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.springframework.stereotype.Service

@Service
class CircuitBreakerService(
    private val registry: CircuitBreakerRegistry
) {
    data class CircuitBreakerInfo(
        val name: String,
        val state: CircuitBreaker.State,
        val failureRate: Float,
        val bufferedCalls: Int,
        val failedCalls: Int,
        val successCalls: Int,
        val slowCallRate: Float,
        val permittedNumberOfCallsInHalfOpenState: Int,
        val slidingWindowSize: Int,
        val slidingWindowType: String,
    )

    fun list(): List<CircuitBreakerInfo> =
        registry.allCircuitBreakers.map { it.toInfo() }

    fun get(name: String): CircuitBreakerInfo =
        registry.circuitBreaker(name).toInfo()

    fun transition(name: String, target: String): CircuitBreakerInfo {
        val cb = registry.circuitBreaker(name)
        when (target.lowercase()) {
            "open" -> cb.transitionToOpenState()
            "closed" -> cb.transitionToClosedState()
            "half_open", "half-open", "halfopen" -> cb.transitionToHalfOpenState()
            else -> throw IllegalArgumentException("Unsupported target state: $target. Use one of [open, closed, half-open]")
        }
        return cb.toInfo()
    }

    private fun CircuitBreaker.toInfo(): CircuitBreakerInfo {
        val metrics = this.metrics
        val cfg = this.circuitBreakerConfig
        return CircuitBreakerInfo(
            name = this.name,
            state = this.state,
            failureRate = metrics.failureRate,
            bufferedCalls = metrics.numberOfBufferedCalls,
            failedCalls = metrics.numberOfFailedCalls,
            successCalls = metrics.numberOfSuccessfulCalls,
            slowCallRate = metrics.slowCallRate,
            permittedNumberOfCallsInHalfOpenState = cfg.permittedNumberOfCallsInHalfOpenState,
            slidingWindowSize = cfg.slidingWindowSize,
            slidingWindowType = cfg.slidingWindowType.name
        )
    }
}
