package com.github.senocak.service;

import com.github.senocak.dto.team.TeamCreateDto;
import com.github.senocak.dto.team.TeamDto;
import com.github.senocak.dto.team.TeamUpdateDto;
import com.github.senocak.dto.team.TeamWrapperDto;
import com.github.senocak.exception.ServerException;
import com.github.senocak.model.Team;
import com.github.senocak.repository.TeamRepository;
import com.github.senocak.util.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final UserService userService;

    /**
     * Create new Team
     * @param teamCreateDto -- TeamCreateDto object to be created
     * @return -- Team entity that is created
     * @throws ServerException -- ServerException if Team is already exist
     */
    public Team createTeam(TeamCreateDto teamCreateDto) throws ServerException {
        TeamWrapperDto team = null;
        try{
            team = convertEntityToDto(findTeamByIdOrName(teamCreateDto.getName()));
        }catch (ServerException serverException){
            log.debug("Caught ServerException, Team not exist in db");
        }
        if (Objects.nonNull(team)){
            String error = "Team " + team.getTeamDto().getName() + " already exist";
            log.error(error);
            throw new ServerException(AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT,
                    new String[]{error}, HttpStatus.CONFLICT);
        }
        Team build = Team.builder()
                .name(teamCreateDto.getName())
                .country(teamCreateDto.getCountry())
                .availableCash(teamCreateDto.getAvailableCash())
                .build();
        if (teamCreateDto.getUsername() != null)
            build.setUser(userService.findByUsername(teamCreateDto.getUsername()));
        return saveTeam(build);
    }

    /**
     * @param team -- Team entity
     * @return -- Team entity that is retrieved from db
     * @throws ServerException -- if Team is not found
     */
    public static TeamWrapperDto convertEntityToDto(Team team) throws ServerException {
        TeamDto teamDto = DtoConverter.convertEntityToDto(team, true);
        if (teamDto.getUser() != null)
            teamDto.getUser().setTeamDto(null);
        return TeamWrapperDto.builder().teamDto(teamDto).build();
    }

    /**
     * @param idOrName -- id or name of Team entity
     * @return -- Team entity that is retrieved from db
     * @throws ServerException -- if Team is not found
     */
    public Team findTeamByIdOrName(String idOrName) throws ServerException {
        Optional<Team> teamOptional = teamRepository.findByIdOrName(idOrName);
        if (Objects.isNull(teamOptional) || !teamOptional.isPresent()) {
            log.error("Team is not found.");
            throw new ServerException(AppConstants.OmaErrorMessageType.NOT_FOUND,
                    new String[]{"Team: " + idOrName}, HttpStatus.NOT_FOUND);
        }
        return teamOptional.get();
    }

    /**
     * @param nextPage -- next page variable to filter
     * @param maxNumber -- max page to retrieve from db
     * @return -- player objects that has retrieved by page
     */
    public TeamWrapperDto getAll(int nextPage, int maxNumber) {
        Pageable paging = PageRequest.of(nextPage, maxNumber);
        Page<Team> all = teamRepository.findAll(paging);
        List<TeamDto> teamDtos = all.stream().map(t -> {
            TeamDto teamDto = DtoConverter.convertEntityToDto(t, true);
            if (teamDto.getUser() != null) {
                teamDto.getUser().setRoles(null);
                teamDto.getUser().setTeamDto(null);
            }
            return teamDto;
        }).collect(Collectors.toList());
        return TeamWrapperDto.builder()
                .teamsDto(teamDtos)
                .next((long) (all.hasNext() ? nextPage + 1 : 0))
                .total(all.getTotalElements())
                .build();
    }

    /**
     * @param idOrName -- id or name of Team entity
     */
    public void deleteTeam(String idOrName) throws ServerException {
        teamRepository.delete(findTeamByIdOrName(idOrName));
    }

    /**
     * @param idOrName -- id or name of Team entity to be updated
     * @return -- Persisted Team entity
     */
    public Team updateTeam(String idOrName, TeamUpdateDto teamUpdateDto) throws ServerException {
        Team team = findTeamByIdOrName(idOrName);
        String teamName = teamUpdateDto.getName();
        if (Objects.nonNull(teamName) && !teamName.isEmpty())
            team.setName(teamName);
        String country = teamUpdateDto.getCountry();
        if (Objects.nonNull(country) && !country.isEmpty())
            team.setCountry(country);
        return saveTeam(team);
    }

    /**
     * Update Team
     * @param team -- Team entity to be wanted to update
     * @return -- Updated Team entity
     */
    private Team saveTeam(Team team) {
        return teamRepository.save(team);
    }
}
