package com.github.senocak.controller.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.senocak.config.SpringBootTestConfig;
import com.github.senocak.controller.AuthController;
import com.github.senocak.dto.auth.LoginRequest;
import com.github.senocak.dto.auth.RegisterRequest;
import com.github.senocak.dto.user.UserWrapperResponse;
import com.github.senocak.exception.RestExceptionHandler;
import com.github.senocak.model.Role;
import com.github.senocak.repository.RoleRepository;
import com.github.senocak.service.RoleService;
import com.github.senocak.service.UserService;
import com.github.senocak.util.AppConstants;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.hamcrest.Matchers;
import org.springframework.transaction.annotation.Transactional;
import static com.github.senocak.TestConstants.USER_EMAIL;
import static com.github.senocak.TestConstants.USER_NAME;
import static com.github.senocak.TestConstants.USER_PASSWORD;
import static com.github.senocak.TestConstants.USER_USERNAME;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * This integration test class is written for
 * @see AuthController
 * 9 tests
 */
@SpringBootTestConfig
@DisplayName("Integration Tests for AuthController")
public class AuthControllerTest {
    @Autowired AuthController authController;
    @Autowired UserService userService;
    @Autowired ObjectMapper objectMapper;
    @Autowired RoleRepository roleRepository;
    @MockBean RoleService roleService;
    private MockMvc mockMvc;

