package com.github.senocak.service;

import com.github.senocak.model.Role;
import com.github.senocak.repository.RoleRepository;
import com.github.senocak.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    /**
     * @param roleName -- enum variable to retrieve from db
     * @return -- Role object retrieved from db
     */
    public Role findByName(AppConstants.RoleName roleName) {
        return roleRepository.findByName(roleName).orElse(null);
    }
}