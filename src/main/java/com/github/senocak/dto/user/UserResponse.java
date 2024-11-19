package com.github.senocak.dto.user;

import com.github.senocak.dto.BaseDto;
import com.github.senocak.dto.auth.RoleResponse;
import com.github.senocak.dto.loan.LoanDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class UserResponse extends BaseDto {
    @Schema(example = "Lorem", description = "Name of the user", required = true, name = "name", type = "String")
    private String name;

    @Schema(example = "lorem@ipsum.com", description = "Email of the user", required = true, name = "email", type = "String")
    private String email;

    @Schema(example = "Ipsum", description = "Surname of the user", required = true, name = "surname", type = "String")
    private String surname;

    @ArraySchema(schema = @Schema(implementation = RoleResponse.class))
    private Set<RoleResponse> roles;

    @Schema(example = "321", description = "Name of the user", required = true, name = "creditLimit", type = "BigDecimal")
    private BigDecimal creditLimit;

    @Schema(example = "32", description = "Name of the user", required = true, name = "usedCreditLimit", type = "BigDecimal")
    private BigDecimal usedCreditLimit;

    @ArraySchema(schema = @Schema(implementation = LoanDto.class))
    private List<LoanDto> loans;
}