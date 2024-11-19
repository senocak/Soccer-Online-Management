package com.github.senocak.dto.team;

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
@JsonPropertyOrder({"team", "teams", "next", "total"})
public class TeamWrapperDto extends BaseDto {
    @JsonProperty("team")
    @Schema(description = "Team wrapper", required = true, name = "team", type = "TeamDto")
    private TeamDto teamDto;

    @JsonProperty("teams")
    @ArraySchema(schema = @Schema(description = "Teams wrapper", required = true, type = "TeamDto"))
    private List<TeamDto> teamsDto;

    @Schema(example = "1", description = "Total page", required = true, name = "total", type = "Long")
    private Long total;

    @Schema(example = "0", description = "Next page", required = true, name = "next", type = "Long")
    private Long next;
}