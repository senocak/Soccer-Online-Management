package com.github.senocak.dto.loan;

import com.github.senocak.dto.BaseDto;
import com.github.senocak.dto.user.UserResponse;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class LoanDto extends BaseDto {
    @Schema(example = "2de8bc63-bee4-4371-a1cd-35f80c04538a", description = "Id", required = true, name = "id", type = "String")
    private String id;

    @Schema(example = "432", description = "Amount", required = true, name = "amount", type = "BigDecimal")
    private BigDecimal amount;

    @Schema(example = "2", description = "Number Of Installment", required = true, name = "numberOfInstallment", type = "int")
    private int numberOfInstallment;

    @Schema(example = "12-12-2024", description = "createDate", required = true, name = "createDate", type = "LocalDateTime")
    private LocalDateTime createDate;

    @Schema(example = "false", description = "is Paid?", required = true, name = "isPaid", type = "boolean")
    private boolean isPaid;

    @Schema(description = "Customer", required = true, name = "customer", type = "UserResponse")
    private UserResponse customer;

    @ArraySchema(schema = @Schema(description = "Installment", required = true, name = "installments", type = "LoanInstallmentDto"))
    private List<LoanInstallmentDto> installments;
}
