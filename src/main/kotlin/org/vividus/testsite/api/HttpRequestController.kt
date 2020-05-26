package org.vividus.testsite.api

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import java.util.UUID
import java.util.concurrent.TimeUnit
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class HttpRequestController {
    val cache: LoadingCache<UUID, Long> = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build { System.currentTimeMillis() }

    @GetMapping("/api/delayed-response")
    fun delayResponse(@RequestParam clientId: UUID): ResponseEntity<Unit> {
        val firstRequestTimestamp = cache.get(clientId)
        if (firstRequestTimestamp != null && countAgeInSeconds(firstRequestTimestamp) < 5) {
            return ResponseEntity.notFound().build()
        }
        cache.invalidate(clientId)
        return ResponseEntity.ok().build()
    }

    private fun countAgeInSeconds(firstRequestTimestamp: Long): Long {
        return (System.currentTimeMillis() - firstRequestTimestamp) / 1000
    }
}
