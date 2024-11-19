package com.github.senocak.dto.player;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.senocak.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Builder
@JsonPropertyOrder({"player", "players", "next", "total"})
public class PlayerWrapperDto extends BaseDto {
    @JsonProperty("player")
    @Schema(description = "Player wrapper", required = true, name = "player", type = "TeamDto")
    private PlayerDto playerDto;

    @JsonProperty("players")
    @ArraySchema(schema = @Schema(description = "Players wrapper", required = true, type = "PlayerDto"))
    private List<PlayerDto> playerDtos;

    @Schema(example = "1", description = "Total page", required = true, name = "total", type = "Long")
    private Long total;

    @Schema(example = "0", description = "Next page", required = true, name = "next", type = "Long")
    private Long next;
}
