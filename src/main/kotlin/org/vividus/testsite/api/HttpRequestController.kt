package org.vividus.testsite.api

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.util.UUID

@RestController()
@RequestMapping("/api")
class HttpRequestController {
    private val CACHE_EVICTION_TIME = Duration.ofSeconds(10)
    private val HALF_CACHE_EVICTION_TIME = CACHE_EVICTION_TIME.toMillis() / 2

    val delayedResponseCache: LoadingCache<UUID, Long> = buildCache { System.currentTimeMillis() }
    val teapotInvocations: LoadingCache<UUID, Long> = buildCache { 0 }

    private fun buildCache(loader: (key: UUID) -> Long): LoadingCache<UUID, Long> {
        return Caffeine.newBuilder().expireAfterWrite(CACHE_EVICTION_TIME).build(loader)
    }

    @GetMapping("/delayed-response")
    fun delayResponse(@RequestParam clientId: UUID): ResponseEntity<Unit> {
        val firstRequestTimestamp = delayedResponseCache.get(clientId)
        if (System.currentTimeMillis() - firstRequestTimestamp!! < HALF_CACHE_EVICTION_TIME) {
            return ResponseEntity.notFound().build()
        }
        delayedResponseCache.invalidate(clientId)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/teapot")
    fun teapot(@RequestParam clientId: UUID): ResponseEntity<Unit> {
        val numberOfInvocations = teapotInvocations.get(clientId)
        if (numberOfInvocations!! == 0L) {
            teapotInvocations.put(clientId, numberOfInvocations.inc())
            return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).build()
        }
        teapotInvocations.invalidate(clientId)
        return ResponseEntity.ok().build()
    }
}
