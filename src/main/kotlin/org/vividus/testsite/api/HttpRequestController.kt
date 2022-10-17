package org.vividus.testsite.api

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RestController()
@RequestMapping("/api")
class HttpRequestController {
    private val CACHE_EVICTION_TIME = Duration.ofSeconds(10)

    val teapotInvocations: LoadingCache<UUID, Long> = buildCache { 0 }

    private fun buildCache(loader: (key: UUID) -> Long): LoadingCache<UUID, Long> {
        return Caffeine.newBuilder().expireAfterWrite(CACHE_EVICTION_TIME).build(loader)
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

    @GetMapping("/redirect")
    fun redirect(): ResponseEntity<Any> {
        return ResponseEntity
            .status(HttpStatus.FOUND)
            .header("Location", "/index.html")
            .build()
    }

    @GetMapping("/no-content")
    fun noContent(): ResponseEntity<Any> {
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/zip-archive")
    fun getResponseAsZipArchive(): ResponseEntity<ByteArray> {
        try {
            ByteArrayOutputStream().use { byteArrayOutputStream ->
                BufferedOutputStream(byteArrayOutputStream).use { bufferedOutputStream ->
                    ZipOutputStream(bufferedOutputStream).use { zipOutputStream ->
                        val responseTextFromZipArchive = "Response text from ZIP archive"

                        zipOutputStream.putNextEntry(ZipEntry("txtFileFromZipArchive.txt"))
                        zipOutputStream.write(responseTextFromZipArchive.toByteArray(StandardCharsets.UTF_8))
                        zipOutputStream.closeEntry()
                        zipOutputStream.putNextEntry(ZipEntry("emptyDataFromZipArchive.data"))
                        zipOutputStream.closeEntry()
                        zipOutputStream.finish()
                        zipOutputStream.flush()

                        val headers = HttpHeaders()
                        headers["Content-Type"] = "application/zip"
                        headers["Content-Disposition"] = "attachment; filename=\"zip-archive.zip\""

                        return ResponseEntity.ok()
                            .headers(headers)
                            .body(byteArrayOutputStream.toByteArray())
                    }
                }
            }
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }
}
