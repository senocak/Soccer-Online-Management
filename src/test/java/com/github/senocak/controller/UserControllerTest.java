package com.github.senocak.controller;

import com.github.senocak.TestConstants;
import com.github.senocak.dto.loan.LoanCreateDto;
import com.github.senocak.dto.loan.LoanDto;
import com.github.senocak.dto.loan.LoanWrapperDto;
import com.github.senocak.dto.loan.PaymentRequest;
import com.github.senocak.dto.user.UserUpdateDto;
import com.github.senocak.dto.user.UserWrapperResponse;
import com.github.senocak.exception.ServerException;
import com.github.senocak.factory.LoanFactory;
import com.github.senocak.factory.UserFactory;
import com.github.senocak.model.Loan;
import com.github.senocak.model.LoanInstallment;
import com.github.senocak.model.User;
import com.github.senocak.repository.LoanInstallmentRepository;
import com.github.senocak.repository.LoanRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for UserController")
public class UserControllerTest {
    @InjectMocks UserController userController;

    @Mock UserService userService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock LoanRepository loanRepository;
    @Mock LoanInstallmentRepository loanInstallmentRepository;
    @Mock BindingResult bindingResult;

    private final User user = UserFactory.createUser(LoanFactory.createLoan(null));
    private final Loan loan = LoanFactory.createLoan(user);

    @BeforeEach
    void setup() throws ServerException {
        lenient().when(userService.loggedInUser()).thenReturn(user);
    }

    @Test
    void given_whenGetMe_thenReturn200() throws ServerException {
        // When
        final UserWrapperResponse getMe = userController.getMe(false);
        // Then
        assertNotNull(getMe);
        assertNotNull(getMe.getUserResponse());
        assertEquals(user.getSurname(), getMe.getUserResponse().getSurname());
        assertEquals(user.getEmail(), getMe.getUserResponse().getEmail());
        assertEquals(user.getName(), getMe.getUserResponse().getName());
        Assertions.assertNull(getMe.getToken());
    }

    @Test
    void given_whenPatchMe_thenAssertResult() throws ServerException {
        // Given
        final UserUpdateDto updateUserDto = new UserUpdateDto();
        updateUserDto.setName(TestConstants.USER_NAME);
        updateUserDto.setPassword("pass1");
        updateUserDto.setPassword_confirmation("pass1");
        // When
        final Map<String, String> patchMe = userController.patchMe(updateUserDto, bindingResult);
        // Then
        assertNotNull(patchMe);
        assertEquals(1, patchMe.size());
        assertEquals("User updated.", patchMe.get("message"));
    }

