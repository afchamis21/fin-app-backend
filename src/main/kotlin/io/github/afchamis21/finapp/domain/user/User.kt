package io.github.afchamis21.finapp.domain.user

import io.github.afchamis21.finapp.domain.AuditableEntity
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "users")
class User(
    @Id @GeneratedValue @Column var id: Long? = null,
    @Column(nullable = false, unique = true) var email: String,
    @Column(nullable = false) var username: String,
    @Column(nullable = false) var password: String,

    createDt: Instant? = null, updateDt: Instant? = null,
) : AuditableEntity(createDt, updateDt)