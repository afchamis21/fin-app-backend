package io.github.afchamis21.finapp.domain.auth

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "api_keys")
class ApiKey(
    @Id @GeneratedValue @Column(name = "key_id") var id: Long? = null,
    @Column(nullable = false) var token: String,
    @Column(nullable = false, name = "expires_at") var expiresAt: Instant
) {
}