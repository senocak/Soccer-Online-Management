package com.github.senocak.service;

import com.github.senocak.dto.auth.RoleResponse;
import com.github.senocak.dto.player.PlayerDto;
import com.github.senocak.dto.team.TeamDto;
import com.github.senocak.dto.transfer.TransferDto;
import com.github.senocak.dto.user.UserResponse;
import com.github.senocak.factory.PlayerFactory;
import com.github.senocak.factory.TeamFactory;
import com.github.senocak.factory.TransferFactory;
import com.github.senocak.factory.UserFactory;
import com.github.senocak.model.Player;
import com.github.senocak.model.Role;
import com.github.senocak.model.Team;
import com.github.senocak.model.Transfer;
import com.github.senocak.model.User;
import com.github.senocak.util.AppConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for DtoConverter")
public class DtoConverterTest {

    @Test
    public void givenUser_whenConvertEntityToDto_thenAssertResult(){
        // Given
        User user = UserFactory.createUser(null);
        // When
        UserResponse convertEntityToDto = DtoConverter.convertEntityToDto(user);
        // Then
        Assertions.assertEquals(user.getName(), convertEntityToDto.getName());
        Assertions.assertEquals(user.getEmail(), convertEntityToDto.getEmail());
        Assertions.assertEquals(user.getUsername(), convertEntityToDto.getUsername());
    }

    @Test
    public void givenRole_whenConvertEntityToDto_thenAssertResult(){
        // Given
        Role role = new Role();
        role.setName(AppConstants.RoleName.ROLE_USER);
        // When
        RoleResponse convertEntityToDto = DtoConverter.convertEntityToDto(role);
        // Then
        Assertions.assertEquals(role.getName(), convertEntityToDto.getName());
    }

    @Test
    public void givenTeam_whenConvertEntityToDto_thenAssertResult(){
        // Given
        Team team = TeamFactory.createTeam(UserFactory.createUser(null));
        // When
        TeamDto convertEntityToDto = DtoConverter.convertEntityToDto(team, true);
        // Then
        Assertions.assertEquals(team.getId(), convertEntityToDto.getId());
        Assertions.assertEquals(team.getName(), convertEntityToDto.getName());
        Assertions.assertEquals(team.getCountry(), convertEntityToDto.getCountry());
        Assertions.assertEquals(team.getUser().getUsername(), convertEntityToDto.getUser().getUsername());
        Assertions.assertEquals(team.getAvailableCash(), convertEntityToDto.getAvailableCash());
        Assertions.assertEquals(1, convertEntityToDto.getPlayers().size());
        Assertions.assertEquals(team.getPlayers().get(0).getFirstName(), convertEntityToDto.getPlayers().get(0).getFirstName());
        Assertions.assertEquals(team.getPlayers().get(0).getLastName(), convertEntityToDto.getPlayers().get(0).getLastName());
        Assertions.assertEquals(team.getUser().getName(),convertEntityToDto.getUser().getName());
    }

    @Test
    public void givenPlayer_whenConvertEntityToDto_thenAssertResult(){
        // Given
        Player player = PlayerFactory.createPlayer(TeamFactory.createTeam(null));
        // When
        PlayerDto convertEntityToDto = DtoConverter.convertEntityToDto(player, true);
        // Then
        Assertions.assertEquals(player.getId(), convertEntityToDto.getId());
        Assertions.assertEquals(player.getFirstName(), convertEntityToDto.getFirstName());
        Assertions.assertEquals(player.getLastName(), convertEntityToDto.getLastName());
        Assertions.assertEquals(player.getCountry(), convertEntityToDto.getCountry());
        Assertions.assertEquals(player.getAge(), convertEntityToDto.getAge());
        Assertions.assertEquals(player.getPosition(), convertEntityToDto.getPosition());
        Assertions.assertEquals(player.getMarketValue(), convertEntityToDto.getMarketValue());
        Assertions.assertEquals(player.getTeam().getName(), convertEntityToDto.getTeamDto().getName());
        Assertions.assertEquals(player.getTeam().getCountry(), convertEntityToDto.getTeamDto().getCountry());
    }

    @Test
    public void givenTransfer_whenConvertEntityToDto_thenAssertResult(){
        // Given
        Transfer transfer = TransferFactory.createTransfer(PlayerFactory.createPlayer(null),
                TeamFactory.createTeam(null),TeamFactory.createTeam(null));
        // When
        TransferDto convertEntityToDto = DtoConverter.convertEntityToDto(transfer);
        // Then
        Assertions.assertEquals(transfer.getId(), convertEntityToDto.getId());
        Assertions.assertEquals(transfer.getMarketValue(), convertEntityToDto.getMarketValue());
        Assertions.assertEquals(transfer.getAskedPrice(), convertEntityToDto.getAskedPrice());
        Assertions.assertEquals(transfer.getPlayer().getFirstName(), convertEntityToDto.getPlayer().getFirstName());
        Assertions.assertEquals(transfer.getTransferredFrom().getName(), convertEntityToDto.getTransferredFrom().getName());
        Assertions.assertEquals(transfer.getTransferredTo().getName(), convertEntityToDto.getTransferredTo().getName());
    }
}
