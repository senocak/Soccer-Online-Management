package com.github.senocak.controller;

import com.github.senocak.dto.auth.LoginRequest;
import com.github.senocak.dto.auth.RegisterRequest;
import com.github.senocak.dto.user.UserWrapperResponse;
import com.github.senocak.exception.ServerException;
import com.github.senocak.factory.TeamFactory;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import static com.github.senocak.TestConstants.USER_EMAIL;
import static com.github.senocak.TestConstants.USER_NAME;
import static com.github.senocak.TestConstants.USER_PASSWORD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    private final User user = UserFactory.createUser(TeamFactory.createTeam(null));

    @Nested
    class LoginTest {
        private final LoginRequest loginRequest = new LoginRequest();

        @BeforeEach
        void setup() {
            loginRequest.setUsername(USER_NAME);
            loginRequest.setPassword(USER_PASSWORD);
        }

        @Test
        void givenSuccessfulPath_whenLogin_thenReturn200() throws ServerException {
            // Given
            doReturn(authentication).when(authenticationManager).authenticate(
                    Mockito.eq(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                            loginRequest.getPassword()))
            );
            doReturn(user).when(userService).findByUsername(loginRequest.getUsername());
            String generatedToken = "generatedToken";
            doReturn(generatedToken).when(tokenProvider).generateJwtToken(user.getUsername());
            // When
            ResponseEntity<UserWrapperResponse> response = authController.login(loginRequest, bindingResult);
            // Then
            assertNotNull(response);
            assertNotNull(response.getBody());
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody().getUserResponse());
            assertEquals(generatedToken, response.getBody().getToken());
            assertEquals(user.getName(), response.getBody().getUserResponse().getName());
            assertEquals(user.getUsername(), response.getBody().getUserResponse().getUsername());
            assertEquals(user.getEmail(), response.getBody().getUserResponse().getEmail());
            assertEquals(user.getRoles().size(), response.getBody().getUserResponse().getRoles().size());
            assertNotNull(response.getBody().getUserResponse().getTeamDto());
            assertEquals("Random Team", response.getBody().getUserResponse().getTeamDto().getName());
            assertEquals("Turkey",response.getBody().getUserResponse().getTeamDto().getCountry());
            assertEquals(100, response.getBody().getUserResponse().getTeamDto().getAvailableCash());
            assertEquals(1, response.getBody().getUserResponse().getTeamDto().getPlayers().size());
            assertEquals("John", response.getBody().getUserResponse().getTeamDto().getPlayers().get(0).getFirstName());
            assertEquals("Doe", response.getBody().getUserResponse().getTeamDto().getPlayers().get(0).getLastName());
            assertEquals("Turkey", response.getBody().getUserResponse().getTeamDto().getPlayers().get(0).getCountry());
            assertEquals(30, response.getBody().getUserResponse().getTeamDto().getPlayers().get(0).getAge());
            assertEquals(AppConstants.PlayerPosition.Forward, response.getBody().getUserResponse().getTeamDto().getPlayers().get(0).getPosition());
            assertEquals(100, response.getBody().getUserResponse().getTeamDto().getPlayers().get(0).getMarketValue());
        }
    }

    @Nested
    class RegisterTest {
        private final RegisterRequest signUpRequest = new RegisterRequest();

        @BeforeEach
        void setup() {
            signUpRequest.setUsername(USER_NAME);
            signUpRequest.setName(USER_NAME);
            signUpRequest.setEmail(USER_EMAIL);
            signUpRequest.setPassword(USER_PASSWORD);
        }

        @Test
        void givenExistedUsername_whenRegister_thenReturn400ThrowServerException() {
            // Given
            doReturn(true).when(userService).existsByUsername(signUpRequest.getUsername());
            // When
            Executable closureToTest = () -> authController.register(signUpRequest, bindingResult);
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
        void givenSuccessfulPath_whenRegister_thenReturn201() throws ServerException {
            // Given
            doReturn(new Role()).when(roleService).findByName(AppConstants.RoleName.ROLE_USER);
            doReturn(user).when(userService).save(Mockito.any(User.class));
            doReturn(user).when(userService).findByUsername(signUpRequest.getUsername());
            String generatedToken = "generatedToken";
            doReturn(generatedToken).when(tokenProvider).generateJwtToken(user.getUsername());
            doReturn(authentication).when(authenticationManager).authenticate(
            Mockito.eq(new UsernamePasswordAuthenticationToken(signUpRequest.getUsername(),
                signUpRequest.getPassword()))
            );
            // When
            ResponseEntity<UserWrapperResponse> response = authController.register(signUpRequest, bindingResult);
            // Then
            assertNotNull(response);
            assertNotNull(response.getBody());
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody().getUserResponse());
            assertEquals(generatedToken, response.getBody().getToken());
            assertEquals(user.getName(), response.getBody().getUserResponse().getName());
            assertEquals(user.getUsername(), response.getBody().getUserResponse().getUsername());
            assertEquals(user.getEmail(), response.getBody().getUserResponse().getEmail());
            assertEquals(user.getRoles().size(), response.getBody().getUserResponse().getRoles().size());
            assertNotNull(response.getBody().getUserResponse().getTeamDto());
            assertEquals("Random Team", response.getBody().getUserResponse().getTeamDto().getName());
            assertEquals("Turkey",response.getBody().getUserResponse().getTeamDto().getCountry());
            assertEquals(100, response.getBody().getUserResponse().getTeamDto().getAvailableCash());
            assertEquals(1, response.getBody().getUserResponse().getTeamDto().getPlayers().size());
            assertEquals("John", response.getBody().getUserResponse().getTeamDto().getPlayers().get(0).getFirstName());
            assertEquals("Doe", response.getBody().getUserResponse().getTeamDto().getPlayers().get(0).getLastName());
            assertEquals("Turkey", response.getBody().getUserResponse().getTeamDto().getPlayers().get(0).getCountry());
            assertEquals(30, response.getBody().getUserResponse().getTeamDto().getPlayers().get(0).getAge());
            assertEquals(AppConstants.PlayerPosition.Forward, response.getBody().getUserResponse().getTeamDto().getPlayers().get(0).getPosition());
            assertEquals(100, response.getBody().getUserResponse().getTeamDto().getPlayers().get(0).getMarketValue());
        }
    }
}
