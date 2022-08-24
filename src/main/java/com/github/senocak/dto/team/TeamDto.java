package com.github.senocak.dto.team;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.senocak.dto.BaseDto;
import com.github.senocak.dto.user.UserResponse;
import com.github.senocak.dto.player.PlayerDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@JsonPropertyOrder({"id", "name", "country", "availableCash", "user", "players", "createdAt", "updatedAt"})
public class TeamDto extends BaseDto {
    @Schema(example = "4f0ae046-5ca3-4dfa-9904-b836cf8c1ff0", description = "uuid of the team", required = true, name = "id", type = "String")
    private String id;

    @Schema(example = "Fenerbahçe", description = "name of the team", required = true, name = "name", type = "String")
    private String name;

    @Schema(example = "Turkey", description = "Country of the team", required = true, name = "country", type = "String")
    private String country;

    @Schema(example = "1000000", description = "Available Cash", required = true, name = "availableCash", type = "Integer")
    private int availableCash;

    @Schema(description = "User detail of team", required = true, name = "user", type = "UserResponse")
    private UserResponse user;

    @ArraySchema(schema = @Schema(description = "Players of the team", required = true, type = "PlayerDto"))
    private List<PlayerDto> players;

    @Schema(example = "2022-06-24 13:25:24", description = "Date of the creation", required = true, name = "createdAt", type = "Long")
    private Long createdAt;

    @Schema(example = "2022-06-24 13:25:24", description = "Date of the update", required = true, name = "createdAt", type = "Long")
    private Long updatedAt;
}