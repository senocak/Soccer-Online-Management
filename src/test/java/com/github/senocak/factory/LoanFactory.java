package com.github.senocak.factory;

import com.github.senocak.model.Loan;
import com.github.senocak.model.LoanInstallment;
import com.github.senocak.model.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class LoanFactory {
    private LoanFactory(){}

    public static Loan createLoan() {
        final Loan build = Loan.builder()
                .amount(BigDecimal.TEN)
                .numberOfInstallment(10)
                .createDate(LocalDateTime.now())
                .isPaid(false)
                .installments(List.of(createInstallment()))
                .build();
        build.setId(UUID.randomUUID().toString());
        return build;
    }

    public static Loan createLoan(final User user) {
        final Loan createLoan = createLoan();
        createLoan.setCustomer(user);
        return createLoan;
    }

    public static LoanInstallment createInstallment(final Loan loan) {
        final LoanInstallment createInstallment = createInstallment();
        createInstallment.setLoan(loan);
        return createInstallment;
    }

    public static LoanInstallment createInstallment() {
        return LoanInstallment.builder()
                .amount(BigDecimal.TEN)
                .paidAmount(BigDecimal.TEN)
                .dueDate(LocalDate.now())
                .paymentDate(LocalDate.now())
                .isPaid(true)
                .build();
    }
}
