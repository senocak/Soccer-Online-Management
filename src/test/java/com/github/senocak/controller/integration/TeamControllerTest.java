package com.github.senocak.controller.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.senocak.config.SpringBootTestConfig;
import com.github.senocak.controller.TeamController;
import com.github.senocak.dto.team.TeamCreateDto;
import com.github.senocak.dto.team.TeamUpdateDto;
import com.github.senocak.exception.RestExceptionHandler;
import com.github.senocak.exception.ServerException;
import com.github.senocak.factory.UserFactory;
import com.github.senocak.model.User;
import com.github.senocak.service.UserService;
import com.github.senocak.util.AppConstants;
import org.hamcrest.Matchers;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * This integration test class is written for
 * @see TeamController
 * 16 tests
 */
@SpringBootTestConfig
@DisplayName("Integration Tests for TeamController")
public class TeamControllerTest {
    @Autowired TeamController teamController;
    @Autowired ObjectMapper objectMapper;
    @MockBean UserService userService;
    private MockMvc mockMvc;
    private final User user = UserFactory.createUser(null);

    @BeforeEach
    void beforeEach() throws ServerException {
        mockMvc = MockMvcBuilders.standaloneSetup(teamController)
                .setControllerAdvice(RestExceptionHandler.class)
                .build();
        Mockito.lenient().doReturn(user).when(userService).loggedInUser();
    }

    @Nested
    @DisplayName("Create new team")
    class CreateTest {
        private final TeamCreateDto teamCreateDto = new TeamCreateDto();

        @Test
        @DisplayName("ServerException is expected since schema is invalid")
        void givenInvalidSchema_whenCreate_thenThrowServerException() throws Exception {
            // Given
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(TeamController.URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(teamCreateDto));
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
        @DisplayName("ServerException is expected since not Transactional")
        void givenNoSession_whenCreate_thenThrowLazyInitializationException() throws Exception {
            // Given
            teamCreateDto.setName("Team 1");
            teamCreateDto.setUsername("username");
            teamCreateDto.setCountry("USA7");
            teamCreateDto.setAvailableCash(1_000_000);
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(TeamController.URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(teamCreateDto));
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
                        Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                        IsEqual.equalTo("failed to lazily initialize a collection of role: com.github.senocak.model.Team.players, could not initialize proxy - no Session")));
        }

        @Test
        @Transactional
        @DisplayName("ServerException is expected since team exists")
        void givenSameTeam_whenCreate_thenThrowServerException() throws Exception {
            // Given
            teamCreateDto.setName("Team 1");
            teamCreateDto.setUsername("username");
            teamCreateDto.setCountry("USA7");
            teamCreateDto.setAvailableCash(1_000_000);
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(TeamController.URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(teamCreateDto));
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                        IsEqual.equalTo(HttpStatus.CONFLICT.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                        IsEqual.equalTo(AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT.getMessageId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                        IsEqual.equalTo(AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT.getText())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",
                        Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                        IsEqual.equalTo("Team " + teamCreateDto.getName() + " already exist")));
        }

