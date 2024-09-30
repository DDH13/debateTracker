package com.dineth.debateTracker.debater;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "debater")
@Getter
@Setter
@NoArgsConstructor
public class Debater implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "debater_seq")
    @SequenceGenerator(name = "debater_seq", sequenceName = "debater_seq", allocationSize = 1)
    private Long id;
    private String firstName;
    private String lastName;
    private String gender;
    private String email;
    private String phone;
    private Date birthdate;
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

    public Debater(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
    public Debater (Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Debater{" +
                "id='" + id + '\'' +
                ", name='" + firstName + " " + lastName + '\'' +
                ", gender='" + gender + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", birthdate=" + birthdate +
                '}';
    }
}
