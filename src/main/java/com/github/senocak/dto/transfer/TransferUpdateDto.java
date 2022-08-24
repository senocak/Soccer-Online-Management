package com.github.senocak.dto.transfer;

import com.github.senocak.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class TransferUpdateDto extends BaseDto {
    @NotNull
    @Min(value = 1_000)
    @Max(value = 100_000_000)
    @Schema(example = "1234", description = "Asked price of the player", required = true, name = "askedPrice", type = "String")
    private int askedPrice;
}