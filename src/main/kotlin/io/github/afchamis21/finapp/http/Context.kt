package io.github.afchamis21.finapp.http

import org.slf4j.MDC
import java.time.Instant
import java.util.*

object Context {
    private val threadLocal = ThreadLocal<ContextData>()
    private const val TRANSACTION_KEY = "transaction-id"
    private const val USER_ID_KEY = "user-id"

    fun init() {
        threadLocal.set(ContextData())
    }

    val executionId: String
        get() = threadLocal.get().executionId

    var userId: Long?
        get() = threadLocal.get().userId
        set(value) {
            threadLocal.get().userId = value

            value?.let {
                MDC.put(USER_ID_KEY, value.toString())
            } ?: MDC.remove(USER_ID_KEY)
        }

    var locale: String
        get() = threadLocal.get().locale
        set(value) {
            threadLocal.get().locale = value
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
        var locale: String = "pt-BR"
        val messages = mutableListOf<String>()
        val exceptions = mutableListOf<Exception>()

        init {
            MDC.put(TRANSACTION_KEY, executionId)
        }
    }
}
