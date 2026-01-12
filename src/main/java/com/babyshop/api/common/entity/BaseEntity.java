package com.babyshop.api.common.entity;
import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;
import java.util.UUID;
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;
    @LastModifiedBy
    @Column(name = "updated_by")
    private UUID updatedBy;
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
    public void softDelete() { this.isDeleted = true; }
    public void restore() { this.isDeleted = false; }
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
        if (isDeleted == null) isDeleted = false;
    }
    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }
}
