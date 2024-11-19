package com.github.senocak.dto.loan;

import com.github.senocak.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Getter
@Setter
public class PaymentRequest extends BaseDto {
    @NotNull
    @Positive
    @Schema(example = "432", description = "Amount", required = true, name = "amount", type = "BigDecimal")
    private BigDecimal amount;
}