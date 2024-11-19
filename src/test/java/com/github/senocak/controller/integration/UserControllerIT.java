package com.github.senocak.controller.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.senocak.config.SpringBootTestConfig;
import com.github.senocak.controller.UserController;
import com.github.senocak.dto.loan.LoanCreateDto;
import com.github.senocak.dto.loan.LoanDto;
import com.github.senocak.dto.loan.PaymentRequest;
import com.github.senocak.dto.user.UserUpdateDto;
import com.github.senocak.exception.RestExceptionHandler;
import com.github.senocak.exception.ServerException;
import com.github.senocak.model.Loan;
import com.github.senocak.model.User;
import com.github.senocak.repository.LoanRepository;
import com.github.senocak.repository.UserRepository;
import com.github.senocak.service.UserService;
import com.github.senocak.util.AppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
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

@Transactional
@SpringBootTestConfig
@WithMockUser(authorities = "ROLE_ADMIN")
@DisplayName("Integration Tests for UserController")
public class UserControllerIT {
    @Autowired UserController userController;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired LoanRepository loanRepository;
    @SpyBean UserService userService;
    private MockMvc mockMvc;
    private User user;

    @BeforeEach
    void beforeEach() throws ServerException {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(RestExceptionHandler.class)
                .build();
        final Optional<User> byEmail = userRepository.findByEmail(USER_EMAIL);
        if (byEmail.isEmpty()) {
            throw new RuntimeException("user not found");
        }
        user = byEmail.get();
        doReturn(user).when(userService).loggedInUser();
    }

    @Test
    @DisplayName("Get me")
    void given_whenGetMe_thenReturn200() throws Exception {
        // Given
        final RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get(UserController.URL + "/me");
        // When
        final ResultActions perform = mockMvc.perform(requestBuilder);
        // Then
        perform
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.user.name", equalTo(user.getName())))
            .andExpect(MockMvcResultMatchers.jsonPath("$.user.surname", equalTo(user.getSurname())))
            .andExpect(MockMvcResultMatchers.jsonPath("$.user.email", equalTo(user.getEmail())))
            .andExpect(MockMvcResultMatchers.jsonPath("$.user.roles", hasSize(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.user.creditLimit", notNullValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.user.usedCreditLimit", notNullValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.user.loans", notNullValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.user.loans[0].amount", notNullValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.user.loans[0].paid", equalTo(false)));
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
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode", equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id", equalTo(AppConstants.OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.getMessageId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text", equalTo(AppConstants.OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.getText())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables", hasSize(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]", is(notNullValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[1]", is(notNullValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[2]", is(notNullValue())));
        }

        @Test
        @DisplayName("Happy Path")
        void given_whenPatchMe_thenReturn200() throws Exception {
            // Given
            updateUserDto.setName(USER_NAME);
            updateUserDto.setPassword(USER_PASSWORD);
            updateUserDto.setPassword_confirmation(USER_PASSWORD);
            RequestBuilder requestBuilder = MockMvcRequestBuilders
                    .patch(UserController.URL + "/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(writeValueAsString(updateUserDto));
            // When
            ResultActions perform = mockMvc.perform(requestBuilder);
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.message", equalTo("User updated.")));
        }
    }

    @Test
    @DisplayName("Get All Loans")
    void given_whenGetAllLoans_thenReturn200() throws Exception {
        // Given
        final RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get(UserController.URL + "/loans")
                .param("userId", user.getId())
                .param("installments", "true")
                .param("isPaid", "false")
                .param("amountGreaterThan", BigDecimal.ONE.toString())
                .param("numberOfInstallment", "5")
                .param("nextPage", "0")
                .param("maxNumber", "50");
        // When
        final ResultActions perform = mockMvc.perform(requestBuilder);
        // Then
        perform
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.total", equalTo(1)))
            .andExpect(jsonPath("$.next", equalTo(0)))
            .andExpect(jsonPath("$.loans", hasSize(1)))
            .andExpect(jsonPath("$.loans[0].amount", equalTo(1_000.0)))
            .andExpect(jsonPath("$.loans[0].numberOfInstallment", equalTo(5)))
            .andExpect(jsonPath("$.loans[0].installments", hasSize(5)));
    }

    @Test
    @DisplayName("Get Single Loans")
    void given_whenGetSingleLoan_thenReturn200() throws Exception {
        // Given
        final List<Loan> all = loanRepository.findAll();
        if (all.isEmpty()) {
            throw new RuntimeException("loans not found");
        }
        final RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get(UserController.URL + "/loans/" + all.get(0).getId());
        // When
        final ResultActions perform = mockMvc.perform(requestBuilder);
        // Then
        perform
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.amount", notNullValue()))
            .andExpect(jsonPath("$.numberOfInstallment", notNullValue()))
            .andExpect(jsonPath("$.installments", notNullValue()));
    }

    @Test
    @DisplayName("Create New Loan")
    void given_whenCreateLoan_thenReturn200() throws Exception {
        // When
        final LoanDto createLoan = createLoan();
        // Then
        assertEquals(createLoan.getAmount(), BigDecimal.valueOf(13.0));
        assertEquals(createLoan.getNumberOfInstallment(), 6);
        assertEquals(createLoan.getInstallments().size(), 6);
    }

    private LoanDto createLoan() throws Exception {
        // Given
        final LoanCreateDto loanCreateDto = new LoanCreateDto();
        loanCreateDto.setAmount(BigDecimal.TEN);
        loanCreateDto.setInterestRate(BigDecimal.valueOf(0.3));
        loanCreateDto.setNumberOfInstallments(6);
        loanCreateDto.setCustomerId(user.getId());
        final RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post(UserController.URL + "/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(loanCreateDto));
        // When
        final ResultActions perform = mockMvc.perform(requestBuilder);
        // Then
        return objectMapper.readValue(perform.andReturn().getResponse().getContentAsString(), LoanDto.class);
    }

    @Test
    @DisplayName("Pay Loan")
    void given_whenPayLoan_thenReturn200() throws Exception {
        // Given
        final LoanDto createLoan = createLoan();
        final PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(BigDecimal.TEN);

        final RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post(UserController.URL + "/loans/" + createLoan.getId() + "/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(paymentRequest));
        // When
        final ResultActions perform = mockMvc.perform(requestBuilder);
        // Then
        perform
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.installmentsPaid", equalTo("3")))
            .andExpect(jsonPath("$.totalPaid", equalTo("6.51")))
            .andExpect(jsonPath("$.isLoanPaid", equalTo("false")));
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
