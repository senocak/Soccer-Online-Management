package com.github.senocak.repository;

import com.github.senocak.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.PagingAndSortingRepository;

@Repository
public interface LoanRepository extends PagingAndSortingRepository<Loan, String>, JpaSpecificationExecutor<Loan>, JpaRepository<Loan, String> {
}
