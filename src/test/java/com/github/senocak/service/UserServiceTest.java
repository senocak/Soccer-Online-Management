package com.github.senocak.service;

import com.github.senocak.exception.ServerException;
import com.github.senocak.factory.UserFactory;
import com.github.senocak.model.User;
import com.github.senocak.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.Optional;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for UserService")
public class UserServiceTest {
    @InjectMocks UserService userService;
    @Mock UserRepository userRepository;
    @Mock Authentication auth;
    @Mock org.springframework.security.core.userdetails.User user;

    @Test
    public void givenUsername_whenFindByUsername_thenAssertResult(){
        // Given
        User user = UserFactory.createUser(null);
        Mockito.doReturn(Optional.of(user)).when(userRepository).findByUsername("username");
        // When
        User findByUsername = userService.findByUsername("username");
        // Then
        Assertions.assertEquals(user, findByUsername);
    }

    @Test
    public void givenNullUsername_whenFindByUsername_thenAssertResult(){
        // When
        Executable closureToTest = () -> userService.findByUsername("username");
        // Then
        Assertions.assertThrows(UsernameNotFoundException.class, closureToTest);
    }

    @Test
    public void givenUsername_whenExistsByUsername_thenAssertResult(){
        // When
        boolean existsByUsername = userService.existsByUsername("username");
        // Then
        Assertions.assertFalse(existsByUsername);
    }

    @Test
    public void givenUsername_whenExistsByEmail_thenAssertResult(){
        // When
        boolean existsByEmail = userService.existsByEmail("username");
        // Then
        Assertions.assertFalse(existsByEmail);
    }

    @Test
    public void givenUser_whenSave_thenAssertResult(){
        // Given
        User user = UserFactory.createUser(null);
        Mockito.doReturn(user).when(userRepository).save(user);
        // When
        User save = userService.save(user);
        // Then
        Assertions.assertEquals(user, save);
    }

    @Test
    public void givenUser_whenCreate_thenAssertResult(){
        // Given
        User user = UserFactory.createUser(null);
        // When
        org.springframework.security.core.userdetails.User create = UserService.create(user);
        // Then
        Assertions.assertEquals(user.getUsername(), create.getUsername());
    }

    @Test
    public void givenNullUsername_whenLoadUserByUsername_thenAssertResult(){
        // When
        Executable closureToTest = () -> userService.loadUserByUsername("username");
        // Then
        Assertions.assertThrows(UsernameNotFoundException.class, closureToTest);
    }

    @Test
    public void givenUsername_whenLoadUserByUsername_thenAssertResult(){
        // Given
        User user = UserFactory.createUser(null);
        Mockito.doReturn(Optional.of(user)).when(userRepository).findByUsername("username");
        // When
        org.springframework.security.core.userdetails.User loadUserByUsername = userService.loadUserByUsername("username");
        // Then
        Assertions.assertEquals(user.getUsername(), loadUserByUsername.getUsername());
    }

    @Test
    public void givenNotLoggedIn_whenLoadUserByUsername_thenAssertResult(){
        // Given
        SecurityContextHolder.getContext().setAuthentication(auth);
        Mockito.doReturn(user).when(auth).getPrincipal();
        // When
        Executable closureToTest = () -> userService.loggedInUser();
        // Then
        Assertions.assertThrows(ServerException.class, closureToTest);
    }

    @Test
    public void givenLoggedIn_whenLoadUserByUsername_thenAssertResult() throws ServerException {
        // Given
        SecurityContextHolder.getContext().setAuthentication(auth);
        Mockito.doReturn(user).when(auth).getPrincipal();
        Mockito.doReturn("username").when(user).getUsername();
        User user = UserFactory.createUser(null);
        Mockito.doReturn(Optional.of(user)).when(userRepository).findByUsername("username");
        // When
        User loggedInUser = userService.loggedInUser();
        // Then
        Assertions.assertEquals(user.getUsername(), loggedInUser.getUsername());
    }
}