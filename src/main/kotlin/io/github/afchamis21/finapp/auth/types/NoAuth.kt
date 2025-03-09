package io.github.afchamis21.finapp.auth.types

@Retention(AnnotationRetention.RUNTIME)
@Target(allowedTargets = [AnnotationTarget.FUNCTION, AnnotationTarget.CLASS])
annotation class NoAuth
