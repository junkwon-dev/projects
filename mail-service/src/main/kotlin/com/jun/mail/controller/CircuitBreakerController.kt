package com.jun.mail.controller

import com.jun.mail.domain.circuitbreaker.CircuitBreakerService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/circuit-breakers")
class CircuitBreakerController(
    private val circuitBreakerService: CircuitBreakerService
) {

    @GetMapping
    fun list() = circuitBreakerService.list()

    @GetMapping("/{name}")
    fun get(@PathVariable name: String) = circuitBreakerService.get(name)

    data class TransitionRequest(val state: String)

    @PostMapping("/{name}/transition")
    @ResponseStatus(HttpStatus.OK)
    fun transition(@PathVariable name: String, @RequestBody req: TransitionRequest) =
        circuitBreakerService.transition(name, req.state)

    @PostMapping("/{name}/probe/success")
    @ResponseStatus(HttpStatus.OK)
    fun probeSuccess(@PathVariable name: String) = circuitBreakerService.recordSuccess(name)

    @PostMapping("/{name}/probe/failure")
    @ResponseStatus(HttpStatus.OK)
    fun probeFailure(@PathVariable name: String) = circuitBreakerService.recordFailure(name)
}
