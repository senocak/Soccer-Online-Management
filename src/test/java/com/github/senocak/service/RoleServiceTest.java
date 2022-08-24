package com.github.senocak.service;

import com.github.senocak.model.Role;
import com.github.senocak.repository.RoleRepository;
import com.github.senocak.util.AppConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for RoleService")
public class RoleServiceTest {
    @InjectMocks RoleService roleService;
    @Mock RoleRepository roleRepository;

    @Test
    public void givenRoleName_whenFindByName_thenAssertResult(){
        // Given
        Role role = new Role();
        AppConstants.RoleName roleName = AppConstants.RoleName.ROLE_USER;
        Mockito.doReturn(Optional.of(role)).when(roleRepository).findByName(roleName);
        // When
        Role findByName = roleService.findByName(roleName);
        // Then
        Assertions.assertEquals(role, findByName);
    }

    @Test
    public void givenNullRoleName_whenFindByName_thenAssertResult(){
        // Given
        AppConstants.RoleName roleName = AppConstants.RoleName.ROLE_USER;
        // When
        Role findByName = roleService.findByName(roleName);
        // Then
        Assertions.assertNull(findByName);
    }
}
