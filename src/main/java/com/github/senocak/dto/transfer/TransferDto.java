package com.github.senocak.dto.transfer;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.senocak.dto.BaseDto;
import com.github.senocak.dto.player.PlayerDto;
import com.github.senocak.dto.team.TeamDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder({"id", "player", "marketValue", "askedPrice", "transferredFrom", "transferredTo", "transferred"})
public class TransferDto extends BaseDto {
    @Schema(example = "1cb9374e-4e52-4142-a1af-16144ef4a271", description = "Id of the transfer", required = true, name = "id", type = "String")
    private String id;

    @Schema(description = "player of the transfer", required = true, name = "player", type = "PlayerDto")
    private PlayerDto player;

    @Schema(example = "123456", description = "Market value of the transfer", required = true, name = "marketValue", type = "String")
    private Integer marketValue;

    @Schema(example = "4356", description = "Asked price of the transfer", required = true, name = "askedPrice", type = "String")
    private Integer askedPrice;

    @Schema(description = "Transferred from", required = true, name = "transferredFrom", type = "TeamDto")
    private TeamDto transferredFrom;

    @Schema(description = "Transferred to", required = true, name = "transferredTo", type = "TeamDto")
    private TeamDto transferredTo;

    @Schema(example = "false", description = "Is transferred", required = true, name = "transferred", type = "Boolean")
    private Boolean transferred;
}