    @Test
    void givenUserIdAndInstallmentsAndIsPaidAndAmountGreaterThanAndNumberOfInstallmentAndNextPageAndMaxNumber_whenGetAllLoans_thenAssertResult() throws ServerException {
        // Given
        when(userService.findById("q")).thenReturn(user);
        final Loan loan = LoanFactory.createLoan();
        final List<Loan> allList = List.of(loan);
        final Page<Loan> all = new PageImpl<>(allList);
        when(loanRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(all);
        // When
        final LoanWrapperDto getAllLoans = userController.getAllLoans("q", false, false,
                BigDecimal.ONE, 1, 0, 100);
        // Then
        assertNotNull(getAllLoans);
        assertNotNull(getAllLoans.getLoans());
        assertEquals(1, getAllLoans.getLoans().size());
        assertEquals(1, getAllLoans.getTotal());
        assertEquals(0, getAllLoans.getNext());
    }

    @Nested
    class GetLoanById {
        @Test
        void givenId_whenGetLoanByIdAndLoanIsNull_thenThrowServerException() {
            // Given
            doReturn(Optional.empty()).when(loanRepository).findById("id");
            // When
            final Executable executable = () -> userController.getLoanById("id");
            // Then
            final ServerException serverException = assertThrows(ServerException.class, executable);
            assertEquals(AppConstants.OmaErrorMessageType.NOT_FOUND, serverException.getOmaErrorMessageType());
            assertEquals(HttpStatus.NOT_FOUND, serverException.getStatusCode());
            assertEquals(1, serverException.getVariables().length);
            final Optional<String> first = Arrays.stream(serverException.getVariables()).findFirst();
            assertTrue(first.isPresent());
            assertEquals("This Loan not found", first.get());
        }

        @Test
        void givenId_whenGetLoanByIdAndLoanIsNotForUser_thenThrowServerException() throws ServerException {
            // Given
            user.setRoles(Set.of());
            doReturn(Optional.of(loan)).when(loanRepository).findById(loan.getCustomer().getId());
            // When
            final Executable executable = () -> userController.getLoanById(loan.getCustomer().getId());
            // Then
            final ServerException serverException = assertThrows(ServerException.class, executable);
            assertEquals(AppConstants.OmaErrorMessageType.UNAUTHORIZED, serverException.getOmaErrorMessageType());
            assertEquals(HttpStatus.UNAUTHORIZED, serverException.getStatusCode());
            assertEquals(1, serverException.getVariables().length);
            final Optional<String> first = Arrays.stream(serverException.getVariables()).findFirst();
            assertTrue(first.isPresent());
            assertEquals("This user does not have the right permission for this operation", first.get());
        }

        @Test
        void givenId_whenGetLoanById_thenThrowServerException() throws ServerException {
            // Given
            doReturn(Optional.of(loan)).when(loanRepository).findById("id");
            // When
            final LoanDto getLoanById = userController.getLoanById("id");
            // Then
            assertNotNull(getLoanById);
            assertEquals(loan.getAmount(), getLoanById.getAmount());
            assertEquals(loan.getNumberOfInstallment(), getLoanById.getNumberOfInstallment());
            assertEquals(loan.isPaid(), getLoanById.isPaid());
            assertEquals(user.getEmail(), getLoanById.getCustomer().getEmail());
            assertEquals(1, getLoanById.getInstallments().size());
        }
    }

    @Nested
    class Create {
        private final LoanCreateDto loanCreateDto = new LoanCreateDto();

        @BeforeEach
        void setup() {
            loanCreateDto.setAmount(BigDecimal.TEN);
            loanCreateDto.setCustomerId(user.getId());
            loanCreateDto.setInterestRate(BigDecimal.valueOf(0.3));
            loanCreateDto.setNumberOfInstallments(6);
        }

        @Test
        void givenLoanCreateDto_whenCreateWithBindingResultErrors_thenThrowServerException() {
            // Given
            doReturn(true).when(bindingResult).hasErrors();
            final FieldError objectError = new FieldError("name", "message", "default");
            final List<FieldError> getAllErrors = List.of(objectError);
            when(bindingResult.getFieldErrors()).thenReturn(getAllErrors);
            // When
            final Executable executable = () -> userController.create(loanCreateDto, bindingResult);
            // Then
            final ServerException serverException = assertThrows(ServerException.class, executable);
            assertEquals(AppConstants.OmaErrorMessageType.JSON_SCHEMA_VALIDATOR, serverException.getOmaErrorMessageType());
            assertEquals(HttpStatus.BAD_REQUEST, serverException.getStatusCode());
            assertEquals(1, serverException.getVariables().length);
            final Optional<String> first = Arrays.stream(serverException.getVariables()).findFirst();
            assertTrue(first.isPresent());
            assertEquals("message: default", first.get());
        }

        @Test
        void givenId_whenCreateWithNotEnoughCreditLimit_thenThrowServerException() {
            // Given
            user.setCreditLimit(BigDecimal.TEN);
            when(userService.findById(loanCreateDto.getCustomerId())).thenReturn(user);
            // When
            final Executable executable = () -> userController.create(loanCreateDto, bindingResult);
            // Then
            final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, executable);
            assertEquals("Customer does not have enough credit limit for this loan", illegalArgumentException.getMessage());
        }

        @Test
        void givenId_whenCreateWithNotValidInterestRate_thenThrowServerException() {
            // Given
            loanCreateDto.setInterestRate(BigDecimal.TEN);
            when(userService.findById(loanCreateDto.getCustomerId())).thenReturn(user);
            // When
            final Executable executable = () -> userController.create(loanCreateDto, bindingResult);
            // Then
            final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, executable);
            assertEquals("Interest rate must be between 0.1 and 0.5", illegalArgumentException.getMessage());
        }

