package com.github.senocak.controller.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.senocak.config.SpringBootTestConfig;
import com.github.senocak.controller.PlayerController;
import com.github.senocak.dto.player.PlayerCreateDto;
import com.github.senocak.dto.player.PlayerPatchDto;
import com.github.senocak.dto.player.PlayerPutDto;
import com.github.senocak.exception.RestExceptionHandler;
import com.github.senocak.exception.ServerException;
import com.github.senocak.factory.UserFactory;
import com.github.senocak.model.User;
import com.github.senocak.service.PlayerService;
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
import java.util.UUID;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * This integration test class is written for
 * @see PlayerController
 * 20 tests
 */
@SpringBootTestConfig
@DisplayName("Integration Tests for PlayerController")
public class PlayerControllerTest {
    @Autowired PlayerController playerController;
    @Autowired ObjectMapper objectMapper;
    @MockBean UserService userService;
    private MockMvc mockMvc;
    private final User user = UserFactory.createUser(null);

    @BeforeEach
    void beforeEach() throws ServerException {
        mockMvc = MockMvcBuilders.standaloneSetup(playerController)
                .setControllerAdvice(RestExceptionHandler.class)
                .build();
        Mockito.lenient().doReturn(user).when(userService).loggedInUser();
    }

    @Nested
    @DisplayName("Create new player")
    class CreateTest {
        private final PlayerCreateDto playerCreateDto = new PlayerCreateDto();

        @Test
        @DisplayName("ServerException is expected since schema is invalid")
        void givenInvalidSchema_whenCreate_thenThrowServerException() throws Exception {
            // Given
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(PlayerController.URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(playerCreateDto));
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
                Matchers.hasSize(5)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                is(notNullValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[1]",
                is(notNullValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[2]",
                is(notNullValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[3]",
                is(notNullValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[4]",
                is(notNullValue())));
        }

        @Test
        @DisplayName("ServerException is expected since not Transactional")
        void givenNoSession_whenCreate_thenThrowLazyInitializationException() throws Exception {
            // Given
            playerCreateDto.setFirstName("John1-7");
            playerCreateDto.setLastName("Doe1-7");
            playerCreateDto.setCountry("USA7");
            playerCreateDto.setAge(27);
            playerCreateDto.setPosition("Defender");
            playerCreateDto.setMarketValue(1_000_000);
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(PlayerController.URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(playerCreateDto));
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
                        IsEqual.equalTo("could not initialize proxy [com.github.senocak.model.Team#921e2f34-7335-405e-a05e-2d4ac1bcc02d] - no Session")));
        }

