package com.github.senocak.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.senocak.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder({"user", "token", "refreshToken"})
public class UserWrapperResponse extends BaseDto {
    @JsonProperty("user")
    @Schema(required = true)
    private UserResponse userResponse;

    @Schema(example = "eyJraWQiOiJ...", description = "Jwt Token", required = true, name = "token", type = "String")
    private String token;
}