package com.github.senocak.dto.loan;

import com.github.senocak.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class LoanInstallmentDto extends BaseDto {
    @Schema(example = "2de8bc63-bee4-4371-a1cd-35f80c04538a", description = "Id", required = true, name = "id", type = "String")
    private String id;

    @Schema(example = "432", description = "Amount", required = true, name = "amount", type = "BigDecimal")
    private BigDecimal amount;

    @Schema(example = "43", description = "Paid Amount", required = true, name = "paidAmount", type = "BigDecimal")
    private BigDecimal paidAmount;

    @Schema(example = "12-12-2024", description = "Due date", required = true, name = "dueDate", type = "LocalDate")
    private LocalDate dueDate;

    @Schema(example = "12-12-2024", description = "Payment date", required = true, name = "paymentDate", type = "LocalDate")
    private LocalDate paymentDate;

    @Schema(example = "false", description = "is Paid?", required = true, name = "isPaid", type = "boolean")
    private boolean isPaid = false;

    @Schema(description = "Loan", required = true, name = "loan")
    private LoanDto loan;

}