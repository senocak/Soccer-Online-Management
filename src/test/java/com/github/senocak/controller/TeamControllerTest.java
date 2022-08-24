package com.github.senocak.controller;

import com.github.senocak.dto.team.TeamCreateDto;
import com.github.senocak.dto.team.TeamDto;
import com.github.senocak.dto.team.TeamUpdateDto;
import com.github.senocak.dto.team.TeamWrapperDto;
import com.github.senocak.exception.ServerException;
import com.github.senocak.factory.TeamFactory;
import com.github.senocak.factory.UserFactory;
import com.github.senocak.model.Team;
import com.github.senocak.model.User;
import com.github.senocak.service.DtoConverter;
import com.github.senocak.service.TeamService;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for TeamController")
public class TeamControllerTest {
    @InjectMocks TeamController teamController;

    @Mock TeamService teamService;
    @Mock UserService userService;
    @Mock BindingResult bindingResult;
    private final User user = UserFactory.createUser(null);

    @BeforeEach
    public void setup() throws ServerException {
        Mockito.lenient().doReturn(user).when(userService).loggedInUser();
    }

    @Nested
    class CreateTest {
        private final TeamCreateDto teamCreateDto = new TeamCreateDto();

        @Test
        void given_whenCreate_thenReturn201() throws ServerException {
            // Given
            teamCreateDto.setName("lorem");
            teamCreateDto.setCountry("USA");
            teamCreateDto.setUsername("lorem");
            teamCreateDto.setAvailableCash(100);
            Team team = TeamFactory.createTeam(null);
            Mockito.doReturn(team).when(teamService).createTeam(teamCreateDto);
            // When
            ResponseEntity<TeamWrapperDto> create = teamController.create(teamCreateDto, bindingResult);
            // Then
            Assertions.assertNotNull(create);
            Assertions.assertNotNull(create.getStatusCode());
            Assertions.assertEquals(HttpStatus.CREATED, create.getStatusCode());
            Assertions.assertNotNull(create.getBody());
            Assertions.assertNotNull(create.getBody().getTeamDto());
            Assertions.assertEquals(team.getId(), create.getBody().getTeamDto().getId());
            Assertions.assertEquals(team.getName(), create.getBody().getTeamDto().getName());
            Assertions.assertEquals(team.getCountry(), create.getBody().getTeamDto().getCountry());
            Assertions.assertEquals(team.getAvailableCash(), create.getBody().getTeamDto().getAvailableCash());
            Assertions.assertEquals(1, create.getBody().getTeamDto().getPlayers().size());
        }
    }

    @Nested
    class GetAllTest {
        @Test
        void given_whenGetAll_thenReturn200() {
            // Given
            Team team = TeamFactory.createTeam(null);
            Page<Team> all = new PageImpl<>(Collections.singletonList(team));
            List<TeamDto> teamDtos = all.stream().map(t -> {
                TeamDto teamDto = DtoConverter.convertEntityToDto(t, true);
                if (teamDto.getUser() != null) {
                    teamDto.getUser().setRoles(null);
                    teamDto.getUser().setTeamDto(null);
                }
                return teamDto;
            }).collect(Collectors.toList());
            TeamWrapperDto build = TeamWrapperDto.builder()
                    .teamsDto(teamDtos)
                    .next(0L)
                    .total(all.getTotalElements())
                    .build();

            Mockito.doReturn(build).when(teamService).getAll(0, 50);
            // When
            ResponseEntity<TeamWrapperDto> getAll = teamController.getAll(0, 50);
            // Then
            Assertions.assertNotNull(getAll);
            Assertions.assertNotNull(getAll.getStatusCode());
            Assertions.assertEquals(HttpStatus.OK, getAll.getStatusCode());
            Assertions.assertNotNull(getAll.getBody());
            Assertions.assertEquals(1, getAll.getBody().getTotal());
            Assertions.assertEquals(0, getAll.getBody().getNext());
            Assertions.assertNotNull(getAll.getBody().getTeamsDto());
            Assertions.assertEquals(1, getAll.getBody().getTeamsDto().size());
            Assertions.assertEquals(team.getName(), getAll.getBody().getTeamsDto().get(0).getName());
            Assertions.assertEquals(team.getCountry(), getAll.getBody().getTeamsDto().get(0).getCountry());
            Assertions.assertEquals(team.getAvailableCash(), getAll.getBody().getTeamsDto().get(0).getAvailableCash());
            Assertions.assertEquals(1, getAll.getBody().getTeamsDto().get(0).getPlayers().size());
        }
    }

