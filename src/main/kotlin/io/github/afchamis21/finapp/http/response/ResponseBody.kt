package io.github.afchamis21.finapp.http.response

class ResponseBody<T> (
    val payload: T,
    val messages: List<String>
)