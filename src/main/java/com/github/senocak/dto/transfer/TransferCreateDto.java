package com.github.senocak.dto.transfer;

import com.github.senocak.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Setter
public class TransferCreateDto extends BaseDto {
    @NotNull
    @Schema(example = "anil", description = "Id of the player", required = true, name = "playerId", type = "String")
    private UUID playerId;

    @NotNull
    @Min(value = 1_000)
    @Max(value = 100_000_000)
    @Schema(example = "1234", description = "Asked price of the player", required = true, name = "askedPrice", type = "String")
    private int askedPrice;
}