package com.github.senocak.repository;

import java.util.List;
import java.util.Optional;
import com.github.senocak.model.User;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    List<User> findByIdIn(List<String> userIds);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
