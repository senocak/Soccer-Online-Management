package com.github.senocak.service;

import com.github.senocak.dto.team.TeamCreateDto;
import com.github.senocak.dto.team.TeamUpdateDto;
import com.github.senocak.dto.team.TeamWrapperDto;
import com.github.senocak.exception.ServerException;
import com.github.senocak.factory.TeamFactory;
import com.github.senocak.factory.UserFactory;
import com.github.senocak.model.Team;
import com.github.senocak.model.User;
import com.github.senocak.repository.TeamRepository;
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

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for TeamService")
public class TeamServiceTest {
    @InjectMocks TeamService teamService;

    @Mock TeamRepository teamRepository;
    @Mock UserService userService;
    private final User user = UserFactory.createUser(null);

    @BeforeEach
    public void setUp() throws ServerException {
        Mockito.lenient().when(userService.loggedInUser()).thenReturn(user);
    }

    @Nested
    class CreateTeamTest {
        private final TeamCreateDto teamCreateDto = new TeamCreateDto();

        @BeforeEach
        public void setup() {
            teamCreateDto.setName("Team1");
            teamCreateDto.setUsername("user1");
            teamCreateDto.setCountry("US");
            teamCreateDto.setAvailableCash(30);
        }

        @Test
        public void givenExist_WhenCreateTeam_ThenThrowServerException() {
            // Given
            Team team = TeamFactory.createTeam(null);
            Mockito.doReturn(Optional.of(team)).when(teamRepository)
                    .findByIdOrName(teamCreateDto.getName());
            // When
            Executable closureToTest = () -> teamService.createTeam(teamCreateDto);
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        public void given_WhenCreateTeam_ThenAssertResult() throws ServerException {
            // Given
            Mockito.doReturn(null).when(teamRepository).findByIdOrName(teamCreateDto.getName());
            Team team = TeamFactory.createTeam(null);
            Mockito.doReturn(team).when(teamRepository).save(Mockito.any(Team.class));
            // When
            Team createTeam = teamService.createTeam(teamCreateDto);
            // Then
            Assertions.assertEquals(team, createTeam);
        }
    }

    @Nested
    class ConvertEntityToDtoTest {
        @Test
        public void given_WhenConvertEntityToDto_ThenAssertResult() throws ServerException {
            // Given
            Team team = TeamFactory.createTeam(null);
            // When
            TeamWrapperDto convertEntityToDto = TeamService.convertEntityToDto(team);
            // Then
            Assertions.assertEquals(team.getName(), convertEntityToDto.getTeamDto().getName());
            Assertions.assertEquals(team.getCountry(), convertEntityToDto.getTeamDto().getCountry());
            Assertions.assertEquals(team.getAvailableCash(), convertEntityToDto.getTeamDto().getAvailableCash());
            Assertions.assertEquals(1, convertEntityToDto.getTeamDto().getPlayers().size());
            Assertions.assertEquals(team.getPlayers().get(0).getFirstName(), convertEntityToDto.getTeamDto().getPlayers().get(0).getFirstName());
        }
    }

    @Nested
    class FindTeamByIdOrNameTest {
        @Test
        public void givenNotFound_WhenFindTeamByIdOrName_ThenThrowServerException() {
            // Given
            Mockito.doReturn(null).when(teamRepository).findByIdOrName("id");
            // When
            Executable closureToTest = () -> teamService.findTeamByIdOrName("id");
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        public void given_WhenFindTeamById_ThenAssertResult() throws ServerException {
            // Given
            Team team = TeamFactory.createTeam(null);
            Mockito.doReturn(Optional.of(team)).when(teamRepository).findByIdOrName("id");
            // When
            Team findTeamByIdOrName = teamService.findTeamByIdOrName("id");
            // Then
            Assertions.assertEquals(team, findTeamByIdOrName);
        }
    }

    @Nested
    class GetAllTest {
        @Test
        public void given_WhenGetAll_ThenAssertResult() {
            // Given
            Team team = TeamFactory.createTeam(null);
            Page<Team> all = new PageImpl<>(Collections.singletonList(team));
            Mockito.doReturn(all).when(teamRepository)
                    .findAll(PageRequest.of(10, 10));
            // When
            TeamWrapperDto getAll = teamService.getAll(10,10);
            // Then
            Assertions.assertNotNull(getAll.getTeamsDto());
            Assertions.assertEquals(1,getAll.getTeamsDto().size());
            Assertions.assertEquals(team.getName(), getAll.getTeamsDto().get(0).getName());
            Assertions.assertEquals(team.getCountry(), getAll.getTeamsDto().get(0).getCountry());
            Assertions.assertEquals(team.getAvailableCash(), getAll.getTeamsDto().get(0).getAvailableCash());
            Assertions.assertEquals(1, getAll.getTotal());
            Assertions.assertEquals(0, getAll.getNext());
        }
    }

    @Nested
    class DeleteTeamTest {
        @Test
        public void given_WhenDeleteTeam_ThenAssertResult() throws ServerException {
            // Given
            Team team = TeamFactory.createTeam(null);
            Mockito.doReturn(Optional.of(team)).when(teamRepository).findByIdOrName("id");
            // When
            teamService.deleteTeam("id");
            // Then
            Mockito.verify(teamRepository).delete(team);
        }
    }

    @Nested
    class UpdateTeamTest {
        private final TeamUpdateDto teamUpdateDto = new TeamUpdateDto();

        @BeforeEach
        public void setup() {
            teamUpdateDto.setName("Team1");
            teamUpdateDto.setCountry("US");
        }

        @Test
        public void givenNotExist_WhenUpdateTeam_ThenThrowServerException() {
            // Given
            Mockito.doReturn(null).when(teamRepository).findByIdOrName("id");
            // When
            Executable closureToTest = () -> teamService.updateTeam("id", teamUpdateDto);
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        public void given_WhenUpdateTeam_ThenAssertResult() throws ServerException {
            // Given
            Team team = TeamFactory.createTeam(null);
            Mockito.doReturn(team).when(teamRepository).save(Mockito.any(Team.class));
            Mockito.doReturn(Optional.of(team)).when(teamRepository).findByIdOrName("id");
            // When
            Team updateTeam = teamService.updateTeam("id", teamUpdateDto);
            // Then
            Assertions.assertEquals(team, updateTeam);
        }
    }
}
