package com.github.senocak.dto.auth;

import com.github.senocak.dto.BaseDto;
import com.github.senocak.util.AppConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleResponse extends BaseDto {
    @Schema(example = "ROLE_USER", description = "Name of the role", required = true, name = "name")
    private AppConstants.RoleName name;
}