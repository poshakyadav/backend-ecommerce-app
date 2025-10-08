package com.ecommerce.bkd.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.bkd.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email); 

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userAddresses WHERE u.id = :userId")
    Optional<User> findUserWithAddresses(@Param("userId") Long userId);

}

