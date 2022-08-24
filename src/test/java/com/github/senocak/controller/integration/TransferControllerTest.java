package com.github.senocak.controller.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.senocak.config.SpringBootTestConfig;
import com.github.senocak.controller.TransferController;
import com.github.senocak.dto.transfer.TransferCreateDto;
import com.github.senocak.dto.transfer.TransferUpdateDto;
import com.github.senocak.exception.RestExceptionHandler;
import com.github.senocak.exception.ServerException;
import com.github.senocak.factory.TeamFactory;
import com.github.senocak.factory.UserFactory;
import com.github.senocak.model.Team;
import com.github.senocak.model.User;
import com.github.senocak.repository.TeamRepository;
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
import java.util.Optional;
import java.util.UUID;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * This integration test class is written for
 * @see TransferController
 * 27 tests
 */
@SpringBootTestConfig
@DisplayName("Integration Tests for TransferController")
public class TransferControllerTest {
    @Autowired TransferController transferController;
    @Autowired TeamRepository teamRepository;
    @Autowired ObjectMapper objectMapper;
    @MockBean UserService userService;
    private MockMvc mockMvc;
    private final User user = UserFactory.createUser(null);

    @BeforeEach
    void beforeEach() throws ServerException {
        mockMvc = MockMvcBuilders.standaloneSetup(transferController)
                .setControllerAdvice(RestExceptionHandler.class)
                .build();
        Mockito.lenient().doReturn(user).when(userService).loggedInUser();
    }

    @Nested
    @DisplayName("Create new transfer")
    class CreateTest {
        private final TransferCreateDto transferCreateDto = new TransferCreateDto();

        @Test
        @DisplayName("ServerException is expected since schema is invalid")
        void givenInvalidSchema_whenCreate_thenThrowServerException() throws Exception {
            // Given
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(TransferController.URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(transferCreateDto));
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
        @DisplayName("ServerException is expected since player is not found")
        void givenNoPlayer_whenCreate_thenServerException() throws Exception {
            // Given
            transferCreateDto.setPlayerId(UUID.randomUUID());
            transferCreateDto.setAskedPrice(1_000_000);
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(TransferController.URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(transferCreateDto));
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
                        IsEqual.equalTo("Player: "+transferCreateDto.getPlayerId().toString())));
        }

