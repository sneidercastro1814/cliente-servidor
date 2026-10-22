package com.carvajal.wishlist.auth.repository;

import com.carvajal.wishlist.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByEmailIgnoreCaseAndActivoTrue(String email);
}
