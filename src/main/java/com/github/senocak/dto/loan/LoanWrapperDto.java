package com.github.senocak.dto.loan;

import com.github.senocak.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Builder
public class LoanWrapperDto extends BaseDto {
    @Schema(description = "Loan wrapper", required = true, name = "loans", type = "List<LoanDto>")
    private List<LoanDto> loans;

    @Schema(example = "1", description = "Total page", required = true, name = "total", type = "Long")
    private Long total;

    @Schema(example = "0", description = "Next page", required = true, name = "next", type = "Long")
    private Long next;
}