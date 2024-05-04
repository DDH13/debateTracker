package com.dineth.debateTracker.debater;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DebaterRepository extends JpaRepository<Debater, Long> {
    List<Debater> findDebatersByFirstNameEqualsIgnoreCaseAndLastNameEqualsIgnoreCase(String firstName, String lastName);
    List<Debater> findDebatersByFirstNameEqualsIgnoreCaseAndLastNameEqualsIgnoreCaseAndBirthdate(String firstName, String lastName, Date birthdate);

}
