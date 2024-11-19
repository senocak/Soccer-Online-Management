package com.github.senocak.service;

import com.github.senocak.dto.auth.RoleResponse;
import com.github.senocak.dto.loan.LoanDto;
import com.github.senocak.dto.loan.LoanInstallmentDto;
import com.github.senocak.dto.user.UserResponse;
import com.github.senocak.factory.LoanFactory;
import com.github.senocak.factory.UserFactory;
import com.github.senocak.model.Loan;
import com.github.senocak.model.LoanInstallment;
import com.github.senocak.model.Role;
import com.github.senocak.model.User;
import com.github.senocak.util.AppConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for DtoConverter")
public class DtoConverterTest {

    @Test
    public void givenUser_whenConvertEntityToDto_thenAssertResult(){
        // Given
        final User user = UserFactory.createUser();
        // When
        final UserResponse convertEntityToDto = DtoConverter.convertEntityToDto(user,
                false, false);
        // Then
        assertEquals(user.getName(), convertEntityToDto.getName());
        assertEquals(user.getEmail(), convertEntityToDto.getEmail());
        assertEquals(user.getSurname(), convertEntityToDto.getSurname());
    }

    @Test
    public void givenRole_whenConvertEntityToDto_thenAssertResult(){
        // Given
        final Role role = Role.builder().name(AppConstants.RoleName.ROLE_USER).build();
        // When
        final RoleResponse convertEntityToDto = DtoConverter.convertEntityToDto(role);
        // Then
        assertEquals(role.getName(), convertEntityToDto.getName());
    }

    @Test
    public void givenLoan_whenConvertEntityToDto_thenAssertResult(){
        // Given
        final Loan loan = LoanFactory.createLoan(UserFactory.createUser());
        // When
        final LoanDto convertEntityToDto = DtoConverter.convertEntityToDto(loan, true, true);
        // Then
        assertEquals(convertEntityToDto.getAmount(), convertEntityToDto.getAmount());
        assertEquals(convertEntityToDto.getNumberOfInstallment(), convertEntityToDto.getNumberOfInstallment());
        assertEquals(convertEntityToDto.getCreateDate(), convertEntityToDto.getCreateDate());
        assertEquals(convertEntityToDto.isPaid(), convertEntityToDto.isPaid());
    }
    @Test
    public void givenLoanInstallment_whenConvertEntityToDto_thenAssertResult(){
        // Given
        final LoanInstallment transfer = LoanFactory.createInstallment(LoanFactory.createLoan());
        // When
        final LoanInstallmentDto convertEntityToDto = DtoConverter.convertEntityToDto(transfer, false);
        // Then
        assertEquals(transfer.getId(), convertEntityToDto.getId());
        assertEquals(transfer.getAmount(), convertEntityToDto.getAmount());
        assertEquals(transfer.getPaidAmount(), convertEntityToDto.getPaidAmount());
        assertEquals(transfer.getDueDate(), convertEntityToDto.getDueDate());
        assertEquals(transfer.getPaymentDate(), convertEntityToDto.getPaymentDate());
        assertEquals(transfer.isPaid(), convertEntityToDto.isPaid());
    }
}
