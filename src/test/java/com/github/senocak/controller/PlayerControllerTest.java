package com.github.senocak.controller;

import com.github.senocak.dto.player.PlayerCreateDto;
import com.github.senocak.dto.player.PlayerDto;
import com.github.senocak.dto.player.PlayerPatchDto;
import com.github.senocak.dto.player.PlayerPutDto;
import com.github.senocak.dto.player.PlayerWrapperDto;
import com.github.senocak.exception.ServerException;
import com.github.senocak.factory.PlayerFactory;
import com.github.senocak.factory.TeamFactory;
import com.github.senocak.factory.UserFactory;
import com.github.senocak.model.Player;
import com.github.senocak.model.User;
import com.github.senocak.service.DtoConverter;
import com.github.senocak.service.PlayerService;
import com.github.senocak.service.UserService;
import com.github.senocak.util.AppConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for PlayerController")
public class PlayerControllerTest {
    @InjectMocks PlayerController playerController;

    @Mock PlayerService playerService;
    @Mock UserService userService;
    @Mock BindingResult bindingResult;
    User user = UserFactory.createUser(null);

    @BeforeEach
    public void setup() throws ServerException {
        Mockito.lenient().doReturn(user).when(userService).loggedInUser();
    }

    @Nested
    class CreateTest {
        private final PlayerCreateDto playerCreateDto = new PlayerCreateDto();

        @Test
        void given_whenCreate_thenReturn201() throws ServerException {
            // Given
            Player player = PlayerFactory.createPlayer(null);
            Mockito.doReturn(player).when(playerService).createPlayer(playerCreateDto);
            // When
            ResponseEntity<PlayerWrapperDto> create = playerController.create(playerCreateDto, bindingResult);
            // Then
            Assertions.assertNotNull(create);
            Assertions.assertNotNull(create.getStatusCode());
            Assertions.assertEquals(HttpStatus.CREATED, create.getStatusCode());
            Assertions.assertNotNull(create.getBody());
            Assertions.assertNotNull(create.getBody().getPlayerDto());
            Assertions.assertEquals(player.getFirstName(), create.getBody().getPlayerDto().getFirstName());
            Assertions.assertEquals(player.getLastName(), create.getBody().getPlayerDto().getLastName());
            Assertions.assertEquals(player.getCountry(), create.getBody().getPlayerDto().getCountry());
            Assertions.assertEquals(player.getAge(), create.getBody().getPlayerDto().getAge());
            Assertions.assertEquals(player.getPosition(), create.getBody().getPlayerDto().getPosition());
            Assertions.assertEquals(player.getMarketValue(), create.getBody().getPlayerDto().getMarketValue());
        }
    }

    @Nested
    class GetAllTest {

        @Test
        void given_whenGetAll_thenReturn200() {
            // Given
            List<Player> playerList = new ArrayList<>();
            Player createPlayer = PlayerFactory.createPlayer(null);
            playerList.add(createPlayer);
            Page<Player> all = new PageImpl<>(playerList);
            List<PlayerDto> playerDtos = all.stream().map(t -> {
                PlayerDto playerDto = DtoConverter.convertEntityToDto(t, t.getTeam() != null);
                if (playerDto.getTeamDto() != null) {
                    playerDto.getTeamDto().setUser(null);
                    playerDto.getTeamDto().setPlayers(null);
                }
                return playerDto;
            }).collect(Collectors.toList());
            PlayerWrapperDto build = PlayerWrapperDto.builder()
                    .playerDtos(playerDtos)
                    .next(0L)
                    .total(all.getTotalElements())
                    .build();
            Mockito.doReturn(build).when(playerService).getAll(0, 0);
            // When
            ResponseEntity<PlayerWrapperDto> getAll = playerController.getAll(0, 0);
            // Then
            Assertions.assertNotNull(getAll);
            Assertions.assertNotNull(getAll.getStatusCode());
            Assertions.assertEquals(HttpStatus.OK, getAll.getStatusCode());
            Assertions.assertNotNull(getAll.getBody());
            Assertions.assertEquals(0, getAll.getBody().getNext());
            Assertions.assertEquals(playerDtos.size(), getAll.getBody().getTotal());
            Assertions.assertEquals(playerDtos.size(), getAll.getBody().getPlayerDtos().size());
            Assertions.assertEquals(createPlayer.getFirstName(), getAll.getBody().getPlayerDtos().get(0).getFirstName());
            Assertions.assertEquals(createPlayer.getLastName(), getAll.getBody().getPlayerDtos().get(0).getLastName());
            Assertions.assertEquals(createPlayer.getCountry(), getAll.getBody().getPlayerDtos().get(0).getCountry());
            Assertions.assertEquals(createPlayer.getAge(), getAll.getBody().getPlayerDtos().get(0).getAge());
            Assertions.assertEquals(createPlayer.getPosition(), getAll.getBody().getPlayerDtos().get(0).getPosition());
            Assertions.assertEquals(createPlayer.getMarketValue(), getAll.getBody().getPlayerDtos().get(0).getMarketValue());
        }
    }

