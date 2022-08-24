package com.github.senocak.service;

import com.github.senocak.dto.player.PlayerCreateDto;
import com.github.senocak.dto.player.PlayerPatchDto;
import com.github.senocak.dto.player.PlayerPutDto;
import com.github.senocak.dto.player.PlayerWrapperDto;
import com.github.senocak.exception.ServerException;
import com.github.senocak.factory.PlayerFactory;
import com.github.senocak.factory.UserFactory;
import com.github.senocak.model.Player;
import com.github.senocak.model.User;
import com.github.senocak.repository.PlayerRepository;
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
import org.springframework.data.domain.PageRequest;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for PlayerService")
public class PlayerServiceTest {
    @InjectMocks PlayerService playerService;
    @Mock PlayerRepository playerRepository;
    @Mock TeamService teamService;
    @Mock UserService userService;
    private final User user = UserFactory.createUser(null);

    @BeforeEach
    public void setUp() throws ServerException {
        Mockito.lenient().when(userService.loggedInUser()).thenReturn(user);
    }

    @Nested
    class CreatePlayerTest {
        private final PlayerCreateDto playerCreateDto = new PlayerCreateDto();

        @BeforeEach
        public void setup() {
            playerCreateDto.setFirstName("John");
            playerCreateDto.setLastName("Doe");
            playerCreateDto.setCountry("US");
            playerCreateDto.setAge(30);
            playerCreateDto.setPosition(AppConstants.PlayerPosition.Forward.toString());
            playerCreateDto.setMarketValue(100000);
            playerCreateDto.setTeamId(UUID.randomUUID());
        }

