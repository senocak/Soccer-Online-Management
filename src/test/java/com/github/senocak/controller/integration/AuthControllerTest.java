package com.github.senocak.controller.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.senocak.config.SpringBootTestConfig;
import com.github.senocak.controller.AuthController;
import com.github.senocak.dto.auth.LoginRequest;
import com.github.senocak.dto.auth.RegisterRequest;
import com.github.senocak.exception.RestExceptionHandler;
import com.github.senocak.model.Role;
import com.github.senocak.repository.RoleRepository;
import com.github.senocak.service.RoleService;
import com.github.senocak.service.UserService;
import com.github.senocak.util.AppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import static com.github.senocak.TestConstants.USER_EMAIL;
import static com.github.senocak.TestConstants.USER_NAME;
import static com.github.senocak.TestConstants.USER_PASSWORD;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * This integration test class is written for
 * @see AuthController
 * 7 tests
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
            final RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(AuthController.URL + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(loginRequest));
            // When
            final ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.exception.statusCode", equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.exception.error.id", equalTo(AppConstants.OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.getMessageId())))
                .andExpect(jsonPath("$.exception.error.text", equalTo(AppConstants.OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.getText())))
                .andExpect(jsonPath("$.exception.variables", hasSize(2)))
                .andExpect(jsonPath("$.exception.variables[0]", is(notNullValue())))
                .andExpect(jsonPath("$.exception.variables[1]", is(notNullValue())));
        }

        @Test
        @DisplayName("ServerException is expected since credentials are not valid")
        void givenInvalidCredentials_whenLogin_thenThrowServerException() throws Exception {
            // Given
            loginRequest.setEmail("EMAIL");
            loginRequest.setPassword("PASSWORD");
            final RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(AuthController.URL + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(loginRequest));
            // When
            final ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.exception.statusCode", equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.exception.error.id", equalTo(AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT.getMessageId())))
                .andExpect(jsonPath("$.exception.error.text", equalTo(AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT.getText())))
                .andExpect(jsonPath("$.exception.variables", hasSize(1)))
                .andExpect(jsonPath("$.exception.variables[0]", equalTo("Bad credentials")));
        }

        @Test
        @Transactional
        @DisplayName("Happy path")
        void given_whenLogin_thenReturn200() throws Exception {
            // Given
            loginRequest.setEmail(USER_EMAIL);
            loginRequest.setPassword(USER_PASSWORD);
            final RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(AuthController.URL + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(loginRequest));
            // When
            final ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.user.name", equalTo(USER_NAME)))
                .andExpect(jsonPath("$.user.email", notNullValue()))
                .andExpect(jsonPath("$.user.roles", hasSize(1)))
                .andExpect(jsonPath("$.user.roles[0].name", equalTo(AppConstants.RoleName.ROLE_ADMIN.toString())))
                .andExpect(jsonPath("$.token", notNullValue()));
        }
    }

    @Nested
    @DisplayName("Test class for register scenarios")
    class RegisterTest {
        final RegisterRequest registerRequest = new RegisterRequest();

        @Test
        @DisplayName("ServerException is expected since request body is not valid")
        void givenInvalidSchema_whenRegister_thenThrowServerException() throws Exception {
            // Given
            final RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(AuthController.URL + "/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(registerRequest));
            // When
            final ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.exception.statusCode", equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.exception.error.id", equalTo(AppConstants.OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.getMessageId())))
                .andExpect(jsonPath("$.exception.error.text", equalTo(AppConstants.OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.getText())))
                .andExpect(jsonPath("$.exception.variables", hasSize(5)));
        }

        @Test
        @DisplayName("ServerException is expected since there is already user with same email")
        void givenEmailExist_whenRegister_thenThrowServerException() throws Exception {
            // Given
            registerRequest.setName(USER_NAME);
            registerRequest.setSurname("USER_USERNAME");
            registerRequest.setEmail(USER_EMAIL);
            registerRequest.setPassword(USER_PASSWORD);
            final RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(AuthController.URL + "/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(registerRequest));
            // When
            final ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.exception.statusCode", equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.exception.error.id", equalTo(AppConstants.OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.getMessageId())))
                .andExpect(jsonPath("$.exception.error.text", equalTo(AppConstants.OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.getText())))
                .andExpect(jsonPath("$.exception.variables", hasSize(1)))
                .andExpect(jsonPath("$.exception.variables[0]", equalTo("Email Address already in use!")));
        }

        @Test
        @DisplayName("ServerException is expected since invalid role")
        void givenNullRole_whenRegister_thenThrowServerException() throws Exception {
            // Given
            doReturn(null).when(roleService).findByName(AppConstants.RoleName.ROLE_USER);
            registerRequest.setName(USER_NAME);
            registerRequest.setSurname("USER_USERNAME");
            registerRequest.setEmail("userNew@email.com");
            registerRequest.setPassword(USER_PASSWORD);
            final RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(AuthController.URL + "/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(registerRequest));
            // When
            final ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.exception.statusCode", equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.exception.error.id", equalTo(AppConstants.OmaErrorMessageType.MANDATORY_INPUT_MISSING.getMessageId())))
                .andExpect(jsonPath("$.exception.error.text", equalTo(AppConstants.OmaErrorMessageType.MANDATORY_INPUT_MISSING.getText())))
                .andExpect(jsonPath("$.exception.variables", hasSize(1)))
                .andExpect(jsonPath("$.exception.variables[0]", equalTo("User Role is not found")));
        }

        @Test
        @Transactional
        @DisplayName("Happy path")
        void given_whenRegister_thenReturn201() throws Exception {
            // Given
            final Role role = roleRepository.findByName(AppConstants.RoleName.ROLE_USER).orElse(null);
            doReturn(role).when(roleService).findByName(AppConstants.RoleName.ROLE_USER);
            registerRequest.setName(USER_NAME);
            registerRequest.setSurname(USER_NAME);
            registerRequest.setEmail("userNew@email.com");
            registerRequest.setPassword(USER_PASSWORD);
            final RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(AuthController.URL + "/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(registerRequest));
            // When
            final ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform.andExpect(MockMvcResultMatchers.status().isCreated());
            final Map<String, String> response = objectMapper.readValue(perform.andReturn().getResponse().getContentAsString(),
                    Map.class);
            assertEquals("User created.", response.get("message"));
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
