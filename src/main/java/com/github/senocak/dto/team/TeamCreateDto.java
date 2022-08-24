package com.github.senocak.dto.team;

import com.github.senocak.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class TeamCreateDto extends BaseDto {
    @NotNull
    @Size(min = 4, max = 100)
    @Schema(example = "Beşiktaş", description = "Name of the team", required = true, name = "name", type = "String")
    private String name;

    @NotNull
    @Size(min = 3)
    @Schema(example = "Italy", description = "Country of the team", required = true, name = "country", type = "String")
    private String country;

    @Schema(example = "1200000", description = "Available Cash of the team", required = true, name = "availableCash", type = "Integer")
    private Integer availableCash;

    @Size(min = 5, max = 35)
    @Schema(example = "asenocak", description = "Id of the user", required = true, name = "userId", type = "String")
    private String username;
}
