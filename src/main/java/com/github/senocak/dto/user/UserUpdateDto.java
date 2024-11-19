package com.github.senocak.dto.user;

import com.github.senocak.dto.BaseDto;
import com.github.senocak.validator.PasswordMatches;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.Size;

@Getter
@Setter
@PasswordMatches
public class UserUpdateDto extends BaseDto {
    @Size(min = 4, max = 40)
    @Schema(example = "Anil", description = "Name", required = true, name = "name", type = "String")
    private String name;

    @Size(min = 4, max = 40)
    @Schema(example = "Senocak", description = "Surname", required = true, name = "surname", type = "String")
    private String surname;

    @Size(min = 5, max = 20)
    @Schema(example = "Anil123", description = "Password", required = true, name = "password", type = "String")
    private String password;

    @Size(min = 5, max = 20)
    @Schema(example = "Anil123", description = "Password confirmation", required = true, name = "password", type = "String")
    private String password_confirmation;
}