        @Test
        @DisplayName("ServerException is expected since not Transactional")
        void givenNoSession_whenCreate_thenThrowLazyInitializationException() throws Exception {
            // Given
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            transferCreateDto.setPlayerId(UUID.fromString("18786fec-802f-4687-885c-063b736b2fad"));
            // This uuid is in transfer list and not transferred yet
            transferCreateDto.setAskedPrice(1_000_000);
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(TransferController.URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(transferCreateDto));
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
                        IsEqual.equalTo("could not initialize proxy [com.github.senocak.model.Player#18786fec-802f-4687-885c-063b736b2fad] - no Session")));
        }

        @Test
        @Transactional
        @DisplayName("ServerException is expected since transfer exists")
        void givenSameTransfer_whenCreate_thenThrowServerException() throws Exception {
            // Given
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            transferCreateDto.setPlayerId(UUID.fromString("18786fec-802f-4687-885c-063b736b2fad"));
            // This uuid is in transfer list and not transferred yet
            transferCreateDto.setAskedPrice(1_000_000);
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(TransferController.URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(transferCreateDto));
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
                        IsEqual.equalTo("Transfer John2-17 already in list")));
        }

        @Test
        @Transactional
        @DisplayName("Happy Path")
        void given_whenCreate_thenReturn200() throws Exception {
            // Given
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            transferCreateDto.setPlayerId(UUID.fromString("1343897b-1bf2-4270-bfae-e4ce9ae05c5a"));
            // This uuid is in transfer list and not transferred yet
            transferCreateDto.setAskedPrice(1_000_000);
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(TransferController.URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(transferCreateDto));
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.id",
                    IsNull.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.player.id",
                        IsEqual.equalTo(transferCreateDto.getPlayerId().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.askedPrice",
                        IsEqual.equalTo(transferCreateDto.getAskedPrice())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.transferred",
                        IsEqual.equalTo(false)));
        }
    }

    @Nested
    @DisplayName("Get all transfers")
    class GetAllTest {

        @Test
        @DisplayName("ServerException is expected since invalid next query param is sent")
        void givenInvalidNextQueryParam_whenGetAll_thenThrowServerException() throws Exception {
            // Given
            RequestBuilder requestBuilder = MockMvcRequestBuilders.get(TransferController.URL + "?next=ipsum");
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
            RequestBuilder requestBuilder = MockMvcRequestBuilders.get(TransferController.URL);
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
                        IsEqual.equalTo("could not initialize proxy [com.github.senocak.model.Player#18786fec-802f-4687-885c-063b736b2fad] - no Session")));
        }

        @Test
        @Transactional
        @DisplayName("Happy Path")
        void given_whenGetAll_thenReturn200() throws Exception {
            // Given
            RequestBuilder requestBuilder = MockMvcRequestBuilders.get(TransferController.URL);
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfers",
                    Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.next",
                    IsEqual.equalTo(0)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.total",
                    IsEqual.equalTo(2)));
        }
    }

    @Nested
    @DisplayName("Get single transfer")
    class GetSingleTest {

        @Test
        @DisplayName("ServerException is expected since transfer is not valid")
        void givenInvalidTransfer_whenGetSingle_thenThrowServerException() throws Exception {
            // Given
            RequestBuilder requestBuilder = MockMvcRequestBuilders.get(TransferController.URL + "/invalid");
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
                    IsEqual.equalTo("Transfer: invalid")));
        }

        @Test
        @DisplayName("ServerException is expected since not Transactional")
        void givenNoSession_whenGetSingle_thenThrowLazyInitializationException() throws Exception {
            // Given
            RequestBuilder requestBuilder = MockMvcRequestBuilders.get(TransferController.URL + "/b6dad2de-a0ff-4148-b2a4-ea5cc97ff3be");
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
                        IsEqual.equalTo("could not initialize proxy [com.github.senocak.model.Player#18786fec-802f-4687-885c-063b736b2fad] - no Session")));
        }

        @Test
        @Transactional
        @DisplayName("Happy Path")
        void given_whenGetSingle_thenReturn200() throws Exception {
            // Given
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            RequestBuilder requestBuilder = MockMvcRequestBuilders.get(TransferController.URL + "/b6dad2de-a0ff-4148-b2a4-ea5cc97ff3be");
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.id",
                IsEqual.equalTo("b6dad2de-a0ff-4148-b2a4-ea5cc97ff3be")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.marketValue",
                        IsEqual.equalTo(380000)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.askedPrice",
                        IsEqual.equalTo(20000)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.transferred",
                        IsEqual.equalTo(false)));
        }
    }

    @Nested
    @DisplayName("Update transfer")
    class UpdateTest {
        private final TransferUpdateDto transferUpdateDto = new TransferUpdateDto();

        @Test
        @DisplayName("ServerException is expected since schema is invalid")
        void givenInvalidSchema_whenUpdate_thenThrowServerException() throws Exception {
            // Given
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .patch(TransferController.URL + "/b6dad2de-a0ff-4148-b2a4-ea5cc97ff3be")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(transferUpdateDto));
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
                    IsEqual.equalTo("askedPrice: must be greater than or equal to 1000")));
        }

        @Test
        @DisplayName("ServerException is expected since invalid transfer is sent")
        void givenInvalidTransfer_whenUpdate_thenThrowServerException() throws Exception {
            // Given
            transferUpdateDto.setAskedPrice(2_000);
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                .patch(TransferController.URL + "/invalid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(transferUpdateDto));
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
                    IsEqual.equalTo("Transfer: invalid")));
        }

        @Test
        @DisplayName("ServerException is expected since not Transactional")
        void givenNoSession_whenUpdate_thenThrowLazyInitializationException() throws Exception {
            // Given
            transferUpdateDto.setAskedPrice(2_000);
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .patch(TransferController.URL + "/b6dad2de-a0ff-4148-b2a4-ea5cc97ff3be")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(transferUpdateDto));
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
                        IsEqual.equalTo("could not initialize proxy [com.github.senocak.model.Player#18786fec-802f-4687-885c-063b736b2fad] - no Session")));
        }

        @Test
        @Transactional
        @DisplayName("ServerException is expected since transfer is transferred")
        void givenTransferred_whenUpdate_thenServerException() throws Exception {
            // Given
            transferUpdateDto.setAskedPrice(2_000);
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            Optional<Team> byId = teamRepository.findById("921e2f34-7335-405e-a05e-2d4ac1bcc02d");
            user.setTeam(byId.orElseGet(() -> TeamFactory.createTeam(null)));
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .patch(TransferController.URL + "/b6dad2de-a0ff-4148-b2a4-ea5cc97ff4be")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(transferUpdateDto));
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                        IsEqual.equalTo(HttpStatus.FORBIDDEN.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                        IsEqual.equalTo(AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT.getMessageId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                        IsEqual.equalTo(AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT.getText())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",
                        Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                        IsEqual.equalTo("Transfer is already transferred")));
        }

        @Test
        @Transactional
        @DisplayName("Happy Path")
        void given_whenUpdate_thenReturn200() throws Exception {
            // Given
            transferUpdateDto.setAskedPrice(2_000);
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .patch(TransferController.URL + "/b6dad2de-a0ff-4148-b2a4-ea5cc97ff3be")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(transferUpdateDto));
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.id",
                        IsEqual.equalTo("b6dad2de-a0ff-4148-b2a4-ea5cc97ff3be")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.player.id",
                        IsEqual.equalTo("18786fec-802f-4687-885c-063b736b2fad")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.marketValue",
                        IsEqual.equalTo(380_000)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.askedPrice",
                        IsEqual.equalTo(2_000)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.transferred",
                        IsEqual.equalTo(false)));
        }
    }

    @Nested
    @DisplayName("Confirm transfer")
    class ConfirmTest {
        @Test
        @DisplayName("ServerException is expected since invalid transfer is sent")
        void givenInvalidTransfer_whenConfirm_thenThrowServerException() throws Exception {
            // Given
            RequestBuilder requestBuilder = MockMvcRequestBuilders.patch(TransferController.URL + "/invalid/confirm");
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
                        IsEqual.equalTo("Transfer: invalid")));
        }

        @Test
        @DisplayName("ServerException is expected since user has no transfer")
        void givenUserNoTeam_whenConfirm_thenThrowServerException() throws Exception {
            // Given
            user.setTeam(null);
            RequestBuilder requestBuilder = MockMvcRequestBuilders.patch(
                    TransferController.URL + "/b6dad2de-a0ff-4148-b2a4-ea5cc97ff3be/confirm");
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
                        IsEqual.equalTo("User has no team")));
        }

        @Test
        @DisplayName("ServerException is expected since not Transactional")
        void givenNoSession_whenConfirm_thenThrowLazyInitializationException() throws Exception {
            // Given
            user.setTeam(TeamFactory.createTeam(null));
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_ADMIN));
            RequestBuilder requestBuilder = MockMvcRequestBuilders.patch(
                    TransferController.URL + "/b6dad2de-a0ff-4148-b2a4-ea5cc97ff3be/confirm");
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
                        IsEqual.equalTo("could not initialize proxy [com.github.senocak.model.Player#18786fec-802f-4687-885c-063b736b2fad] - no Session")));
        }

        @Test
        @Transactional
        @DisplayName("ServerException is expected since user has no enough money")
        void givenNotEnoughMoney_whenConfirm_thenServerException() throws Exception {
            // Given
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            Team team = TeamFactory.createTeam(null);
            team.setAvailableCash(1);
            user.setTeam(team);
            RequestBuilder requestBuilder = MockMvcRequestBuilders.patch(
                    TransferController.URL + "/b6dad2de-a0ff-4148-b2a4-ea5cc97ff4be/confirm");
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
                        IsEqual.equalTo("Buyer team has not enough cash")));
        }

        @Test
        @Transactional
        @DisplayName("Happy Path")
        void given_whenConfirm_thenReturn200() throws Exception {
            // Given
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            Team team = TeamFactory.createTeam(null);
            team.setAvailableCash(100_000_000);
            user.setTeam(team);
            RequestBuilder requestBuilder = MockMvcRequestBuilders.patch(
                    TransferController.URL + "/b6dad2de-a0ff-4148-b2a4-ea5cc97ff3be/confirm");
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.id",
                        IsEqual.equalTo("b6dad2de-a0ff-4148-b2a4-ea5cc97ff3be")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.player.id",
                        IsEqual.equalTo("18786fec-802f-4687-885c-063b736b2fad")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.player.firstName",
                        IsEqual.equalTo("John2-17")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.player.lastName",
                        IsEqual.equalTo("Doe2-17")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.player.country",
                        IsEqual.equalTo("USA17")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.player.age",
                        IsEqual.equalTo(37)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.player.position",
                        IsEqual.equalTo("Forward")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.player.marketValue",
                        IsEqual.equalTo(1_000_000)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.askedPrice",
                        IsEqual.equalTo(20_000)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.transferred",
                        IsEqual.equalTo(true)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.transferredFrom.name",
                        IsEqual.equalTo("Team2")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.transferredFrom.country",
                        IsEqual.equalTo("Turkey")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.transferredFrom.availableCash",
                        IsEqual.equalTo(1_990_000)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.transferredTo.name",
                        IsEqual.equalTo("Random Team")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.transferredTo.country",
                        IsEqual.equalTo("Turkey")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.transferredTo.availableCash",
                        IsEqual.equalTo(100_000_000)));
        }
    }

    @Nested
    @DisplayName("Delete transfer")
    class DeleteTest {
        @Test
        @DisplayName("ServerException is expected since invalid transfer is sent")
        void  givenInvalidTransferName_whenDelete_thenThrowServerException() throws Exception {
            // Given
            RequestBuilder requestBuilder = MockMvcRequestBuilders.delete(TransferController.URL + "/invalid");
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
                        IsEqual.equalTo("Transfer: invalid")));
        }

        @Test
        @DisplayName("ServerException is expected since not Transactional")
        void givenNoSession_whenConfirm_thenThrowLazyInitializationException() throws Exception {
            // Given
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_ADMIN));
            RequestBuilder requestBuilder = MockMvcRequestBuilders.delete(TransferController.URL + "/b6dad2de-a0ff-4148-b2a4-ea5cc97ff3be");
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
        @DisplayName("ServerException is expected since invalid role is sent")
        void  givenInvalidRole_whenDelete_thenThrowServerException() throws Exception {
            // Given
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_ADMIN));
            RequestBuilder requestBuilder = MockMvcRequestBuilders.delete(TransferController.URL + "/b6dad2de-a0ff-4148-b2a4-ea5cc97ff3be");
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                        IsEqual.equalTo(HttpStatus.UNAUTHORIZED.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                        IsEqual.equalTo(AppConstants.OmaErrorMessageType.UNAUTHORIZED.getMessageId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                        IsEqual.equalTo(AppConstants.OmaErrorMessageType.UNAUTHORIZED.getText())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",
                        Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                        IsEqual.equalTo("This user does not have the right permission for transfer operation")));
        }

        @Test
        @Transactional
        @DisplayName("ServerException is expected since user has no transfer")
        void givenNoTransfer_whenDelete_thenThrowServerException() throws Exception {
            // Given
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .delete(TransferController.URL + "/b6dad2de-a0ff-4148-b2a4-ea5cc97ff3be");
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
                        IsEqual.equalTo("User has no team")));

        }

        @Test
        @Transactional
        @DisplayName("ServerException is expected since transfer is already transferred")
        void givenAlreadyTransferred_whenDelete_thenThrowServerException() throws Exception {
            // Given
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            user.setTeam(TeamFactory.createTeam(null));
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .delete(TransferController.URL + "/b6dad2de-a0ff-4148-b2a4-ea5cc97ff4be");
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
                        IsEqual.equalTo("Transfer is already transferred")));

        }

        @Test
        @Transactional
        @DisplayName("Happy Path")
        void given_whenDelete_thenReturn200() throws Exception {
            // Given
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            user.setTeam(TeamFactory.createTeam(null));
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .delete(TransferController.URL + "/b6dad2de-a0ff-4148-b2a4-ea5cc97ff3be");
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
