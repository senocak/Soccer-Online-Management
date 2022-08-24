package com.github.senocak.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.senocak.dto.BaseDto;
import com.github.senocak.dto.auth.RoleResponse;
import com.github.senocak.dto.team.TeamDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
@JsonPropertyOrder({"name", "username", "email", "roles", "teamDto"})
public class UserResponse extends BaseDto {
    @JsonProperty("name")
    @Schema(example = "Lorem Ipsum", description = "Name of the user", required = true, name = "name", type = "String")
    private String name;

    @Schema(example = "lorem@ipsum.com", description = "Email of the user", required = true, name = "email", type = "String")
    private String email;

    @Schema(example = "asenocak", description = "Username of the user", required = true, name = "username", type = "String")
    private String username;

    @ArraySchema(schema = @Schema(example = "ROLE_USER", description = "Roles of the user", required = true, name = "roles"))
    private Set<RoleResponse> roles;

    @JsonProperty("team")
    private TeamDto teamDto;
}