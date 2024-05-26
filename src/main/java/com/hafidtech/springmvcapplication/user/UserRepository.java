package com.hafidtech.springmvcapplication.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query(
            value = "SELECT * FROM User u WHERE u.email = email",
            nativeQuery = true)
    User getByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.failedAttempt =:failAttempts WHERE u.email =:email")
    void updateFailedAttempts(int failAttempts, String email);

    @Modifying
    @Query(value = "UPDATE User u set u.firstName =:firstName,"+
            " u.lastName =:lastName," + "u.email =:email where u.id =:id")
    void update(String firstName, String lastName, String email, Long id);
}
