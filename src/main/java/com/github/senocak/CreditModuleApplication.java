package com.github.senocak;

import com.github.senocak.model.Loan;
import com.github.senocak.model.LoanInstallment;
import com.github.senocak.model.Role;
import com.github.senocak.model.User;
import com.github.senocak.repository.LoanInstallmentRepository;
import com.github.senocak.repository.LoanRepository;
import com.github.senocak.repository.RoleRepository;
import com.github.senocak.repository.UserRepository;
import com.github.senocak.util.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class CreditModuleApplication {
	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final LoanRepository loanRepository;
	private final LoanInstallmentRepository loanInstallmentRepository;

	public static void main(String[] args) {
		SpringApplication.run(CreditModuleApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void applicationReadyEvent(final ApplicationReadyEvent event) {
		log.info("ApplicationReadyEvent: {} seconds", event.getTimeTaken().toSeconds());
		roleRepository.deleteAll();
		final Role roleAdmin = roleRepository.save(Role.builder().name(AppConstants.RoleName.ROLE_ADMIN).build());
		final Role roleUser = roleRepository.save(Role.builder().name(AppConstants.RoleName.ROLE_USER).build());

		userRepository.deleteAll();
		final User user1 = userRepository.save(User.builder()
				.name("Lorem1")
				.surname("Ipsum1")
				.email("admin@lorem.com")
				.password(passwordEncoder.encode("lorem"))
				.roles(Set.of(roleAdmin))
				.creditLimit(BigDecimal.valueOf(1_000_000))
				.usedCreditLimit(BigDecimal.valueOf(100_000))
				.build());
		final User user2 = userRepository.save(User.builder()
				.name("Lorem2")
				.surname("Ipsum2")
				.email("user@lorem.com")
				.password(passwordEncoder.encode("lorem"))
				.roles(Set.of(roleUser))
				.creditLimit(BigDecimal.valueOf(2_000_000))
				.usedCreditLimit(BigDecimal.valueOf(200_000))
				.build());
		final User user3 = userRepository.save(User.builder()
				.name("Lorem3")
				.surname("Ipsum3")
				.email("mix@lorem.com")
				.password(passwordEncoder.encode("lorem"))
				.roles(Set.of(roleAdmin, roleUser))
				.creditLimit(BigDecimal.valueOf(3_000_000))
				.usedCreditLimit(BigDecimal.valueOf(300_000))
				.build());

		loanRepository.deleteAll();
		final Loan loan1 = loanRepository.save(Loan.builder()
				.amount(BigDecimal.valueOf(1_000))
				.numberOfInstallment(5)
				.createDate(LocalDateTime.now())
				.isPaid(false)
				.customer(user1)
				.build());
		final Loan loan2 = loanRepository.save(Loan.builder()
				.amount(BigDecimal.valueOf(2_000))
				.numberOfInstallment(2)
				.createDate(LocalDateTime.now())
				.isPaid(false)
				.customer(user1)
				.build());
		final Loan loan3 = loanRepository.save(Loan.builder()
				.amount(BigDecimal.valueOf(100))
				.numberOfInstallment(1)
				.createDate(LocalDateTime.now())
				.isPaid(true)
				.customer(user2)
				.build());

		loanInstallmentRepository.deleteAll();
		final LoanInstallment loanInstallment11 = loanInstallmentRepository.save(LoanInstallment.builder()
				.loan(loan1)
				.amount(BigDecimal.valueOf(200))
				.paidAmount(BigDecimal.valueOf(200))
				.dueDate(LocalDate.of(2025, 1, 1))
				.paymentDate(LocalDate.of(2024, 12, 12))
				.isPaid(true)
				.build());
		final LoanInstallment loanInstallment12 = loanInstallmentRepository.save(LoanInstallment.builder()
				.loan(loan1)
				.amount(BigDecimal.valueOf(200))
				.paidAmount(BigDecimal.ZERO)
				.dueDate(LocalDate.of(2025, 2, 1))
				//.paymentDate(LocalDate.of(2024, 12, 12))
				.isPaid(false)
				.build());
		final LoanInstallment loanInstallment13 = loanInstallmentRepository.save(LoanInstallment.builder()
				.loan(loan1)
				.amount(BigDecimal.valueOf(200))
				.paidAmount(BigDecimal.ZERO)
				.dueDate(LocalDate.of(2025, 3, 1))
				//.paymentDate(LocalDate.of(2024, 12, 12))
				.isPaid(false)
				.build());
		final LoanInstallment loanInstallment14 = loanInstallmentRepository.save(LoanInstallment.builder()
				.loan(loan1)
				.amount(BigDecimal.valueOf(200))
				.paidAmount(BigDecimal.ZERO)
				.dueDate(LocalDate.of(2025, 4, 1))
				//.paymentDate(LocalDate.of(2024, 12, 12))
				.isPaid(false)
				.build());
		final LoanInstallment loanInstallment15 = loanInstallmentRepository.save(LoanInstallment.builder()
				.loan(loan1)
				.amount(BigDecimal.valueOf(200))
				.paidAmount(BigDecimal.ZERO)
				.dueDate(LocalDate.of(2025, 5, 1))
				//.paymentDate(LocalDate.of(2024, 12, 12))
				.isPaid(false)
				.build());
		final LoanInstallment loanInstallment21 = loanInstallmentRepository.save(LoanInstallment.builder()
				.loan(loan2)
				.amount(BigDecimal.valueOf(1_000))
				.paidAmount(BigDecimal.valueOf(1_000))
				.dueDate(LocalDate.of(2025, 1, 1))
				.paymentDate(LocalDate.of(2024, 12, 2))
				.isPaid(true)
				.build());
		final LoanInstallment loanInstallment22 = loanInstallmentRepository.save(LoanInstallment.builder()
				.loan(loan2)
				.amount(BigDecimal.valueOf(1_000))
				.paidAmount(BigDecimal.ZERO)
				.dueDate(LocalDate.of(2025, 2, 1))
				//.paymentDate(LocalDate.of(2024, 12, 12))
				.isPaid(false)
				.build());
		final LoanInstallment loanInstallment31 = loanInstallmentRepository.save(LoanInstallment.builder()
				.loan(loan3)
				.amount(BigDecimal.valueOf(100))
				.paidAmount(BigDecimal.ZERO)
				.dueDate(LocalDate.of(2025, 1, 1))
				.paymentDate(LocalDate.of(2024, 12, 22))
				.isPaid(true)
				.build());
		log.info("Db population completed");
	}
}
