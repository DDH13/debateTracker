package com.dineth.debateTracker.debater;

import com.dineth.debateTracker.institution.Institution;
import com.dineth.debateTracker.team.Team;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "debater")
@Getter
@Setter
@NoArgsConstructor
public class Debater implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "debater_seq")
    @SequenceGenerator(name = "debater_seq", sequenceName = "debater_seq", allocationSize = 1)
    private Long id;
    private String fname;
    private String lname;
    private String gender;
    private String email;
    private String phone;
    private Date birthdate;

    public Debater(String fname, String lname) {
        this.fname = fname;
        this.lname = lname;
    }

    @Override
    public String toString() {
        return "Debater{" +
                "id='" + id + '\'' +
                ", name='" + fname + " " + lname + '\'' +
//                ", gender='" + gender + '\'' +
//                ", email='" + email + '\'' +
//                ", phone='" + phone + '\'' +
//                ", birthdate=" + birthdate +
                '}';
    }
}
