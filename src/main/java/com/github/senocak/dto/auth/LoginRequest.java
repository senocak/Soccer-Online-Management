package com.github.senocak.dto.auth;

import com.github.senocak.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class LoginRequest extends BaseDto {
    @NotBlank
    @Size(min = 3, max = 30)
    @Schema(example = "asenocak", description = "Username of the user", required = true, name = "username", type = "String")
    private String username;

    @NotBlank
    @Size(min = 6, max = 20)
    @Schema(description = "Password of the user", name = "password", type = "String", example = "password", required = true)
    private String password;
}