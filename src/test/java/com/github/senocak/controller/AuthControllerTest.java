package com.github.senocak.controller;

import com.github.senocak.dto.auth.LoginRequest;
import com.github.senocak.dto.auth.RegisterRequest;
import com.github.senocak.dto.user.UserWrapperResponse;
import com.github.senocak.exception.ServerException;
import com.github.senocak.factory.LoanFactory;
import com.github.senocak.factory.UserFactory;
import com.github.senocak.model.Role;
import com.github.senocak.model.User;
import com.github.senocak.security.JwtTokenProvider;
import com.github.senocak.service.RoleService;
import com.github.senocak.service.UserService;
import com.github.senocak.util.AppConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import java.util.Map;
import static com.github.senocak.TestConstants.USER_EMAIL;
import static com.github.senocak.TestConstants.USER_NAME;
import static com.github.senocak.TestConstants.USER_PASSWORD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for AuthController")
public class AuthControllerTest {
    @InjectMocks AuthController authController;

    @Mock UserService userService;
    @Mock RoleService roleService;
    @Mock JwtTokenProvider tokenProvider;
    @Mock AuthenticationManager authenticationManager;
    @Mock Authentication authentication;
    @Mock PasswordEncoder passwordEncoder;
    @Mock BindingResult bindingResult;

    private final User user = UserFactory.createUser(LoanFactory.createLoan(null));

    @Nested
    class LoginTest {
        private LoginRequest loginRequest;

        @BeforeEach
        void setup() {
            loginRequest = LoginRequest.builder()
                    .email(USER_EMAIL)
                    .password(USER_PASSWORD)
                    .build();
        }

        @Test
        void givenSuccessfulPath_whenLogin_thenReturn200() throws ServerException {
            // Given
            doReturn(authentication).when(authenticationManager).authenticate(
                    eq(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),
                            loginRequest.getPassword())));
            doReturn(user).when(userService).findByEmail(loginRequest.getEmail());
            String generatedToken = "generatedToken";
            doReturn(generatedToken).when(tokenProvider).generateJwtToken(user.getEmail());
            // When
            final UserWrapperResponse response = authController.login(loginRequest, bindingResult);
            // Then
            assertNotNull(response);
            assertEquals(generatedToken, response.getToken());
            assertEquals(user.getName(), response.getUserResponse().getName());
            assertEquals(user.getSurname(), response.getUserResponse().getSurname());
            assertEquals(user.getEmail(), response.getUserResponse().getEmail());
            assertEquals(user.getRoles().size(), response.getUserResponse().getRoles().size());
        }
    }

    @Nested
    class RegisterTest {
        private final RegisterRequest signUpRequest = new RegisterRequest();

        @BeforeEach
        void setup() {
            signUpRequest.setSurname(USER_NAME);
            signUpRequest.setName(USER_NAME);
            signUpRequest.setEmail(USER_EMAIL);
            signUpRequest.setPassword(USER_PASSWORD);
        }

        @Test
        void givenExistedUsername_whenRegister_thenReturn400ThrowServerException() {
            // Given
            doReturn(true).when(userService).existsByEmail(signUpRequest.getEmail());
            // When
            final Executable closureToTest = () -> authController.register(signUpRequest, bindingResult);
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        void givenExistedEmail_whenRegister_thenReturn400ThrowServerException() {
            // Given
            doReturn(true).when(userService).existsByEmail(signUpRequest.getEmail());
            // When
            Executable closureToTest = () -> authController.register(signUpRequest, bindingResult);
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        void givenInvalidRole_whenRegister_thenReturn400ThrowServerException() {
            // Given
            doReturn(null).when(roleService).findByName(AppConstants.RoleName.ROLE_USER);
            // When
            Executable closureToTest = () -> authController.register(signUpRequest, bindingResult);
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        void givenSuccessfulPath_whenRegister_thenReturn200() throws ServerException {
            // Given
            doReturn(new Role()).when(roleService).findByName(AppConstants.RoleName.ROLE_USER);
            // When
            final Map<String, String> response = authController.register(signUpRequest, bindingResult);
            // Then
            assertNotNull(response);
            assertEquals("User created.", response.get("message"));
        }
    }
}
