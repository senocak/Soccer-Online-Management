package com.github.senocak.dto.team;

import com.github.senocak.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.Size;

@Getter
@Setter
public class TeamUpdateDto extends BaseDto {
    @Size(min = 4, max = 40)
    @Schema(example = "Galatasaray", description = "New name of the team", required = true, name = "name", type = "String")
    private String name;

    @Size(min = 3)
    @Schema(example = "Ireland", description = "New county of the team", required = true, name = "country", type = "String")
    private String country;
}
