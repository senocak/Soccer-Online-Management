package com.github.senocak.controller.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.senocak.TestConstants;
import com.github.senocak.config.SpringBootTestConfig;
import com.github.senocak.controller.UserController;
import com.github.senocak.dto.user.UserUpdateDto;
import com.github.senocak.exception.RestExceptionHandler;
import com.github.senocak.exception.ServerException;
import com.github.senocak.factory.TeamFactory;
import com.github.senocak.factory.UserFactory;
import com.github.senocak.model.User;
import com.github.senocak.repository.UserRepository;
import com.github.senocak.service.UserService;
import com.github.senocak.util.AppConstants;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * This integration test class is written for
 * @see UserController
 * 3 tests
 */
@SpringBootTestConfig
@DisplayName("Integration Tests for UserController")
public class UserControllerTest {
    @Autowired UserController userController;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @MockBean UserService userService;
    private MockMvc mockMvc;
    private final User user = UserFactory.createUser(TeamFactory.createTeam(null));

    @BeforeEach
    void beforeEach() throws ServerException {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(RestExceptionHandler.class)
                .build();
        Mockito.doReturn(user).when(userService).loggedInUser();
    }

    @Nested
    @DisplayName("Get me")
    class GetMeTest {
        @Test
        @DisplayName("Happy Path")
        void given_whenGetMe_thenReturn200() throws Exception {
            // Given
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .get(UserController.URL + "/me");
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.name",
                    IsEqual.equalTo(user.getName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.username",
                    IsEqual.equalTo(user.getUsername())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.email",
                    IsEqual.equalTo(user.getEmail())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.roles",
                    Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.team.id",
                        IsEqual.equalTo(user.getTeam().getId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.team.name",
                        IsEqual.equalTo("Random Team")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.team.country",
                        IsEqual.equalTo("Turkey")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.team.availableCash",
                        IsEqual.equalTo(100)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.team.players",
                        Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.team.players[0].id",
                        IsEqual.equalTo(user.getTeam().getPlayers().get(0).getId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.team.players[0].firstName",
                        IsEqual.equalTo("John")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.team.players[0].lastName",
                        IsEqual.equalTo("Doe")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.team.players[0].country",
                        IsEqual.equalTo("Turkey")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.team.players[0].age",
                        IsEqual.equalTo(30)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.team.players[0].position",
                        IsEqual.equalTo("Forward")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.team.players[0].marketValue",
                        IsEqual.equalTo(100)));
        }
    }

    @Nested
    @DisplayName("Patch me")
    class PatchMeTest {
        private final UserUpdateDto updateUserDto = new UserUpdateDto();

        @Test
        @DisplayName("ServerException is expected since schema is invalid")
        void givenInvalidSchema_whenPatchMe_thenThrowServerException() throws Exception {
            // Given
            updateUserDto.setName("as");
            updateUserDto.setPassword("a");
            updateUserDto.setPassword_confirmation("b");
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .patch(UserController.URL + "/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(updateUserDto));
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
                    Matchers.hasSize(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                    is(notNullValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[1]",
                    is(notNullValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[2]",
                    is(notNullValue())));
        }

        @Test
        @DisplayName("Happy Path")
        void given_whenPatchMe_thenReturn200() throws Exception {
            // Given
            updateUserDto.setName(TestConstants.USER_NAME);
            updateUserDto.setPassword(TestConstants.USER_PASSWORD);
            updateUserDto.setPassword_confirmation(TestConstants.USER_PASSWORD);
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .patch(UserController.URL + "/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(updateUserDto));
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        IsEqual.equalTo("User updated.")));
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
