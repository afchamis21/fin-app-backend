package io.github.afchamis21.finapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FinAppApplication

fun main(args: Array<String>) {
    runApplication<FinAppApplication>(*args)
}
