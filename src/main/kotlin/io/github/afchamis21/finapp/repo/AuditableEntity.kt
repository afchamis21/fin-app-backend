package io.github.afchamis21.finapp.repo

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import java.time.Instant

@MappedSuperclass
abstract class AuditableEntity(
    @Column(name = "create_dt", updatable = false) var createDt: Instant?,
    @Column(name = "update_dt") var updateDt: Instant?
) {

    @PrePersist
    fun prePersist() {
        if (createDt == null) createDt = Instant.now()
        if (updateDt == null) updateDt = Instant.now()
    }

    @PreUpdate
    fun preUpdate() {
        updateDt = Instant.now()
    }
}
