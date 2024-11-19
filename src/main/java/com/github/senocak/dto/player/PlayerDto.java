package com.github.senocak.dto.player;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.senocak.dto.BaseDto;
import com.github.senocak.dto.team.TeamDto;
import com.github.senocak.util.AppConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder({"id", "firstName", "lastName", "country", "age", "position", "marketValue", "team"})
public class PlayerDto extends BaseDto {
    @Schema(example = "4f0ae046-5ca3-4dfa-9904-b836cf8c1ff0", description = "uuid of the player", required = true, name = "id", type = "String")
    private String id;

    @Schema(example = "Lorem", description = "First name of the player", required = true, name = "firstName", type = "String")
    private String firstName;

    @Schema(example = "Ipsum", description = "Last name of the player", required = true, name = "lastName", type = "String")
    private String lastName;

    @Schema(example = "Turkey", description = "Country of the player", required = true, name = "country", type = "String")
    private String country;

    @Schema(example = "33", description = "Age of the player", required = true, name = "age", type = "Integer")
    private int age;

    @Schema(example = "GoalKeeper", description = "Position of the player", required = true, name = "position", type = "String")
    private AppConstants.PlayerPosition position;

    @Schema(example = "100000", description = "Market value of the player", required = true, name = "marketValue", type = "Integer")
    private int marketValue;

    @JsonProperty("team")
    @Schema(description = "Team of the player", required = true, name = "team")
    private TeamDto teamDto;
}
