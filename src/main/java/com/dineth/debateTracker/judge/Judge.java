package com.dineth.debateTracker.judge;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "judge")
@Getter
@Setter
@NoArgsConstructor
public class Judge implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "judge_seq")
    @SequenceGenerator(name = "judge_seq", sequenceName = "judge_seq", allocationSize = 1)
    private Long id;
    private String fname;
    private String lname;
    private String gender;
    private String email;
    private String phone;
    private Date birthdate;
    private Float rating;
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

    public Judge(Float rating, String fname, String lname) {
        this.fname = fname;
        this.lname = lname == null ? "" : lname;
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "Judge{" + "id='" + id + '\'' + ", name='" + fname + " " + lname + '\'' + ", gender='" + gender + '\'' + ", email='" + email + '\'' + ", phone='" + phone + '\'' + ", birthdate=" + birthdate + ", rating=" + rating + '}';
    }
}
