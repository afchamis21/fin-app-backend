package io.github.afchamis21.finapp.domain.auth

import io.github.afchamis21.finapp.domain.user.User
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.Instant

@Entity
@Table(name = "refres_tokens")
class RefreshToken(
    @Id @GeneratedValue @Column(name = "token_id") var id: Long? = null,
    @Column(nullable = false) var token: String,
    @Column(nullable = false, name = "expires_at") var expiresAt: Instant,

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne var owner: User,
) {
}