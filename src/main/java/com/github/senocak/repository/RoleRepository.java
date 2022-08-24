package com.github.senocak.repository;

import java.util.Optional;
import com.github.senocak.model.Role;
import com.github.senocak.util.AppConstants;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(AppConstants.RoleName roleName);
}
