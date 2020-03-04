package org.vividus.testsite

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TestSiteApplication

fun main(args: Array<String>) {
	runApplication<TestSiteApplication>(*args)
}
