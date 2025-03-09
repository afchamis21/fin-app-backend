package io.github.afchamis21.finapp.context

import org.slf4j.MDC
import java.time.Instant
import java.util.*

object Context {
    private val threadLocal = ThreadLocal<ContextData>()
    private const val TRANSACTION_KEY = "transaction-id"

    fun init() {
        threadLocal.set(ContextData())
    }

    val executionId: String
        get() = threadLocal.get().executionId

    var userId: Long?
        get() = threadLocal.get().userId
        set(value) {
            threadLocal.get().userId = value
        }

    fun getStart(): Instant = threadLocal.get().start

    fun getMessages(): List<String> = threadLocal.get().messages.toList()

    fun getExceptions(): List<Exception> = threadLocal.get().exceptions.toList()

    fun addException(ex: Exception) {
        threadLocal.get().exceptions.add(ex)
    }

    fun addMessage(message: String) {
        threadLocal.get().messages.add(message)
    }

    fun clearContext() {
        threadLocal.remove()
        MDC.remove(TRANSACTION_KEY)
    }

    private class ContextData(
        val executionId: String = UUID.randomUUID().toString(),
        val start: Instant = Instant.now(),
        var userId: Long? = null
    ) {
        val messages = mutableListOf<String>()
        val exceptions = mutableListOf<Exception>()

        init {
            MDC.put(TRANSACTION_KEY, executionId)
        }
    }
}
