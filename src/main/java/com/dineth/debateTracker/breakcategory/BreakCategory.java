package com.dineth.debateTracker.breakcategory;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity @Table(name = "break_category") @Getter @Setter @NoArgsConstructor
public class BreakCategory implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "break_category_seq")
    @SequenceGenerator(name = "break_category_seq", sequenceName = "break_category_seq", allocationSize = 1)
    private Long id;
    private String name;
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    public BreakCategory(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "BreakCategory{" + "id='" + id + '\'' + ", name='" + name + '\'' + '}';
    }
}