    @Nested
    class GetSingleTest {

        @Test
        void givenNotValidPlayer_whenGetSingle_thenReturnServerException() throws ServerException {
            // Given
            Player player = PlayerFactory.createPlayer(null);
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_ADMIN));
            Mockito.doReturn(player).when(playerService).findPlayerById("id");
            // When
            Executable closureToTest = () -> playerController.getPlayer("id");
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        void given_whenGetSingle_thenReturn200() throws ServerException {
            // Given
            Player player = PlayerFactory.createPlayer(TeamFactory.createTeam(null));
            Mockito.doReturn(player).when(playerService).findPlayerById("id");
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            // When
            ResponseEntity<PlayerWrapperDto> getSingle = playerController.getPlayer("id");
            // Then
            Assertions.assertNotNull(getSingle);
            Assertions.assertNotNull(getSingle.getStatusCode());
            Assertions.assertEquals(HttpStatus.OK, getSingle.getStatusCode());
            Assertions.assertNotNull(getSingle.getBody());
            Assertions.assertNotNull(getSingle.getBody().getPlayerDto());
            Assertions.assertEquals(player.getId(), getSingle.getBody().getPlayerDto().getId());
            Assertions.assertEquals(player.getFirstName(), getSingle.getBody().getPlayerDto().getFirstName());
            Assertions.assertEquals(player.getLastName(), getSingle.getBody().getPlayerDto().getLastName());
            Assertions.assertEquals(player.getCountry(), getSingle.getBody().getPlayerDto().getCountry());
            Assertions.assertEquals(player.getAge(), getSingle.getBody().getPlayerDto().getAge());
            Assertions.assertEquals(player.getPosition(), getSingle.getBody().getPlayerDto().getPosition());
            Assertions.assertEquals(player.getMarketValue(), getSingle.getBody().getPlayerDto().getMarketValue());
            Assertions.assertEquals(player.getTeam().getName(), getSingle.getBody().getPlayerDto().getTeamDto().getName());
        }
    }

    @Nested
    class UpdateTest {
        private final PlayerPutDto playerPutDto = new PlayerPutDto();

        @Test
        void givenNotValidPlayer_whenUpdate_thenReturnServerException() throws ServerException {
            // Given
            playerPutDto.setFirstName("firstName");
            playerPutDto.setLastName("lastName");
            playerPutDto.setCountry("country");
            playerPutDto.setAge(20);
            playerPutDto.setPosition("position");
            playerPutDto.setMarketValue(1000);
            playerPutDto.setTeamId(UUID.randomUUID());
            Player player = PlayerFactory.createPlayer(null);
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_ADMIN));
            Mockito.doReturn(player).when(playerService).findPlayerById("id");
            // When
            Executable closureToTest = () -> playerController.updatePlayer("id", playerPutDto, bindingResult);
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        void given_whenUpdate_thenReturn200() throws ServerException {
            // Given
            playerPutDto.setFirstName("firstName");
            playerPutDto.setLastName("lastName");
            playerPutDto.setCountry("country");
            playerPutDto.setAge(20);
            playerPutDto.setPosition("position");
            playerPutDto.setMarketValue(1000);
            playerPutDto.setTeamId(UUID.randomUUID());
            Player player = PlayerFactory.createPlayer(null);
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            Mockito.doReturn(player).when(playerService).updatePlayer("id", playerPutDto);
            // When
            ResponseEntity<PlayerWrapperDto> update = playerController.updatePlayer("id", playerPutDto, bindingResult);
            // Then
            Assertions.assertNotNull(update);
            Assertions.assertNotNull(update.getStatusCode());
            Assertions.assertEquals(HttpStatus.OK, update.getStatusCode());
            Assertions.assertNotNull(update.getBody());
            Assertions.assertNotNull(update.getBody().getPlayerDto());
            Assertions.assertEquals(player.getId(), update.getBody().getPlayerDto().getId());
            Assertions.assertEquals(player.getFirstName(), update.getBody().getPlayerDto().getFirstName());
            Assertions.assertEquals(player.getLastName(), update.getBody().getPlayerDto().getLastName());
            Assertions.assertEquals(player.getCountry(), update.getBody().getPlayerDto().getCountry());
            Assertions.assertEquals(player.getAge(), update.getBody().getPlayerDto().getAge());
            Assertions.assertEquals(player.getPosition(), update.getBody().getPlayerDto().getPosition());
            Assertions.assertEquals(player.getMarketValue(), update.getBody().getPlayerDto().getMarketValue());
        }
    }

    @Nested
    class UpdatePartiallyTest {
        private final PlayerPatchDto playerPatchDto = new PlayerPatchDto();

        @Test
        void givenNotValidPlayer_whenUpdatePartially_thenReturnServerException() throws ServerException {
            // Given
            playerPatchDto.setFirstName("firstName");
            playerPatchDto.setLastName("lastName");
            playerPatchDto.setCountry("country");
            playerPatchDto.setAge(20);
            playerPatchDto.setPosition("position");
            playerPatchDto.setMarketValue(1000);
            playerPatchDto.setTeamId(UUID.randomUUID());
            Player player = PlayerFactory.createPlayer(null);
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_ADMIN));
            Mockito.doReturn(player).when(playerService).findPlayerById("id");
            // When
            Executable closureToTest = () -> playerController.updatePartiallyPlayer("id", playerPatchDto, bindingResult);
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        void given_whenUpdatePartially_thenReturn200() throws ServerException {
            // Given
            playerPatchDto.setFirstName("firstName");
            playerPatchDto.setLastName("lastName");
            playerPatchDto.setCountry("country");
            playerPatchDto.setAge(20);
            playerPatchDto.setPosition("position");
            playerPatchDto.setMarketValue(1000);
            playerPatchDto.setTeamId(UUID.randomUUID());
            Player player = PlayerFactory.createPlayer(null);
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            Mockito.doReturn(player).when(playerService).updatePartiallyPlayer("id", playerPatchDto);
            // When
            ResponseEntity<PlayerWrapperDto> update = playerController.updatePartiallyPlayer("id", playerPatchDto, bindingResult);
            // Then
            Assertions.assertNotNull(update);
            Assertions.assertNotNull(update.getStatusCode());
            Assertions.assertEquals(HttpStatus.OK, update.getStatusCode());
            Assertions.assertNotNull(update.getBody());
            Assertions.assertNotNull(update.getBody().getPlayerDto());
            Assertions.assertEquals(player.getId(), update.getBody().getPlayerDto().getId());
            Assertions.assertEquals(player.getFirstName(), update.getBody().getPlayerDto().getFirstName());
            Assertions.assertEquals(player.getLastName(), update.getBody().getPlayerDto().getLastName());
            Assertions.assertEquals(player.getCountry(), update.getBody().getPlayerDto().getCountry());
            Assertions.assertEquals(player.getAge(), update.getBody().getPlayerDto().getAge());
            Assertions.assertEquals(player.getPosition(), update.getBody().getPlayerDto().getPosition());
            Assertions.assertEquals(player.getMarketValue(), update.getBody().getPlayerDto().getMarketValue());
        }
    }

    @Nested
    class DeleteTest {

        @Test
        void givenNotValidPlayer_whenDelete_thenReturnServerException() throws ServerException {
            // Given
            Player player = PlayerFactory.createPlayer(null);
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_ADMIN));
            Mockito.doReturn(player).when(playerService).findPlayerById("id");
            // When
            Executable closureToTest = () -> playerController.deletePlayer("id");
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        void given_whenDelete_thenReturn200() throws ServerException {
            // Given
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            Mockito.doNothing().when(playerService).deletePlayer("id");
            // When
            ResponseEntity<Void> create = playerController.deletePlayer("id");
            // Then
            Assertions.assertNotNull(create);
            Assertions.assertNotNull(create.getStatusCode());
            Assertions.assertEquals(HttpStatus.NO_CONTENT, create.getStatusCode());
        }
    }
}
