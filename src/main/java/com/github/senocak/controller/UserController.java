package com.github.senocak.controller;

import com.github.senocak.config.SwaggerConfig;
import com.github.senocak.dto.ExceptionDto;
import com.github.senocak.dto.loan.LoanDto;
import com.github.senocak.dto.loan.LoanCreateDto;
import com.github.senocak.dto.loan.LoanWrapperDto;
import com.github.senocak.dto.loan.PaymentRequest;
import com.github.senocak.dto.user.UserWrapperResponse;
import com.github.senocak.dto.user.UserUpdateDto;
import com.github.senocak.model.Loan;
import com.github.senocak.model.LoanInstallment;
import com.github.senocak.repository.LoanInstallmentRepository;
import com.github.senocak.repository.LoanRepository;
import com.github.senocak.service.DtoConverter;
import com.github.senocak.service.UserService;
import com.github.senocak.util.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import com.github.senocak.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.github.senocak.exception.ServerException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import static com.github.senocak.util.AppConstants.ADMIN;
import static com.github.senocak.util.AppConstants.USER;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(UserController.URL)
@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
@Tag(name = "User", description = "User Controller")
public class UserController extends BaseController {
    public static final String URL = "/api/v1/user";
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final LoanRepository loanRepository;
    private final LoanInstallmentRepository loanInstallmentRepository;