        @Test
        public void givenNotExist_WhenCreatePlayer_ThenThrowServerException() {
            // Given
            Player player = PlayerFactory.createPlayer(null);
            Mockito.doReturn(Optional.of(player)).when(playerRepository)
                    .findPlayerByFirstNameAndLastName(playerCreateDto.getFirstName(), playerCreateDto.getLastName());
            // When
            Executable closureToTest = () -> playerService.createPlayer(playerCreateDto);
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        public void givenInvalidPosition_WhenCreatePlayer_ThenThrowServerException() {
            // Given
            playerCreateDto.setPosition("invalid");
            Mockito.doReturn(null).when(playerRepository)
                    .findPlayerByFirstNameAndLastName(playerCreateDto.getFirstName(), playerCreateDto.getLastName());
            // When
            Executable closureToTest = () -> playerService.createPlayer(playerCreateDto);
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        public void given_WhenCreatePlayer_ThenAssertResult() throws ServerException {
            // Given
            Player player = PlayerFactory.createPlayer(null);
            Mockito.doReturn(player).when(playerRepository).save(Mockito.any(Player.class));
            Mockito.doReturn(null).when(playerRepository)
                    .findPlayerByFirstNameAndLastName(playerCreateDto.getFirstName(), playerCreateDto.getLastName());
            // When
            Player createPlayer = playerService.createPlayer(playerCreateDto);
            // Then
            Assertions.assertEquals(player, createPlayer);
        }
    }

    @Nested
    class ConvertEntityToDtoTest {
        @Test
        public void given_WhenConvertEntityToDto_ThenAssertResult() {
            // Given
            Player player = PlayerFactory.createPlayer(null);
            // When
            PlayerWrapperDto convertEntityToDto = PlayerService.convertEntityToDto(player);
            // Then
            Assertions.assertEquals(player.getFirstName(), convertEntityToDto.getPlayerDto().getFirstName());
            Assertions.assertEquals(player.getLastName(), convertEntityToDto.getPlayerDto().getLastName());
            Assertions.assertEquals(player.getCountry(), convertEntityToDto.getPlayerDto().getCountry());
            Assertions.assertEquals(player.getAge(), convertEntityToDto.getPlayerDto().getAge());
            Assertions.assertEquals(player.getMarketValue(), convertEntityToDto.getPlayerDto().getMarketValue());
            Assertions.assertEquals(player.getPosition(), convertEntityToDto.getPlayerDto().getPosition());
            Assertions.assertNull(convertEntityToDto.getPlayerDto().getTeamDto());
        }
    }

    @Nested
    class FindPlayerByIdTest {
        @Test
        public void givenNotFound_WhenFindPlayerById_ThenThrowServerException() {
            // Given
            Mockito.doReturn(null).when(playerRepository).findPlayerById("id");
            // When
            Executable closureToTest = () -> playerService.findPlayerById("id");
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        public void given_WhenFindPlayerById_ThenAssertResult() throws ServerException {
            // Given
            Player player = PlayerFactory.createPlayer(null);
            Mockito.doReturn(Optional.of(player)).when(playerRepository).findPlayerById("id");
            // When
            Player findPlayerById = playerService.findPlayerById("id");
            // Then
            Assertions.assertEquals(player, findPlayerById);
        }
    }

    @Nested
    class FindPlayerByFirstNameAndLastNameTest {
        @Test
        public void givenNotFound_WhenFindPlayerByFirstNameAndLastName_ThenThrowServerException() {
            // Given
            Mockito.doReturn(null).when(playerRepository).findPlayerByFirstNameAndLastName("firstName", "lastName");
            // When
            Executable closureToTest = () -> playerService.findPlayerByFirstNameAndLastName("firstName", "lastName");
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        public void given_WhenFindPlayerByFirstNameAndLastName_ThenAssertResult() throws ServerException {
            // Given
            Player player = PlayerFactory.createPlayer(null);
            Mockito.doReturn(Optional.of(player)).when(playerRepository)
                    .findPlayerByFirstNameAndLastName("firstName", "lastName");
            // When
            Player findPlayerById = playerService.findPlayerByFirstNameAndLastName("firstName", "lastName");
            // Then
            Assertions.assertEquals(player, findPlayerById);
        }
    }

    @Nested
    class GetAllTest {
        @Test
        public void given_WhenGetAll_ThenAssertResult() throws ServerException {
            // Given
            Player player = PlayerFactory.createPlayer(null);
            Page<Player> all = new PageImpl<>(Collections.singletonList(player));
            Mockito.doReturn(all).when(playerRepository)
                    .findAll(PageRequest.of(10, 10));
            // When
            PlayerWrapperDto getAll = playerService.getAll(10,10);
            // Then
            Assertions.assertNotNull(getAll.getPlayerDtos());
            Assertions.assertEquals(1,getAll.getPlayerDtos().size());
            Assertions.assertEquals(player.getFirstName(), getAll.getPlayerDtos().get(0).getFirstName());
            Assertions.assertEquals(player.getLastName(), getAll.getPlayerDtos().get(0).getLastName());
            Assertions.assertEquals(player.getCountry(), getAll.getPlayerDtos().get(0).getCountry());
            Assertions.assertEquals(player.getAge(), getAll.getPlayerDtos().get(0).getAge());
            Assertions.assertEquals(player.getPosition(), getAll.getPlayerDtos().get(0).getPosition());
            Assertions.assertEquals(player.getMarketValue(), getAll.getPlayerDtos().get(0).getMarketValue());
            Assertions.assertEquals(1, getAll.getTotal());
            Assertions.assertEquals(0, getAll.getNext());
        }
    }

    @Nested
    class DeletePlayerTest {
        @Test
        public void given_WhenDeletePlayer_ThenAssertResult() throws ServerException {
            // Given
            Player player = PlayerFactory.createPlayer(null);
            Mockito.doReturn(Optional.of(player)).when(playerRepository).findPlayerById("id");
            // When
            playerService.deletePlayer("id");
            // Then
            Mockito.verify(playerRepository).delete(player);
        }
    }

    @Nested
    class UpdatePlayerTest {
        private final PlayerPutDto playerPutDto = new PlayerPutDto();

        @BeforeEach
        public void setup() {
            playerPutDto.setFirstName("John");
            playerPutDto.setLastName("Doe");
            playerPutDto.setCountry("US");
            playerPutDto.setAge(30);
            playerPutDto.setPosition(AppConstants.PlayerPosition.Forward.toString());
            playerPutDto.setMarketValue(100000);
            playerPutDto.setTeamId(UUID.randomUUID());
        }

        @Test
        public void givenNotExist_WhenUpdatePlayer_ThenThrowServerException() {
            // Given
            Mockito.doReturn(null).when(playerRepository).findPlayerById("id");
            // When
            Executable closureToTest = () -> playerService.updatePlayer("id",playerPutDto);
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        public void givenInvalidPosition_WhenUpdatePlayer_ThenThrowServerException() {
            // Given
            playerPutDto.setPosition("invalid");
            Mockito.doReturn(null).when(playerRepository).findPlayerById("id");
            // When
            Executable closureToTest = () -> playerService.updatePlayer("id",playerPutDto);
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        public void givenNotAdmin_WhenUpdatePlayer_ThenAssertResult() throws ServerException {
            // Given
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            Player player = PlayerFactory.createPlayer(null);
            Mockito.doReturn(player).when(playerRepository).save(Mockito.any(Player.class));
            Mockito.doReturn(Optional.of(player)).when(playerRepository).findPlayerById("id");
            // When
            Player createPlayer = playerService.updatePlayer("id",playerPutDto);
            // Then
            Assertions.assertEquals(player, createPlayer);
        }
    }

    @Nested
    class UpdatePartiallyPlayerTest {
        private final PlayerPatchDto playerPatchDto = new PlayerPatchDto();

        @BeforeEach
        public void setup() {
            playerPatchDto.setFirstName("John");
            playerPatchDto.setLastName("Doe");
            playerPatchDto.setCountry("US");
            playerPatchDto.setAge(30);
            playerPatchDto.setPosition(AppConstants.PlayerPosition.Forward.toString());
            playerPatchDto.setMarketValue(100000);
            playerPatchDto.setTeamId(UUID.randomUUID());
        }

        @Test
        public void givenNotExist_WhenUpdatePartiallyPlayer_ThenThrowServerException() {
            // Given
            Mockito.doReturn(null).when(playerRepository).findPlayerById("id");
            // When
            Executable closureToTest = () -> playerService.updatePartiallyPlayer("id", playerPatchDto);
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        public void givenInvalidPosition_WhenUpdatePartiallyPlayer_ThenThrowServerException() {
            // Given
            playerPatchDto.setPosition("invalid");
            Mockito.doReturn(null).when(playerRepository).findPlayerById("id");
            // When
            Executable closureToTest = () -> playerService.updatePartiallyPlayer("id", playerPatchDto);
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        public void givenNotAdmin_WhenUpdatePartiallyPlayer_ThenAssertResult() throws ServerException {
            // Given
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            Player player = PlayerFactory.createPlayer(null);
            Mockito.doReturn(player).when(playerRepository).save(Mockito.any(Player.class));
            Mockito.doReturn(Optional.of(player)).when(playerRepository).findPlayerById("id");
            // When
            Player createPlayer = playerService.updatePartiallyPlayer("id", playerPatchDto);
            // Then
            Assertions.assertEquals(player, createPlayer);
        }
    }
}
