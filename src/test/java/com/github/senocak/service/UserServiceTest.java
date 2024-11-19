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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for UserService")
public class UserServiceTest {
    @InjectMocks UserService userService;
    @Mock UserRepository userRepository;
    @Mock Authentication auth;
    @Mock org.springframework.security.core.userdetails.User user;

    @Test
    public void givenEmail_whenFindByEmail_thenAssertResult(){
        // Given
        User user = UserFactory.createUser();
        doReturn(Optional.of(user)).when(userRepository).findByEmail("lorem@ipsum.com");
        // When
        User findByEmail = userService.findByEmail("lorem@ipsum.com");
        // Then
        assertEquals(user, findByEmail);
    }

    @Test
    public void givenNullEmail_whenFindByEmail_thenAssertResult(){
        // When
        Executable closureToTest = () -> userService.findByEmail("lorem@ipsum.com");
        // Then
        assertThrows(UsernameNotFoundException.class, closureToTest);
    }

    @Test
    public void givenEmail_whenExistsByEmail_thenAssertResult(){
        // When
        boolean existsByEmail = userService.existsByEmail("lorem@ipsum.com");
        // Then
        Assertions.assertFalse(existsByEmail);
    }

    @Test
    public void givenUser_whenSave_thenAssertResult(){
        // Given
        final User user = UserFactory.createUser();
        doReturn(user).when(userRepository).save(user);
        // When
        final User save = userService.save(user);
        // Then
        assertEquals(user, save);
    }

    @Test
    public void givenUser_whenCreate_thenAssertResult(){
        // Given
        final User user = UserFactory.createUser();
        // When
        final org.springframework.security.core.userdetails.User create = UserService.create(user);
        // Then
        assertEquals(user.getEmail(), create.getUsername());
    }

    @Test
    public void givenNullUsername_whenLoadUserByUsername_thenAssertResult(){
        // When
        final Executable closureToTest = () -> userService.loadUserByUsername("lorem@ipsum.com");
        // Then
        assertThrows(UsernameNotFoundException.class, closureToTest);
    }

    @Test
    public void givenUsername_whenLoadUserByUsername_thenAssertResult(){
        // Given
        final User user = UserFactory.createUser();
        doReturn(Optional.of(user)).when(userRepository).findByEmail("lorem@ipsum.com");
        // When
        final org.springframework.security.core.userdetails.User loadUserByUsername = userService.loadUserByUsername("lorem@ipsum.com");
        // Then
        assertEquals(user.getEmail(), loadUserByUsername.getUsername());
    }

    @Test
    public void givenNotLoggedIn_whenLoadUserByUsername_thenAssertResult(){
        // Given
        SecurityContextHolder.getContext().setAuthentication(auth);
        doReturn(user).when(auth).getPrincipal();
        // When
        final Executable closureToTest = () -> userService.loggedInUser();
        // Then
        assertThrows(ServerException.class, closureToTest);
    }

    @Test
    public void givenLoggedIn_whenLoadUserByUsername_thenAssertResult() throws ServerException {
        // Given
        SecurityContextHolder.getContext().setAuthentication(auth);
        doReturn(user).when(auth).getPrincipal();
        doReturn("lorem@ipsum.com").when(user).getUsername();
        final User user = UserFactory.createUser();
        doReturn(Optional.of(user)).when(userRepository).findByEmail("lorem@ipsum.com");
        // When
        final User loggedInUser = userService.loggedInUser();
        // Then
        assertEquals(user.getSurname(), loggedInUser.getSurname());
    }
}