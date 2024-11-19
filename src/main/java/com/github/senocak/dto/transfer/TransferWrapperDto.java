package com.github.senocak.dto.transfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.senocak.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Builder
@JsonPropertyOrder({"transfer", "transfers", "next", "total"})
public class TransferWrapperDto extends BaseDto {
    @JsonProperty("transfer")
    @Schema(description = "Transfer wrapper", name = "transferDto", type = "TransferDto")
    private TransferDto transferDto;

    @JsonProperty("transfers")
    @ArraySchema(schema = @Schema(description = "Transfer wrapper", name = "transferDto", type = "TransferDto"))
    private List<TransferDto> transfersDto;

    @Schema(example = "1", description = "Total page", required = true, name = "total", type = "Long")
    private Long total;

    @Schema(example = "0", description = "Next page", required = true, name = "next", type = "Long")
    private Long next;
}