    @BeforeEach
    void beforeEach() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(RestExceptionHandler.class)
                .build();
    }

    @Nested
    @DisplayName("Test class for login scenarios")
    class LoginTest {
        LoginRequest loginRequest = new LoginRequest();

        @Test
        @DisplayName("ServerException is expected since request body is not valid")
        void givenInvalidSchema_whenLogin_thenThrowServerException() throws Exception {
            // Given
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(AuthController.URL + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(loginRequest));
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                    IsEqual.equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                    IsEqual.equalTo(AppConstants.OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.getMessageId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                    IsEqual.equalTo(AppConstants.OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.getText())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",
                    Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                    is(notNullValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[1]",
                    is(notNullValue())));
        }

        @Test
        @DisplayName("ServerException is expected since credentials are not valid")
        void givenInvalidCredentials_whenLogin_thenThrowServerException() throws Exception {
            // Given
            loginRequest.setUsername("USERNAME");
            loginRequest.setPassword("PASSWORD");
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(AuthController.URL + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(loginRequest));
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                    IsEqual.equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                    IsEqual.equalTo(AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT.getMessageId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                    IsEqual.equalTo(AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT.getText())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",
                    Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                        IsEqual.equalTo("Bad credentials")));
        }

        @Test
        @Transactional
        @DisplayName("Happy path")
        void given_whenLogin_thenReturn200() throws Exception {
            // Given
            loginRequest.setUsername(USER_USERNAME);
            loginRequest.setPassword(USER_PASSWORD);

            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(AuthController.URL + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(loginRequest));
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.username",
                    IsEqual.equalTo(USER_USERNAME)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.email",
                    IsNull.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.roles",
                    Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.roles[0].name",
                    IsEqual.equalTo(AppConstants.RoleName.ROLE_USER.toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.team.name",
                    IsEqual.equalTo("Team1")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.team.country",
                    IsEqual.equalTo("Turkey")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.team.availableCash",
                    IsEqual.equalTo(1000000)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.team.players",
                    Matchers.hasSize(19)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.token",
                    IsNull.notNullValue()));
        }
    }

    @Nested
    @DisplayName("Test class for register scenarios")
    class RegisterTest {
        RegisterRequest registerRequest = new RegisterRequest();

        @Test
        @DisplayName("ServerException is expected since request body is not valid")
        void givenInvalidSchema_whenRegister_thenThrowServerException() throws Exception {
            // Given
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(AuthController.URL + "/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(registerRequest));
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                    IsEqual.equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                    IsEqual.equalTo(AppConstants.OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.getMessageId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                    IsEqual.equalTo(AppConstants.OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.getText())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",
                    Matchers.hasSize(5)));
        }

        @Test
        @DisplayName("ServerException is expected since there is already user with same username")
        void givenUserNameExist_whenRegister_thenThrowServerException() throws Exception {
            // Given
            registerRequest.setName(USER_NAME);
            registerRequest.setUsername(USER_USERNAME);
            registerRequest.setEmail(USER_EMAIL);
            registerRequest.setPassword(USER_PASSWORD);
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(AuthController.URL + "/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(registerRequest));
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                        IsEqual.equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                        IsEqual.equalTo(AppConstants.OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.getMessageId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                        IsEqual.equalTo(AppConstants.OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.getText())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",
                        Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                        IsEqual.equalTo("Username is already taken!")));
        }

        @Test
        @DisplayName("ServerException is expected since there is already user with same email")
        void givenEmailExist_whenRegister_thenThrowServerException() throws Exception {
            // Given
            registerRequest.setName(USER_NAME);
            registerRequest.setUsername("USER_USERNAME");
            registerRequest.setEmail(USER_EMAIL);
            registerRequest.setPassword(USER_PASSWORD);
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(AuthController.URL + "/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(registerRequest));
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                        IsEqual.equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                        IsEqual.equalTo(AppConstants.OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.getMessageId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                        IsEqual.equalTo(AppConstants.OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.getText())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",
                        Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                        IsEqual.equalTo("Email Address already in use!")));
        }

        @Test
        @DisplayName("ServerException is expected since invalid role")
        void givenNullRole_whenRegister_thenThrowServerException() throws Exception {
            // Given
            Mockito.doReturn(null).when(roleService).findByName(AppConstants.RoleName.ROLE_USER);
            registerRequest.setName(USER_NAME);
            registerRequest.setUsername("USER_USERNAME");
            registerRequest.setEmail("userNew@email.com");
            registerRequest.setPassword(USER_PASSWORD);
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(AuthController.URL + "/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(registerRequest));
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                        IsEqual.equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                        IsEqual.equalTo(AppConstants.OmaErrorMessageType.MANDATORY_INPUT_MISSING.getMessageId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                        IsEqual.equalTo(AppConstants.OmaErrorMessageType.MANDATORY_INPUT_MISSING.getText())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",
                        Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                        IsEqual.equalTo("User Role is not found")));
        }

        @Test
        @DisplayName("LazyInitializationException is expected since test is not transactional")
        void given_whenRegister_thenThrowLazyInitializationException() throws Exception {
            // Given
            Role role = roleRepository.findByName(AppConstants.RoleName.ROLE_USER).orElse(null);
            Mockito.doReturn(role).when(roleService).findByName(AppConstants.RoleName.ROLE_USER);
            registerRequest.setName(USER_NAME);
            registerRequest.setUsername("USER_USERNAME");
            registerRequest.setEmail("userNew@email.com");
            registerRequest.setPassword(USER_PASSWORD);
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(AuthController.URL + "/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(registerRequest));
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                        IsEqual.equalTo(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                        IsEqual.equalTo(AppConstants.OmaErrorMessageType.GENERIC_SERVICE_ERROR.getMessageId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                        IsEqual.equalTo(AppConstants.OmaErrorMessageType.GENERIC_SERVICE_ERROR.getText())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",
                        Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                        IsEqual.equalTo("Error occured for generating jwt attempt")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[1]",
                        IsEqual.equalTo("500 INTERNAL_SERVER_ERROR")));
        }

        @Test
        @Transactional
        @DisplayName("Happy path")
        void given_whenRegister_thenReturn201() throws Exception {
            // Given
            Role role = roleRepository.findByName(AppConstants.RoleName.ROLE_USER).orElse(null);
            Mockito.doReturn(role).when(roleService).findByName(AppConstants.RoleName.ROLE_USER);
            registerRequest.setName(USER_NAME);
            registerRequest.setUsername("USER_USERNAME");
            registerRequest.setEmail("userNew@email.com");
            registerRequest.setPassword(USER_PASSWORD);
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(AuthController.URL + "/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(registerRequest));
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform.andExpect(MockMvcResultMatchers.status().isCreated());
            UserWrapperResponse response = objectMapper.readValue(perform.andReturn().getResponse().getContentAsString(),
                    UserWrapperResponse.class);
            assertNotNull(response.getToken());
            assertEquals(USER_NAME, response.getUserResponse().getName());
            assertEquals("USER_USERNAME", response.getUserResponse().getUsername());
            assertEquals("userNew@email.com", response.getUserResponse().getEmail());
            assertEquals(1, response.getUserResponse().getRoles().size());
            assertNotNull(response.getUserResponse().getTeamDto());
            assertEquals("Default Team", response.getUserResponse().getTeamDto().getName());
            assertEquals("Turkey",response.getUserResponse().getTeamDto().getCountry());
            assertEquals(5_000_000, response.getUserResponse().getTeamDto().getAvailableCash());
            assertNull(response.getUserResponse().getTeamDto().getPlayers());
        }
    }

    /**
     * @param value -- an object that want to be serialized
     * @return -- string
     * @throws JsonProcessingException -- throws JsonProcessingException
     */
    private String writeValueAsString(Object value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }
}
