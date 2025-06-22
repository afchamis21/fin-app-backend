package io.github.afchamis21.finapp.user.model

import io.github.afchamis21.finapp.repo.AuditableEntity
import io.github.afchamis21.finapp.repo.ICacheable
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
) : AuditableEntity(createDt, updateDt), ICacheable<Long> {
    override fun getCacheKey(): Long {
        return id!!
    }
}