    @Nested
    class GetTeamTest {
        @Test
        void givenInvalidTeam_whenGetTeam_thenThrowServerException() throws ServerException {
            // Given
            Team team = TeamFactory.createTeam(null);
            Mockito.doReturn(team).when(teamService).findTeamByIdOrName("id");
            user.getRoles().removeIf(r -> r.getName().equals(AppConstants.RoleName.ROLE_ADMIN));
            // When
            Executable closureToTest = () -> teamController.getTeam("id");
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        void given_whenGetTeam_thenReturn200() throws ServerException {
            // Given
            Team team = TeamFactory.createTeam(null);
            Mockito.doReturn(team).when(teamService).findTeamByIdOrName("id");
            user.getRoles().removeIf(r -> r.getName().equals(AppConstants.RoleName.ROLE_USER));
            // When
            ResponseEntity<TeamWrapperDto> getTeam = teamController.getTeam("id");
            // Then
            Assertions.assertNotNull(getTeam);
            Assertions.assertNotNull(getTeam.getStatusCode());
            Assertions.assertEquals(HttpStatus.OK, getTeam.getStatusCode());
            Assertions.assertNotNull(getTeam.getBody());
            Assertions.assertNotNull(getTeam.getBody().getTeamDto());
            Assertions.assertEquals(team.getName(), getTeam.getBody().getTeamDto().getName());
            Assertions.assertEquals(team.getCountry(), getTeam.getBody().getTeamDto().getCountry());
            Assertions.assertEquals(team.getAvailableCash(), getTeam.getBody().getTeamDto().getAvailableCash());
            Assertions.assertEquals(1, getTeam.getBody().getTeamDto().getPlayers().size());
        }
    }

    @Nested
    class UpdateTeamTest {
        private final TeamUpdateDto teamUpdateDto = new TeamUpdateDto();

        @Test
        void givenInvalidTeam_whenUpdateTeam_thenThrowServerException() throws ServerException {
            // Given
            Team team = TeamFactory.createTeam(null);
            Mockito.doReturn(team).when(teamService).findTeamByIdOrName("id");
            user.getRoles().removeIf(r -> r.getName().equals(AppConstants.RoleName.ROLE_ADMIN));
            // When
            Executable closureToTest = () -> teamController.updateTeam("id", teamUpdateDto, bindingResult);
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        void given_whenUpdateTeam_thenReturn200() throws ServerException {
            // Given
            Team team = TeamFactory.createTeam(null);
            Mockito.doReturn(team).when(teamService).findTeamByIdOrName("id");
            Mockito.doReturn(team).when(teamService).updateTeam("id", teamUpdateDto);
            user.getRoles().removeIf(r -> r.getName().equals(AppConstants.RoleName.ROLE_USER));
            // When
            ResponseEntity<TeamWrapperDto> getTeam = teamController.updateTeam("id", teamUpdateDto, bindingResult);
            // Then
            Assertions.assertNotNull(getTeam);
            Assertions.assertNotNull(getTeam.getStatusCode());
            Assertions.assertEquals(HttpStatus.OK, getTeam.getStatusCode());
            Assertions.assertNotNull(getTeam.getBody());
            Assertions.assertNotNull(getTeam.getBody().getTeamDto());
            Assertions.assertEquals(team.getName(), getTeam.getBody().getTeamDto().getName());
            Assertions.assertEquals(team.getCountry(), getTeam.getBody().getTeamDto().getCountry());
            Assertions.assertEquals(team.getAvailableCash(), getTeam.getBody().getTeamDto().getAvailableCash());
            Assertions.assertEquals(1, getTeam.getBody().getTeamDto().getPlayers().size());
        }
    }

    @Nested
    class DeleteTeamTest {
        @Test
        void givenInvalidTeam_whenDeleteTeam_thenThrowServerException() throws ServerException {
            // Given
            Team team = TeamFactory.createTeam(null);
            Mockito.doReturn(team).when(teamService).findTeamByIdOrName("id");
            user.getRoles().removeIf(r -> r.getName().equals(AppConstants.RoleName.ROLE_ADMIN));
            // When
            Executable closureToTest = () -> teamController.deleteTeam("id");
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        void given_whenDeleteTeam_thenReturn200() throws ServerException {
            // Given
            Team team = TeamFactory.createTeam(null);
            Mockito.doReturn(team).when(teamService).findTeamByIdOrName("id");
            user.getRoles().removeIf(r -> r.getName().equals(AppConstants.RoleName.ROLE_USER));
            // When
            ResponseEntity<Void> getTeam = teamController.deleteTeam("id");
            // Then
            Assertions.assertNotNull(getTeam);
            Assertions.assertNotNull(getTeam.getStatusCode());
            Assertions.assertEquals(HttpStatus.NO_CONTENT, getTeam.getStatusCode());
        }
    }
}
