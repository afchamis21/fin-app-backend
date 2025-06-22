package io.github.afchamis21.finapp.category.model

import io.github.afchamis21.finapp.repo.AuditableEntity
import io.github.afchamis21.finapp.repo.ICacheable
import io.github.afchamis21.finapp.user.model.User
import jakarta.persistence.*
import lombok.EqualsAndHashCode
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "categories")
@EqualsAndHashCode(of = ["id"])
class Category(
    @Id @GeneratedValue @Column(name = "category_id") var id: Long? = null,
    @Column(nullable = false) var label: String,
    @Column(nullable = false) var color: String,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var type: CategoryType,

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne var owner: User,

    @Column(nullable = false) var active: Boolean = true,

    @Column(nullable = true) var goal: BigDecimal?,

    createDt: Instant? = null, updateDt: Instant? = null
) : AuditableEntity(createDt, updateDt), ICacheable<Long> {
    override fun getCacheKey(): Long {
        return id!!
    }
}