        @Test
        @Transactional
        @DisplayName("ServerException is expected since player exists")
        void givenSamePlayer_whenCreate_thenThrowServerException() throws Exception {
            // Given
            playerCreateDto.setFirstName("John1-7");
            playerCreateDto.setLastName("Doe1-7");
            playerCreateDto.setCountry("USA7");
            playerCreateDto.setAge(27);
            playerCreateDto.setPosition("Defender");
            playerCreateDto.setMarketValue(1_000_000);
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(PlayerController.URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(playerCreateDto));
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
                IsEqual.equalTo("Player " + playerCreateDto.getFirstName() + " already exist")));
        }

        @Test
        @Transactional
        @DisplayName("ServerException is expected since position is invalid")
        void givenInvalidPosition_whenCreate_thenThrowServerException() throws Exception {
            // Given
            playerCreateDto.setFirstName("John"+System.currentTimeMillis());
            playerCreateDto.setLastName("Doe1"+System.currentTimeMillis());
            playerCreateDto.setCountry("USA");
            playerCreateDto.setAge(27);
            playerCreateDto.setPosition("InvalidPosition");
            playerCreateDto.setMarketValue(1_000_000);
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(PlayerController.URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(playerCreateDto));
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
                    IsEqual.equalTo("Invalid position:"+playerCreateDto.getPosition())));
        }

        @Test
        @DisplayName("Happy Path")
        void given_whenCreate_thenReturn200() throws Exception {
            // Given
            playerCreateDto.setFirstName("John"+System.currentTimeMillis());
            playerCreateDto.setLastName("Doe1"+System.currentTimeMillis());
            playerCreateDto.setCountry("USA");
            playerCreateDto.setAge(27);
            playerCreateDto.setPosition("Defender");
            playerCreateDto.setMarketValue(1_000_000);
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(PlayerController.URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(playerCreateDto));
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.id",
                    IsNull.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.firstName",
                    IsEqual.equalTo(playerCreateDto.getFirstName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.lastName",
                    IsEqual.equalTo(playerCreateDto.getLastName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.country",
                    IsEqual.equalTo(playerCreateDto.getCountry())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.age",
                    IsEqual.equalTo(playerCreateDto.getAge())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.position",
                    IsEqual.equalTo(playerCreateDto.getPosition())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.marketValue",
                    IsEqual.equalTo(playerCreateDto.getMarketValue())));
        }
    }

    @Nested
    @DisplayName("Get all players")
    class GetAllTest {
        @Test
        @DisplayName("ServerException is expected since invalid next query param is sent")
        void givenInvalidNextQueryParam_whenGetAll_thenThrowServerException() throws Exception {
            // Given
            RequestBuilder requestBuilder = MockMvcRequestBuilders.get(PlayerController.URL + "?next=ipsum");
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
            RequestBuilder requestBuilder = MockMvcRequestBuilders.get(PlayerController.URL);
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
                        IsEqual.equalTo("could not initialize proxy [com.github.senocak.model.Team#921e2f34-7335-405e-a05e-2d4ac1bcc02d] - no Session")));
        }

        @Test
        @Transactional
        @DisplayName("Happy Path")
        void given_whenGetAll_thenReturn200() throws Exception {
            // Given
            RequestBuilder requestBuilder = MockMvcRequestBuilders.get(PlayerController.URL);
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.players",
                    Matchers.hasSize(10)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.next",
                    IsEqual.equalTo(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.total",
                    IsEqual.equalTo(40)));
        }
    }

    @Nested
    @DisplayName("Get single player")
    class GetSingleTest {

        @Test
        @DisplayName("ServerException is expected since invalid player name is sent")
        void givenInvalidPlayerName_whenGetSingle_thenThrowServerException() throws Exception {
            // Given
            RequestBuilder requestBuilder = MockMvcRequestBuilders.get(PlayerController.URL + "/invalid");
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
                    IsEqual.equalTo("Player: invalid")));
        }

        @Test
        @DisplayName("ServerException is expected since not Transactional")
        void givenNoSession_whenGetSingle_thenThrowLazyInitializationException() throws Exception {
            // Given
            RequestBuilder requestBuilder = MockMvcRequestBuilders.get(PlayerController.URL + "/1343897b-1bf2-4270-bfae-e4ce9ae05c5a");
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
                        IsEqual.equalTo("could not initialize proxy [com.github.senocak.model.Team#921e2f34-7335-405e-a05e-2d4ac1bcc02d] - no Session")));
        }

        @Test
        @Transactional
        @DisplayName("Happy Path")
        void given_whenGetSingle_thenReturn200() throws Exception {
            // Given
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            RequestBuilder requestBuilder = MockMvcRequestBuilders.get(PlayerController.URL + "/1343897b-1bf2-4270-bfae-e4ce9ae05c5a");
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.id",
                IsEqual.equalTo("1343897b-1bf2-4270-bfae-e4ce9ae05c5a")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.firstName",
                        IsEqual.equalTo("John1-7")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.lastName",
                        IsEqual.equalTo("Doe1-7")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.country",
                        IsEqual.equalTo("USA7")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.age",
                        IsEqual.equalTo(27)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.position",
                        IsEqual.equalTo("Defender")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.marketValue",
                        IsEqual.equalTo(1_000_000)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.team.id",
                    IsEqual.equalTo("921e2f34-7335-405e-a05e-2d4ac1bcc02d")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.team.name",
                        IsEqual.equalTo("Team2")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.team.country",
                        IsEqual.equalTo("Turkey")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.team.availableCash",
                        IsEqual.equalTo(1_990_000)));
        }
    }

    @Nested
    @DisplayName("Update player")
    class UpdateTest {
        private final PlayerPutDto playerPutDto = new PlayerPutDto();

        @Test
        @DisplayName("ServerException is expected since schema is invalid")
        void givenInvalidSchema_whenUpdate_thenThrowServerException() throws Exception {
            // Given
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .put(PlayerController.URL + "/1343897b-1bf2-4270-bfae-e4ce9ae05c5a")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(playerPutDto));
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
                    Matchers.hasSize(7)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                    is(notNullValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[1]",
                    is(notNullValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[2]",
                    is(notNullValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[3]",
                    is(notNullValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[4]",
                    is(notNullValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[5]",
                    is(notNullValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[6]",
                    is(notNullValue())));
        }

        @Test
        @DisplayName("ServerException is expected since invalid player name is sent")
        void givenInvalidPlayerName_whenUpdate_thenThrowServerException() throws Exception {
            // Given
            playerPutDto.setFirstName("John Updated");
            playerPutDto.setLastName("Doe Updated");
            playerPutDto.setCountry("USA Updated");
            playerPutDto.setAge(28);
            playerPutDto.setMarketValue(1_000_001);
            playerPutDto.setPosition("Forward");
            playerPutDto.setTeamId(UUID.fromString("921e2f34-7335-405e-a05e-2d4ac1bcc02d"));
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                .put(PlayerController.URL + "/invalid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(playerPutDto));
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
                    IsEqual.equalTo("Player: invalid")));
        }

        @Test
        @DisplayName("ServerException is expected since not Transactional")
        void givenNoSession_whenUpdate_thenThrowLazyInitializationException() throws Exception {
            // Given
            playerPutDto.setFirstName("John Updated");
            playerPutDto.setLastName("Doe Updated");
            playerPutDto.setCountry("USA Updated");
            playerPutDto.setAge(28);
            playerPutDto.setMarketValue(1_000_001);
            playerPutDto.setPosition("Forward");
            playerPutDto.setTeamId(UUID.fromString("921e2f34-7335-405e-a05e-2d4ac1bcc02d"));
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .put(PlayerController.URL + "/1343897b-1bf2-4270-bfae-e4ce9ae05c5a")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(playerPutDto));
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
                        IsEqual.equalTo("could not initialize proxy [com.github.senocak.model.Team#921e2f34-7335-405e-a05e-2d4ac1bcc02d] - no Session")));
        }

        @Test
        @Transactional
        @DisplayName("Happy Path")
        void given_whenUpdate_thenReturn200() throws Exception {
            // Given
            playerPutDto.setFirstName("John Updated");
            playerPutDto.setLastName("Doe Updated");
            playerPutDto.setCountry("USA Updated");
            playerPutDto.setAge(28);
            playerPutDto.setMarketValue(1_000_001);
            playerPutDto.setPosition("Forward");
            playerPutDto.setTeamId(UUID.fromString("921e2f34-7335-405e-a05e-2d4ac1bcc02d"));
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .put(PlayerController.URL + "/1343897b-1bf2-4270-bfae-e4ce9ae05c5a")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(playerPutDto));
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.id",
                        IsEqual.equalTo("1343897b-1bf2-4270-bfae-e4ce9ae05c5a")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.firstName",
                        IsEqual.equalTo("John Updated")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.lastName",
                        IsEqual.equalTo("Doe Updated")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.country",
                        IsEqual.equalTo("USA Updated")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.age",
                        IsEqual.equalTo(28)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.position",
                        IsEqual.equalTo("Forward")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.marketValue",
                        IsEqual.equalTo(1_000_001)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.team.id",
                        IsEqual.equalTo("921e2f34-7335-405e-a05e-2d4ac1bcc02d")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.team.name",
                        IsEqual.equalTo("Team2")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.team.country",
                        IsEqual.equalTo("Turkey")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.team.availableCash",
                        IsEqual.equalTo(1_990_000)));
        }
    }

    @Nested
    @DisplayName("Update player partially")
    class UpdatePartiallyTest {
        private final PlayerPatchDto playerPatchDto = new PlayerPatchDto();

        @Test
        @DisplayName("ServerException is expected since invalid player name is sent")
        void givenInvalidPlayerName_whenUpdate_thenThrowServerException() throws Exception {
            // Given
            playerPatchDto.setFirstName("John Updated");
            playerPatchDto.setLastName("Doe Updated");
            playerPatchDto.setCountry("USA Updated");
            playerPatchDto.setAge(28);
            playerPatchDto.setMarketValue(1_000_001);
            playerPatchDto.setPosition("Forward");
            playerPatchDto.setTeamId(UUID.fromString("921e2f34-7335-405e-a05e-2d4ac1bcc02d"));
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .patch(PlayerController.URL + "/invalid")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(playerPatchDto));
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
                        IsEqual.equalTo("Player: invalid")));
        }

        @Test
        @DisplayName("ServerException is expected since not Transactional")
        void givenNoSession_whenUpdate_thenThrowLazyInitializationException() throws Exception {
            // Given
            playerPatchDto.setFirstName("John Updated");
            playerPatchDto.setLastName("Doe Updated");
            playerPatchDto.setCountry("USA Updated");
            playerPatchDto.setAge(28);
            playerPatchDto.setMarketValue(1_000_001);
            playerPatchDto.setPosition("Forward");
            playerPatchDto.setTeamId(UUID.fromString("921e2f34-7335-405e-a05e-2d4ac1bcc02d"));
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .patch(PlayerController.URL + "/1343897b-1bf2-4270-bfae-e4ce9ae05c5a")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(playerPatchDto));
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
                        IsEqual.equalTo("could not initialize proxy [com.github.senocak.model.Team#921e2f34-7335-405e-a05e-2d4ac1bcc02d] - no Session")));
        }

        @Test
        @Transactional
        @DisplayName("Happy Path")
        void given_whenUpdate_thenReturn200() throws Exception {
            // Given
            playerPatchDto.setFirstName("John Updated");
            playerPatchDto.setLastName("Doe Updated");
            playerPatchDto.setCountry("USA Updated");
            playerPatchDto.setAge(28);
            playerPatchDto.setMarketValue(1_000_001);
            playerPatchDto.setPosition("Forward");
            playerPatchDto.setTeamId(UUID.fromString("921e2f34-7335-405e-a05e-2d4ac1bcc02d"));
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .patch(PlayerController.URL + "/1343897b-1bf2-4270-bfae-e4ce9ae05c5a")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(playerPatchDto));
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.id",
                        IsEqual.equalTo("1343897b-1bf2-4270-bfae-e4ce9ae05c5a")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.firstName",
                        IsEqual.equalTo("John Updated")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.lastName",
                        IsEqual.equalTo("Doe Updated")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.country",
                        IsEqual.equalTo("USA Updated")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.age",
                        IsEqual.equalTo(28)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.position",
                        IsEqual.equalTo("Forward")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.marketValue",
                        IsEqual.equalTo(1_000_001)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.team.id",
                        IsEqual.equalTo("921e2f34-7335-405e-a05e-2d4ac1bcc02d")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.team.name",
                        IsEqual.equalTo("Team2")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.team.country",
                        IsEqual.equalTo("Turkey")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.player.team.availableCash",
                        IsEqual.equalTo(1_990_000)));
        }
    }

    @Nested
    @DisplayName("Delete player")
    class DeleteTest {

        @Test
        @DisplayName("ServerException is expected since invalid player name is sent")
        void givenInvalidPlayerName_whenDelete_thenThrowServerException() throws Exception {
            // Given
            RequestBuilder requestBuilder = MockMvcRequestBuilders.delete(PlayerController.URL + "/invalid");
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
                        IsEqual.equalTo("Player: invalid")));
        }

        @Test
        @Transactional
        @DisplayName("Happy Path")
        void given_whenDelete_thenReturn200() throws Exception {
            // Given
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .delete(PlayerController.URL + "/1343897b-1bf2-4270-bfae-e4ce9ae05c5a");
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
