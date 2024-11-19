package com.github.senocak.service;

import com.github.senocak.TestConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for AuthenticationService")
public class AuthorizationInterceptorTest {
    @InjectMocks
    AuthenticationServiceImpl authenticationServiceImpl;
    @Mock Authentication auth;
    User user;

    @BeforeEach
    public void initSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void givenNullAuthenticationWhenIsAuthorizedThenThrowAccessDeniedException() {
        // When
        Executable closureToTest = () -> authenticationServiceImpl.isAuthorized("");
        // Then
        Assertions.assertThrows(AccessDeniedException.class, closureToTest);
    }

    @Test
    public void givenWhenIsAuthorizedThenAssertResult() throws AccessDeniedException {
        // Given
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        user = new User(TestConstants.USER_USERNAME, TestConstants.USER_PASSWORD, authorities);
        Mockito.doReturn(user).when(auth).getPrincipal();
        // When
        boolean preHandle = authenticationServiceImpl.isAuthorized("ADMIN");
        // Then
        Assertions.assertTrue(preHandle);
    }

    @Test
    public void givenNotValidRoleWhenIsAuthorizedThenAssertResult() throws AccessDeniedException {
        // Given
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        user = new User(TestConstants.USER_USERNAME, TestConstants.USER_PASSWORD, authorities);
        Mockito.doReturn(user).when(auth).getPrincipal();
        // When
        boolean preHandle = authenticationServiceImpl.isAuthorized("USER");
        // Then
        Assertions.assertFalse(preHandle);
    }
}
