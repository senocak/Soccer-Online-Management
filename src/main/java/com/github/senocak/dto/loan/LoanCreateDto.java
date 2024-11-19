package com.github.senocak.dto.loan;

import com.github.senocak.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Getter
@Setter
public class LoanCreateDto extends BaseDto {
    @Size(min = 4, max = 100)
    @Schema(example = "2de8bc63-bee4-4371-a1cd-35f80c04538a", description = "Customer Id", required = true, name = "customerId", type = "String")
    private String customerId;

    @NotNull
    @Schema(example = "234.4", description = "Amount", required = true, name = "amount", type = "BigDecimal")
    private BigDecimal amount;

    @Schema(example = "0.1", description = "Iinterest Rate", required = true, name = "interestRate", type = "BigDecimal")
    private BigDecimal interestRate;

    @Schema(example = "12", description = "Number Of Installments", required = true, name = "numberOfInstallments", type = "Integer")
    private Integer numberOfInstallments;
}
