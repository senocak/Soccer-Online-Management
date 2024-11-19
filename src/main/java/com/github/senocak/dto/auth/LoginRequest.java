package com.github.senocak.dto.auth;

import com.github.senocak.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest extends BaseDto {
    @NotBlank
    @Size(min = 3, max = 30)
    @Schema(example = "anil@senocak.com", description = "Email of the user", required = true, name = "email", type = "String")
    private String email;

    @NotBlank
    @Size(min = 5, max = 20)
    @Schema(description = "Password of the user", name = "password", type = "String", example = "password", required = true)
    private String password;
}