package com.dineth.debateTracker.debater;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DebaterRepository extends JpaRepository<Debater, Long> {
    List<Debater> findDebatersByFirstNameEqualsIgnoreCaseAndLastNameEqualsIgnoreCase(String firstName, String lastName);

    List<Debater> findDebatersByFirstNameEqualsIgnoreCaseAndLastNameEqualsIgnoreCaseAndBirthdate(String firstName, String lastName, Date birthdate);

    @Query("SELECT d.firstName, d.lastName, COUNT(d.id) FROM Debater d GROUP BY d.firstName, d.lastName HAVING COUNT(d.id) > 1")
    List<Object[]> findDebaterNameDuplicates();

    @Query("SELECT d.firstName, d.lastName, d.birthdate, COUNT(d.id) FROM Debater d GROUP BY d.firstName, d.lastName, d.birthdate HAVING COUNT(d.id) > 1")
    List<Object[]> findDebaterNameAndBirthdayDuplicates();

    List<Debater> findByFirstNameAndLastNameAllIgnoreCase(String firstName, String lastName);
}
