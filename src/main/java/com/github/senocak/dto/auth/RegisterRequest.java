package com.github.senocak.dto.auth;

import com.github.senocak.validator.ValidEmail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class RegisterRequest {
    @NotNull
    @Size(min = 4, max = 40)
    @Schema(example = "Lorem Ipsum", description = "Name of the user", required = true, name = "name", type = "String")
    private String name;

    @NotNull
    @Size(min = 3, max = 15)
    @Schema(example = "asenocak", description = "Username of the user", required = true, name = "username", type = "String")
    private String username;

    @NotNull
    @ValidEmail
    @Schema(example = "lorem@ipsum.com", description = "Email of the user", required = true, name = "email", type = "String")
    private String email;

    @NotNull
    @Size(min = 6, max = 20)
    @Schema(example = "asenocak123", description = "Password of the user", required = true, name = "password", type = "String")
    private String password;
}