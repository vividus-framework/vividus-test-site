package org.vividus.testsite.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

@Controller()
class PageController
    @Autowired
    constructor(
        private val resourceLoader: ResourceLoader,
    ) {
        @GetMapping("/delayedLoading")
        fun delayedLoading(
            model: Model,
            @RequestParam(required = false, defaultValue = "0") pageTimeout: Long,
            @RequestParam(required = false, defaultValue = "0") imageTimeout: Long,
        ): String {
            sleepFor(pageTimeout)
            model["pageTimeout"] = pageTimeout
            model["imageTimeout"] = imageTimeout
            return "delayedLoading"
        }

        @GetMapping("/image")
        fun getImageAsResponseEntity(
            @RequestParam(required = false, defaultValue = "0") timeout: Long,
        ): ResponseEntity<Any?> {
            sleepFor(timeout)
            val image: ByteArray =
                resourceLoader.getResource("classpath:/static/img/vividus.png").inputStream.use {
                    it.readBytes()
                }
            return ResponseEntity.ok(image)
        }

        @PostMapping("/upload")
        fun uploadFile(
            @RequestParam("file") file: MultipartFile,
        ): ResponseEntity<Any?> {
            return ResponseEntity.ok(file.bytes.size)
        }

        private fun sleepFor(timeout: Long) {
            Thread.sleep(timeout)
        }
    }