        @Test
        void givenId_whenCreateWithNotValidNumberOfInstallments_thenThrowServerException() {
            // Given
            loanCreateDto.setNumberOfInstallments(1);
            when(userService.findById(loanCreateDto.getCustomerId())).thenReturn(user);
            // When
            final Executable executable = () -> userController.create(loanCreateDto, bindingResult);
            // Then
            final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, executable);
            assertEquals("Number of installments must be 6, 9, 12, or 24", illegalArgumentException.getMessage());
        }

        @Test
        void givenId_whenCreate_thenAssertResult() throws ServerException {
            // Given
            when(userService.findById(loanCreateDto.getCustomerId())).thenReturn(user);
            when(loanRepository.save(any(Loan.class))).thenReturn(loan);
            // When
            final LoanDto response = userController.create(loanCreateDto, bindingResult);
            // Then
            assertNotNull(response);
            assertEquals(loan.getAmount(), response.getAmount());
        }
    }

    @Nested
    class PayLoan {
        private final PaymentRequest paymentRequest = new PaymentRequest();

        @BeforeEach
        void setup() {
            paymentRequest.setAmount(BigDecimal.TEN);
        }

        @Test
        void givenLoanIdAndPaymentRequest_whenPayLoanWithLoanIsNull_thenThrowServerException() {
            // When
            final Executable executable = () -> userController.payLoan("loanId", paymentRequest);
            // Then
            final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, executable);
            assertEquals("Loan not found", illegalArgumentException.getMessage());
        }

        @Test
        void givenLoanIdAndPaymentRequest_whenPayLoanWithLoanIsNotBelongToUser_thenThrowServerException() {
            // Given
            final User customer = new User();
            customer.setId(UUID.randomUUID().toString());
            loan.setCustomer(customer);
            when(loanRepository.findById("loanId")).thenReturn(Optional.of(loan));
            // When
            final Executable executable = () -> userController.payLoan("loanId", paymentRequest);
            // Then
            final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, executable);
            assertEquals("User is not authorized to pay", illegalArgumentException.getMessage());
        }

        @Test
        void givenLoanIdAndPaymentRequest_whenPayLoanWithLoanIsPaid_thenThrowServerException() {
            // Given
            loan.setPaid(true);
            when(loanRepository.findById("loanId")).thenReturn(Optional.of(loan));
            // When
            final Executable executable = () -> userController.payLoan("loanId", paymentRequest);
            // Then
            final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, executable);
            assertEquals("Loan is already fully paid", illegalArgumentException.getMessage());
        }

        @Test
        void givenLoanIdAndPaymentRequest_whenPayLoan_thenAssertResult() throws ServerException {
            // Given
            when(loanRepository.findById("loanId")).thenReturn(Optional.of(loan));
            final LoanInstallment installment = LoanFactory.createInstallment(loan);
            List<LoanInstallment> installments = List.of(installment);
            when(loanInstallmentRepository.findByLoanIdAndPaidFalseOrderByDueDateAsc("loanId")).thenReturn(installments);
            when(loanInstallmentRepository.saveAll(any(List.class))).thenReturn(installments);
            // When
            final Map<String, String> response = userController.payLoan("loanId", paymentRequest);
            // Then
            assertEquals("1", response.get("installmentsPaid"));
            assertEquals("10", response.get("totalPaid"));
            assertEquals("true", response.get("isLoanPaid"));
        }
    }
}