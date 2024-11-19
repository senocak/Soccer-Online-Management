package com.github.senocak.service;

import com.github.senocak.dto.auth.RoleResponse;
import com.github.senocak.dto.loan.LoanDto;
import com.github.senocak.dto.loan.LoanInstallmentDto;
import com.github.senocak.dto.user.UserResponse;
import com.github.senocak.model.Loan;
import com.github.senocak.model.LoanInstallment;
import com.github.senocak.model.Role;
import com.github.senocak.model.User;
import java.util.stream.Collectors;

public class DtoConverter {
    private DtoConverter(){}

    /**
     * @param user -- User object to convert to dto object
     * @return -- UserResponse object
     */
    public static UserResponse convertEntityToDto(final User user, final Boolean showLoans, final Boolean showInstallment) {
        final UserResponse userResponse = new UserResponse();
        userResponse.setName(user.getName());
        userResponse.setEmail(user.getEmail());
        userResponse.setSurname(user.getSurname());
        userResponse.setRoles(
            user.getRoles().stream()
                    .map(DtoConverter::convertEntityToDto)
                    .collect(Collectors.toSet())
        );
        userResponse.setCreditLimit(user.getCreditLimit());
        userResponse.setUsedCreditLimit(user.getUsedCreditLimit());
        if (showLoans)
            userResponse.setLoans(user.getLoans().stream().map(l -> DtoConverter.convertEntityToDto(l, false, showInstallment)).toList());
        return userResponse;
    }

    /**
     * @param role -- role object to convert to dto object
     * @return -- RoleResponse object
     */
    public static RoleResponse convertEntityToDto(final Role role){
        final RoleResponse roleResponse = new RoleResponse();
        roleResponse.setName(role.getName());
        return roleResponse;
    }

    /**
     * @param loan -- Date object to convert to long timestamp
     * @return -- converted timestamp object that is long type
     */
    public static LoanDto convertEntityToDto(final Loan loan, final Boolean showUser, final Boolean showInstallment) {
        final LoanDto loanDto = new LoanDto();
        loanDto.setId(loan.getId());
        loanDto.setAmount(loan.getAmount());
        loanDto.setNumberOfInstallment(loan.getNumberOfInstallment());
        loanDto.setCreateDate(loan.getCreateDate());
        loanDto.setPaid(loan.isPaid());
        if (showUser)
            loanDto.setCustomer(DtoConverter.convertEntityToDto(loan.getCustomer(), false, showInstallment));
        if (showInstallment && loan.getInstallments() != null)
            loanDto.setInstallments(loan.getInstallments().stream()
                    .map(i -> DtoConverter.convertEntityToDto(i, false)).toList());
        return loanDto;
    }

    /**
     * @param loanInstallment -- Date object to convert to long timestamp
     * @return -- converted timestamp object that is long type
     */
    public static LoanInstallmentDto convertEntityToDto(final LoanInstallment loanInstallment, final Boolean showLoan) {
        final LoanInstallmentDto loanInstallmentDto = new LoanInstallmentDto();
        loanInstallmentDto.setId(loanInstallment.getId());
        loanInstallmentDto.setAmount(loanInstallment.getAmount());
        loanInstallmentDto.setPaidAmount(loanInstallment.getPaidAmount());
        loanInstallmentDto.setDueDate(loanInstallment.getDueDate());
        loanInstallmentDto.setPaymentDate(loanInstallment.getPaymentDate());
        loanInstallmentDto.setPaid(loanInstallment.isPaid());
        if (showLoan)
            loanInstallmentDto.setLoan(convertEntityToDto(loanInstallment.getLoan(), true, false));
        return loanInstallmentDto;
    }
}
