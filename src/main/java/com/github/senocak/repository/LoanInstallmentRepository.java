package com.github.senocak.repository;

import com.github.senocak.model.LoanInstallment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

@Repository
public interface LoanInstallmentRepository extends JpaRepository<LoanInstallment, String> {
    @Query("select l from LoanInstallment l where l.loan.id = ?1 and l.isPaid = false order by l.dueDate")
    List<LoanInstallment> findByLoanIdAndPaidFalseOrderByDueDateAsc(String loanId);
}
