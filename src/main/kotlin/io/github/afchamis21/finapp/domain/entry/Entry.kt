package io.github.afchamis21.finapp.domain.entry

import io.github.afchamis21.finapp.domain.AuditableEntity
import io.github.afchamis21.finapp.domain.category.Category
import io.github.afchamis21.finapp.domain.user.User
import jakarta.persistence.*
import lombok.EqualsAndHashCode
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "entries")
@EqualsAndHashCode(of = ["id"])
class Entry(
    @Id @GeneratedValue @Column(name = "entry_id") var id: Long? = null,
    @Column(precision = 18, scale = 2, nullable = false) var value: BigDecimal,
    @Column(nullable = false) var label: String,
    @Column(nullable = false) var date: LocalDate,

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne
    var owner: User,

    @ManyToMany
    @JoinTable(
        name = "entry_categories",
        joinColumns = [JoinColumn(name = "entry_id")],
        inverseJoinColumns = [JoinColumn(name = "category_id")]
    )
    var categories: MutableSet<Category> = mutableSetOf(),

    createDt: Instant? = null, updateDt: Instant? = null
) : AuditableEntity(createDt, updateDt)