        @Test
        @Transactional
        @DisplayName("Happy Path")
        void given_whenCreate_thenReturn200() throws Exception {
            // Given
            teamCreateDto.setName("Team"+System.currentTimeMillis());
            teamCreateDto.setUsername("username");
            teamCreateDto.setCountry("USA7");
            teamCreateDto.setAvailableCash(1_000_000);
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(TeamController.URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(teamCreateDto));
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.team.id",
                    IsNull.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.team.name",
                    IsEqual.equalTo(teamCreateDto.getName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.team.country",
                    IsEqual.equalTo(teamCreateDto.getCountry())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.team.availableCash",
                    IsEqual.equalTo(teamCreateDto.getAvailableCash())));
        }
    }

    @Nested
    @DisplayName("Get all teams")
    class GetAllTest {
        @Test
        @DisplayName("ServerException is expected since invalid next query param is sent")
        void givenInvalidNextQueryParam_whenGetAll_thenThrowServerException() throws Exception {
            // Given
            RequestBuilder requestBuilder = MockMvcRequestBuilders.get(TeamController.URL + "?next=ipsum");
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
                IsEqual.equalTo("Failed to convert value of type 'java.lang.String' to required type 'int'; nested exception is java.lang.NumberFormatException: For input string: \"ipsum\"")));
        }

        @Test
        @DisplayName("ServerException is expected since not Transactional")
        void givenNoSession_whenGetAll_thenThrowLazyInitializationException() throws Exception {
            // Given
            RequestBuilder requestBuilder = MockMvcRequestBuilders.get(TeamController.URL);
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
                        Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                        IsEqual.equalTo("failed to lazily initialize a collection of role: com.github.senocak.model.Team.players, could not initialize proxy - no Session")));
        }

        @Test
        @Transactional
        @DisplayName("Happy Path")
        void given_whenGetAll_thenReturn200() throws Exception {
            // Given
            RequestBuilder requestBuilder = MockMvcRequestBuilders.get(TeamController.URL);
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.teams",
                    Matchers.hasSize(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.next",
                    IsEqual.equalTo(0)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.total",
                    IsEqual.equalTo(3)));
        }
    }

    @Nested
    @DisplayName("Get single team")
    class GetSingleTest {

        @Test
        @DisplayName("ServerException is expected since invalid team name is sent")
        void givenInvalidTeamName_whenGetSingle_thenThrowServerException() throws Exception {
            // Given
            RequestBuilder requestBuilder = MockMvcRequestBuilders.get(TeamController.URL + "/invalid");
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                    IsEqual.equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                    IsEqual.equalTo(AppConstants.OmaErrorMessageType.NOT_FOUND.getMessageId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                    IsEqual.equalTo(AppConstants.OmaErrorMessageType.NOT_FOUND.getText())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",
                    Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                    IsEqual.equalTo("Team: invalid")));
        }

        @Test
        @DisplayName("ServerException is expected since not Transactional")
        void givenNoSession_whenGetSingle_thenThrowLazyInitializationException() throws Exception {
            // Given
            RequestBuilder requestBuilder = MockMvcRequestBuilders.get(TeamController.URL + "/2796c7d1-90f9-443f-9e5f-372ff9b8e8d5");
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
                        Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                        IsEqual.equalTo("failed to lazily initialize a collection of role: com.github.senocak.model.Team.players, could not initialize proxy - no Session")));
        }

        @Test
        @Transactional
        @DisplayName("Happy Path")
        void given_whenGetSingle_thenReturn200() throws Exception {
            // Given
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            RequestBuilder requestBuilder = MockMvcRequestBuilders.get(TeamController.URL + "/2796c7d1-90f9-443f-9e5f-372ff9b8e8d5");
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.team.id",
                IsEqual.equalTo("2796c7d1-90f9-443f-9e5f-372ff9b8e8d5")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.team.name",
                        IsEqual.equalTo("Team 1")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.team.country",
                        IsEqual.equalTo("Indonesia")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.team.availableCash",
                        IsEqual.equalTo(100000000)));
        }
    }

    @Nested
    @DisplayName("Update team")
    class UpdateTest {
        private final TeamUpdateDto teamUpdateDto = new TeamUpdateDto();

        @Test
        @DisplayName("ServerException is expected since schema is invalid")
        void givenInvalidSchema_whenUpdate_thenThrowServerException() throws Exception {
            // Given
            teamUpdateDto.setName("na");
            teamUpdateDto.setCountry("co");
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .patch(TeamController.URL + "/2796c7d1-90f9-443f-9e5f-372ff9b8e8d5")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(teamUpdateDto));
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
        @DisplayName("ServerException is expected since invalid team name is sent")
        void givenInvalidTeamName_whenUpdate_thenThrowServerException() throws Exception {
            // Given
            teamUpdateDto.setName("Team Updated");
            teamUpdateDto.setCountry("USA Updated");
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                .patch(TeamController.URL + "/invalid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(teamUpdateDto));
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                    IsEqual.equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                    IsEqual.equalTo(AppConstants.OmaErrorMessageType.NOT_FOUND.getMessageId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                    IsEqual.equalTo(AppConstants.OmaErrorMessageType.NOT_FOUND.getText())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",
                    Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                    IsEqual.equalTo("Team: invalid")));
        }

        @Test
        @DisplayName("ServerException is expected since not Transactional")
        void givenNoSession_whenUpdate_thenThrowLazyInitializationException() throws Exception {
            // Given
            teamUpdateDto.setName("Team Updated");
            teamUpdateDto.setCountry("USA Updated");
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .patch(TeamController.URL + "/2796c7d1-90f9-443f-9e5f-372ff9b8e8d5")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(teamUpdateDto));
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
                        Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                        IsEqual.equalTo("failed to lazily initialize a collection of role: com.github.senocak.model.Team.players, could not initialize proxy - no Session")));
        }

        @Test
        @Transactional
        @DisplayName("Happy Path")
        void given_whenUpdate_thenReturn200() throws Exception {
            // Given
            teamUpdateDto.setName("Team Updated");
            teamUpdateDto.setCountry("USA");
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .patch(TeamController.URL + "/2796c7d1-90f9-443f-9e5f-372ff9b8e8d5")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(teamUpdateDto));
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.team.id",
                        IsEqual.equalTo("2796c7d1-90f9-443f-9e5f-372ff9b8e8d5")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.team.name",
                        IsEqual.equalTo("Team Updated")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.team.country",
                        IsEqual.equalTo("USA")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.team.availableCash",
                        IsEqual.equalTo(100000000)));
        }
    }

    @Nested
    @DisplayName("Delete team")
    class DeleteTest {

        @Test
        @DisplayName("ServerException is expected since invalid team name is sent")
        void givenInvalidTeamName_whenDelete_thenThrowServerException() throws Exception {
            // Given
            RequestBuilder requestBuilder = MockMvcRequestBuilders.delete(TeamController.URL + "/invalid");
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                        IsEqual.equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                        IsEqual.equalTo(AppConstants.OmaErrorMessageType.NOT_FOUND.getMessageId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                        IsEqual.equalTo(AppConstants.OmaErrorMessageType.NOT_FOUND.getText())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",
                        Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                        IsEqual.equalTo("Team: invalid")));
        }

        @Test
        @Transactional
        @DisplayName("Happy Path")
        void given_whenDelete_thenReturn200() throws Exception {
            // Given
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .delete(TeamController.URL + "/2796c7d1-90f9-443f-9e5f-372ff9b8e8d5");
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform.andExpect(MockMvcResultMatchers.status().isNoContent());
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
