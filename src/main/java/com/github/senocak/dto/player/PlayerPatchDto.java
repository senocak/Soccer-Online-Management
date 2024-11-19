package com.github.senocak.dto.player;

import com.github.senocak.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.UUID;

@Getter
@Setter
public class PlayerPatchDto extends BaseDto {
    @Size(min = 3, max = 30)
    @Schema(example = "Lorem", description = "Name of the player", required = true, name = "firstName", type = "String")
    private String firstName;

    @Size(min = 4, max = 100)
    @Schema(example = "Ipsum", description = "Image of the player", required = true, name = "lastName", type = "String")
    private String lastName;

    @Size(min = 3)
    @Schema(example = "Spain", description = "Country of the player", required = true, name = "country", type = "String")
    private String country;

    @Min(value = 18)
    @Max(value = 40)
    @Schema(example = "33", description = "Age of the player", required = true, name = "age", type = "String")
    private Integer age;

    @Schema(example = "GoalKeeper", description = "Position of the player", required = true, name = "position", type = "String")
    private String position;

    @Schema(example = "1300000", description = "Market value of the player", required = true, name = "marketValue", type = "String")
    private Integer marketValue;

    @Schema(example = "4f0ae046-5ca3-4dfa-9904-b836cf8c1ff0", description = "Identifier of the team", required = true, name = "teamId", type = "UUID")
    private UUID teamId;
}