    @GetMapping("/me")
    @Operation(summary = "Get me", tags = {"User"}, responses = {
        @ApiResponse(responseCode = "200", description = "successful operation",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserWrapperResponse.class))),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionDto.class)))
    }, security = @SecurityRequirement(name = SwaggerConfig.securitySchemeName, scopes = {ADMIN, USER}))
    public UserWrapperResponse getMe(
        @RequestParam(value = "showInstallment", required = false, defaultValue = "false") final Boolean showInstallment
    ) throws ServerException {
        return UserWrapperResponse.builder()
                .userResponse(DtoConverter.convertEntityToDto(userService.loggedInUser(), true, showInstallment))
                .build();
    }

    @PatchMapping("/me")
    @Operation(summary = "Update user by username", tags = {"User"}, responses = {
        @ApiResponse(responseCode = "200", description = "successful operation",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = HashMap.class))),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionDto.class)))
    }, security = @SecurityRequirement(name = SwaggerConfig.securitySchemeName, scopes = {ADMIN, USER}))
    public Map<String, String> patchMe(
        @Parameter(description = "Request body to update", required = true) @RequestBody @Validated UserUpdateDto userDto,
        BindingResult errors
    ) throws ServerException {
        hasErrors(errors);
        User user = userService.loggedInUser();
        final String name = userDto.getName();
        if (Objects.nonNull(name) && !name.isEmpty())
            user.setName(name);
        final String surname = userDto.getSurname();
        if (Objects.nonNull(surname) && !surname.isEmpty())
            user.setSurname(surname);
        final String password = userDto.getPassword();
        final String password_confirmation = userDto.getPassword_confirmation();
        if (Objects.nonNull(password) && !password.isEmpty() &&
                Objects.nonNull(password_confirmation) && !password_confirmation.isEmpty()){
            user.setPassword(passwordEncoder.encode(password));
        }
        user = userService.save(user);
        return Map.of("message", "User updated.");
    }

    @GetMapping("/loans")
    @Operation(summary = "Get All Loans For User", tags = {"Loan"}, responses = {
        @ApiResponse(responseCode = "200", description = "successful operation",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LoanWrapperDto.class))),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionDto.class)))
    })
    public LoanWrapperDto getAllLoans(
        @Parameter(description = "User Id") @RequestParam(value = "userId", required = false) final String userId,
        @Parameter(description = "installments param") @RequestParam(value = "installments", required = false) final Boolean installments,
        @Parameter(description = "isPaid param") @RequestParam(value = "isPaid", required = false) final Boolean isPaid,
        @Parameter(description = "amountGreaterThan param") @RequestParam(value = "amountGreaterThan", required = false) final BigDecimal amountGreaterThan,
        @Parameter(description = "numberOfInstallment param") @RequestParam(value = "numberOfInstallment", required = false) final Integer numberOfInstallment,
        @Parameter(description = "Number of resources that is requested.") @RequestParam(value = "next", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) @Min(0) @Max(99) final int nextPage,
        @Parameter(description = "Pointer for the next page to retrieve.") @RequestParam(value = "max", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) @Min(0) @Max(99) final int maxNumber
    ) throws ServerException {
        User user = userService.loggedInUser();
        if (user.getRoles().stream().anyMatch(role -> role.getName().equals(AppConstants.RoleName.ROLE_ADMIN)) && userId != null)
            user = userService.findById(userId);
        Specification<Loan> specification = Specification.where(null);
        final User finalUser = user;
        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("customer").get("id"), finalUser.getId()));
        if (isPaid != null)
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("isPaid"), isPaid));
        if (amountGreaterThan != null)
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThan(root.get("amount"), amountGreaterThan));
        if (numberOfInstallment != null)
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("numberOfInstallment"), numberOfInstallment));
        final Page<Loan> all = loanRepository.findAll(specification, PageRequest.of(nextPage, maxNumber));
        final List<LoanDto> loanDtos = all.stream().map(l -> DtoConverter.convertEntityToDto(l, false, installments)).collect(Collectors.toList());
        return LoanWrapperDto.builder()
                .loans(loanDtos)
                .next((long) (all.hasNext() ? nextPage + 1 : 0))
                .total(all.getTotalElements())
                .build();
    }

    @GetMapping(value = "/loans/{id}")
    @Operation(summary = "Get Single Loan", tags = {"Loan"}, responses = {
        @ApiResponse(responseCode = "200", description = "successful operation",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LoanDto.class))),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionDto.class)))
    })
    public LoanDto getLoanById(@Parameter(description = "Loan ID", required = true) @PathVariable String id) throws ServerException {
        final Optional<Loan> byId = loanRepository.findById(id);
        if (byId.isEmpty())
            throw new ServerException(AppConstants.OmaErrorMessageType.NOT_FOUND,
                    new String[]{"This Loan not found"}, HttpStatus.NOT_FOUND);
        final Loan loan = byId.get();
        final User getUserFromContext = userService.loggedInUser();
        if (getUserFromContext.getRoles().stream().noneMatch(role -> role.getName().equals(AppConstants.RoleName.ROLE_ADMIN)) &&
                id.equals(loan.getCustomer().getId())) {
            throw new ServerException(AppConstants.OmaErrorMessageType.UNAUTHORIZED,
                    new String[]{"This user does not have the right permission for this operation"},
                    HttpStatus.UNAUTHORIZED);
        }
        return DtoConverter.convertEntityToDto(loan, true, true);
    }

    @PostMapping("/loans")
    @Operation(summary = "Create Loan", tags = {"Loan"}, responses = {
        @ApiResponse(responseCode = "201", description = "successful operation",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LoanDto.class))),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionDto.class)))
    }, security = @SecurityRequirement(name = SwaggerConfig.securitySchemeName, scopes = {ADMIN, USER}))
    @ResponseStatus(HttpStatus.CREATED)
    public LoanDto create(
        @Parameter(description = "Request body to create", required = true) @RequestBody @Validated LoanCreateDto loanCreateDto,
        BindingResult errors
    ) throws ServerException {
        hasErrors(errors);
        final String userId = loanCreateDto.getCustomerId();
        User user = userService.loggedInUser();
        if (user.getRoles().stream().anyMatch(role -> role.getName().equals(AppConstants.RoleName.ROLE_ADMIN)) && userId != null)
            user = userService.findById(userId);
        final BigDecimal totalLoanAmount = loanCreateDto.getAmount().multiply(BigDecimal.ONE.add(loanCreateDto.getInterestRate()));
        if (user.getCreditLimit().subtract(user.getUsedCreditLimit()).compareTo(totalLoanAmount) < 0)
            throw new IllegalArgumentException("Customer does not have enough credit limit for this loan");
        if (loanCreateDto.getInterestRate().compareTo(BigDecimal.valueOf(0.1)) < 0 ||
                loanCreateDto.getInterestRate().compareTo(BigDecimal.valueOf(0.5)) > 0)
            throw new IllegalArgumentException("Interest rate must be between 0.1 and 0.5");
        if (!List.of(6, 9, 12, 24).contains(loanCreateDto.getNumberOfInstallments()))
            throw new IllegalArgumentException("Number of installments must be 6, 9, 12, or 24");
        Loan loan = Loan.builder()
                .amount(totalLoanAmount)
                .numberOfInstallment(loanCreateDto.getNumberOfInstallments())
                .createDate(LocalDateTime.now())
                .isPaid(false)
                .customer(user)
                .build();
        List<LoanInstallment> installments = new ArrayList<>();
        for (int i = 0; i < loanCreateDto.getNumberOfInstallments(); i++) {
            final LoanInstallment installment = LoanInstallment.builder()
                    .loan(loan)
                    .amount(totalLoanAmount.divide(BigDecimal.valueOf(loanCreateDto.getNumberOfInstallments()), 2, RoundingMode.CEILING))
                    .dueDate(LocalDate.now().withDayOfMonth(1).plusMonths(1).plusMonths(i))
                    .paidAmount(BigDecimal.ZERO)
                    .isPaid(false)
                    .build();
            installments.add(installment);
        }
        loan.setInstallments(installments);
        loan = loanRepository.save(loan);
        installments = loanInstallmentRepository.saveAll(installments);
        user.setUsedCreditLimit(user.getUsedCreditLimit().add(totalLoanAmount));
        user = userService.save(user);
        return DtoConverter.convertEntityToDto(loan, true, true);
    }

    @PostMapping("/loans/{loanId}/pay")
    @Operation(summary = "Pay Loan", tags = {"Loan"}, responses = {
        @ApiResponse(responseCode = "200", description = "successful operation",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionDto.class)))
    }, security = @SecurityRequirement(name = SwaggerConfig.securitySchemeName, scopes = {ADMIN, USER}))
    public Map<String, String> payLoan(
        @Parameter(description = "Loan Id", required = true) @PathVariable String loanId,
        @Parameter(description = "Request body to pay", required = true)@RequestBody @Validated PaymentRequest paymentRequest
    ) throws ServerException {
        final User loggedInUser = userService.loggedInUser();
        final Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));
        if (!loan.getCustomer().getId().equals(loggedInUser.getId()))
            throw new IllegalArgumentException("User is not authorized to pay");
        if (loan.isPaid())
            throw new IllegalArgumentException("Loan is already fully paid");
        List<LoanInstallment> installments = loanInstallmentRepository.findByLoanIdAndPaidFalseOrderByDueDateAsc(loanId);
        final LocalDate maxPayableDate = LocalDate.now().plusMonths(3).withDayOfMonth(1);
        int installmentsPaid = 0;
        BigDecimal totalPaid = BigDecimal.ZERO;
        for (final LoanInstallment installment: installments) {
            if (installment.getDueDate().isAfter(maxPayableDate)) {
                log.warn("Skip installments with due dates beyond the allowable period");
                continue;
            }
            final BigDecimal remainingAmount = installment.getAmount().subtract(installment.getPaidAmount());
            if (paymentRequest.getAmount().compareTo(remainingAmount) >= 0) {
                installment.setPaidAmount(installment.getAmount());
                installment.setPaid(true);
                installment.setPaymentDate(LocalDate.now());
                paymentRequest.setAmount(paymentRequest.getAmount().subtract(remainingAmount));
                totalPaid = totalPaid.add(installment.getAmount());
                installmentsPaid++;
            } else {
                log.warn("Not enough amount to pay this installment");
                break;
            }
        }
        log.info("Total {}, installments paid", installmentsPaid);
        installments = loanInstallmentRepository.saveAll(installments);
        boolean isLoanPaid = installments.stream().allMatch(LoanInstallment::isPaid);
        if (isLoanPaid) {
            loan.setPaid(true);
            loanRepository.save(loan);
        }
        loggedInUser.setUsedCreditLimit(loggedInUser.getUsedCreditLimit().subtract(totalPaid));
        userService.save(loggedInUser);
        return Map.of("installmentsPaid", Integer.toString(installmentsPaid),
                "totalPaid", totalPaid.toString(),
                "isLoanPaid", Boolean.toString(isLoanPaid));
    }
}
