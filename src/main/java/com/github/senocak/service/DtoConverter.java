package com.github.senocak.service;

import com.github.senocak.dto.auth.RoleResponse;
import com.github.senocak.dto.player.PlayerDto;
import com.github.senocak.dto.team.TeamDto;
import com.github.senocak.dto.transfer.TransferDto;
import com.github.senocak.dto.user.UserResponse;
import com.github.senocak.model.Player;
import com.github.senocak.model.Role;
import com.github.senocak.model.Team;
import com.github.senocak.model.Transfer;
import com.github.senocak.model.User;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DtoConverter {
    private DtoConverter(){}

    /**
     * @param user -- User object to convert to dto object
     * @return -- UserResponse object
     */
    public static UserResponse convertEntityToDto(User user){
        UserResponse userResponse = new UserResponse();
        userResponse.setName(user.getName());
        userResponse.setEmail(user.getEmail());
        userResponse.setUsername(user.getUsername());
        userResponse.setRoles(
            user.getRoles().stream()
                    .map(DtoConverter::convertEntityToDto)
                    .collect(Collectors.toSet())
        );
        if (Objects.nonNull(user.getTeam())) {
            TeamDto teamDto = convertEntityToDto(user.getTeam(), false);
            teamDto.setUser(null);
            userResponse.setTeamDto(teamDto);
        }
        return userResponse;
    }

    /**
     * @param role -- role object to convert to dto object
     * @return -- RoleResponse object
     */
    public static RoleResponse convertEntityToDto(Role role){
        RoleResponse roleResponse = new RoleResponse();
        roleResponse.setName(role.getName());
        return roleResponse;
    }

    /**
     * @param team -- Date object to convert to long timestamp
     * @return -- converted timestamp object that is long type
     */
    public static TeamDto convertEntityToDto(Team team, boolean isSetUser){
        TeamDto teamDto = new TeamDto();
        teamDto.setId(team.getId());
        teamDto.setName(team.getName());
        teamDto.setCountry(team.getCountry());
        teamDto.setAvailableCash(team.getAvailableCash());
        if (isSetUser && team.getUser() != null)
            teamDto.setUser(convertEntityToDto(team.getUser()));
        if (team.getPlayers() != null) {
            List<PlayerDto> playerDtos = team.getPlayers().stream()
                    .map(player -> convertEntityToDto(player, false)).collect(Collectors.toList());
            teamDto.setPlayers(playerDtos);
        }
        return teamDto;
    }

    /**
     * @param player -- Date object to convert to long timestamp
     * @return -- converted timestamp object that is long type
     */
    public static PlayerDto convertEntityToDto(Player player, boolean isSetTeam){
        PlayerDto playerDto = new PlayerDto();
        playerDto.setId(player.getId());
        playerDto.setFirstName(player.getFirstName());
        playerDto.setLastName(player.getLastName());
        playerDto.setCountry(player.getCountry());
        playerDto.setAge(player.getAge());
        playerDto.setPosition(player.getPosition());
        playerDto.setMarketValue(player.getMarketValue());
        if (isSetTeam) {
            TeamDto teamDto = convertEntityToDto(player.getTeam(), true);
            teamDto.setPlayers(null);
            teamDto.setUser(null);
            playerDto.setTeamDto(teamDto);
        }
        return playerDto;
    }

    /**
     * @param transfer -- Transfer object to convert to dto object
     * @return  -- Transfer dto object
     */
    public static TransferDto convertEntityToDto(Transfer transfer){
        TransferDto transferDto = new TransferDto();
        transferDto.setId(transfer.getId());
        transferDto.setMarketValue(transfer.getMarketValue());
        transferDto.setAskedPrice(transfer.getAskedPrice());

        if (Objects.nonNull(transfer.getPlayer())) {
            PlayerDto getPlayer = convertEntityToDto(transfer.getPlayer(), false);
            getPlayer.setTeamDto(null);
            transferDto.setPlayer(getPlayer);
        }

        if (Objects.nonNull(transfer.getTransferredFrom())){
            TeamDto getTransferredFrom = convertEntityToDto(transfer.getTransferredFrom(), false);
            getTransferredFrom.setPlayers(null);
            getTransferredFrom.setUser(null);
            transferDto.setTransferredFrom(getTransferredFrom);
        }

        if (Objects.nonNull(transfer.getTransferredTo())) {
            TeamDto getTransferredTo = convertEntityToDto(transfer.getTransferredTo(), false);
            getTransferredTo.setPlayers(null);
            getTransferredTo.setUser(null);
            transferDto.setTransferredTo(getTransferredTo);
        }

        transferDto.setTransferred(transfer.isTransferred());
        return transferDto;
    }
}
