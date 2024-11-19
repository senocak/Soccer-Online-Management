package com.github.senocak.service;

import com.github.senocak.dto.player.PlayerCreateDto;
import com.github.senocak.dto.player.PlayerDto;
import com.github.senocak.dto.player.PlayerPatchDto;
import com.github.senocak.dto.player.PlayerPutDto;
import com.github.senocak.dto.player.PlayerWrapperDto;
import com.github.senocak.exception.ServerException;
import com.github.senocak.model.Player;
import com.github.senocak.model.Team;
import com.github.senocak.model.User;
import com.github.senocak.repository.PlayerRepository;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final TeamService teamService;
    private final UserService userService;

    /**
     * Create new Player
     * @param playerCreateDto -- PlayerCreateDto object to be created
     * @return -- Player that is created
     * @throws ServerException -- ServerException if Player is already exist
     */
    public Player createPlayer(PlayerCreateDto playerCreateDto) throws ServerException {
        PlayerWrapperDto playerWrapperDto = null;
        try{
            Player playerByFirstNameAndLastName = findPlayerByFirstNameAndLastName(playerCreateDto.getFirstName(),
                    playerCreateDto.getLastName());
            playerWrapperDto = convertEntityToDto(playerByFirstNameAndLastName);
        }catch (ServerException serverException){
            log.debug("Caught ServerException, Player not exist in db");
        }
        if (Objects.nonNull(playerWrapperDto)){
            String error = "Player " + playerWrapperDto.getPlayerDto().getFirstName() + " already exist";
            log.error(error);
            throw new ServerException(AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT,
                    new String[]{error}, HttpStatus.CONFLICT);
        }
        Player build = Player.builder()
                .firstName(playerCreateDto.getFirstName())
                .lastName(playerCreateDto.getLastName())
                .country(playerCreateDto.getCountry())
                .age(playerCreateDto.getAge())
                .build();
        if (Objects.nonNull(playerCreateDto.getPosition())) {
            try {
                build.setPosition(AppConstants.PlayerPosition.valueOf(playerCreateDto.getPosition()));
            }catch (IllegalArgumentException e){
                log.error("Invalid position");
                throw new ServerException(AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT,
                        new String[]{"Invalid position:" + playerCreateDto.getPosition()}, HttpStatus.BAD_REQUEST);
            }
        }
        if (Objects.nonNull(playerCreateDto.getMarketValue())) {
            build.setMarketValue(playerCreateDto.getMarketValue());
        }
        if (Objects.nonNull(playerCreateDto.getTeamId())) {
            Team teamByIdOrName = teamService.findTeamByIdOrName(playerCreateDto.getTeamId().toString());
            build.setTeam(teamByIdOrName);
        }
        return savePlayer(build);
    }

    /**
     * @param player -- Player object to be updated
     * @return -- Player that is updated
     */
    public static PlayerWrapperDto convertEntityToDto(Player player)  {
        PlayerDto playerDto = DtoConverter.convertEntityToDto(player, player.getTeam() != null);
        if (playerDto.getTeamDto() != null){
            playerDto.getTeamDto().setUser(null);
            playerDto.getTeamDto().setPlayers(null);
        }
        return PlayerWrapperDto.builder().playerDto(playerDto).build();
    }

    /**
     * @param id -- id of the player to be found
     * @return -- Player object that is found
     * @throws ServerException  -- if Player is not found
     */
    public Player findPlayerById(String id) throws ServerException {
        Optional<Player> optionalPlayer = playerRepository.findPlayerById(id);
        if (Objects.isNull(optionalPlayer) || !optionalPlayer.isPresent()) {
            log.error("Player is not found.");
            throw new ServerException(AppConstants.OmaErrorMessageType.NOT_FOUND,
                    new String[]{"Player: " + id}, HttpStatus.NOT_FOUND);
        }
        return optionalPlayer.get();
    }

    /**
     * @param firstName -- first name of Player entity
     * @param lastName  -- last name of Player entity
     * @return  -- Player entity that is retrieved from db
     * @throws ServerException  -- if Player is not found
     */
    public Player findPlayerByFirstNameAndLastName(String firstName, String lastName) throws ServerException {
        Optional<Player> teamOptional = playerRepository.findPlayerByFirstNameAndLastName(firstName, lastName);
        if (Objects.isNull(teamOptional) || !teamOptional.isPresent()) {
            log.error("Player is not found.");
            throw new ServerException(AppConstants.OmaErrorMessageType.NOT_FOUND,
                    new String[]{"Player: " + firstName + " " + lastName}, HttpStatus.NOT_FOUND);
        }
        return teamOptional.get();
    }

    /**
     * @param nextPage -- next page variable to filter
     * @param maxNumber -- max page to retrieve from db
     * @return -- player objects that has retrieved by page
     */
    public PlayerWrapperDto getAll(int nextPage, int maxNumber) {
        Pageable paging = PageRequest.of(nextPage, maxNumber);
        Page<Player> all = playerRepository.findAll(paging);
        List<PlayerDto> playerDtos = all.stream().map(t -> {
            PlayerDto playerDto = DtoConverter.convertEntityToDto(t, t.getTeam() != null);
            if (playerDto.getTeamDto() != null) {
                playerDto.getTeamDto().setUser(null);
                playerDto.getTeamDto().setPlayers(null);
            }
            return playerDto;
        }).collect(Collectors.toList());
        return PlayerWrapperDto.builder()
                .playerDtos(playerDtos)
                .next((long) (all.hasNext() ? nextPage + 1 : 0))
                .total(all.getTotalElements())
                .build();
    }

    /**
     * @param id -- id of the player to be updated
     * @throws ServerException  -- if Player is not found
     */
    public void deletePlayer(String id) throws ServerException {
        playerRepository.delete(findPlayerById(id));
    }

    /**
     * @param id -- id of the Player entity to be updated
     * @return -- Persisted Player entity
     */
    public Player updatePlayer(String id, PlayerPutDto playerPutDto) throws ServerException {
        Player player = findPlayerById(id);
        player.setFirstName(playerPutDto.getFirstName());
        player.setLastName(playerPutDto.getLastName());
        player.setCountry(playerPutDto.getCountry());
        player.setAge(playerPutDto.getAge());
        String position = playerPutDto.getPosition();
        try {
            player.setPosition(AppConstants.PlayerPosition.valueOf(position));
        } catch (IllegalArgumentException e) {
            log.error("Invalid position");
            throw new ServerException(AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT,
                    new String[]{"Invalid position:" + position}, HttpStatus.BAD_REQUEST);
        }
        player.setMarketValue(playerPutDto.getMarketValue());

        User loggedInUser = userService.loggedInUser();
        Optional<Boolean> isAdmin = loggedInUser.getRoles().stream().map(role -> role.getName().equals(AppConstants.RoleName.ROLE_ADMIN)).findFirst();
        if (isAdmin.isPresent() && isAdmin.get()) {
            log.debug("Only admin can update the team of a player");
            Team newTeam = teamService.findTeamByIdOrName(playerPutDto.getTeamId().toString());
            if (player.getTeam() != null && !player.getTeam().getId().equals(newTeam.getId())) {
                player.setTeam(newTeam);
            }
        }
        return savePlayer(player);
    }

    /**
     * @param id -- id of the Player entity to be updated
     * @return -- Persisted Player entity
     */
    public Player updatePartiallyPlayer(String id, PlayerPatchDto playerPutDto) throws ServerException {
        Player player = findPlayerById(id);
        String firstName = playerPutDto.getFirstName();
        if (Objects.nonNull(firstName) && !firstName.isEmpty())
            player.setFirstName(firstName);

        String lastName = playerPutDto.getLastName();
        if (Objects.nonNull(lastName) && !lastName.isEmpty())
            player.setLastName(lastName);

        String country = playerPutDto.getCountry();
        if (Objects.nonNull(country) && !country.isEmpty())
            player.setCountry(country);

        Integer age = playerPutDto.getAge();
        if (Objects.nonNull(age))
            player.setAge(age);

        String position = playerPutDto.getPosition();
        if (Objects.nonNull(position) && !position.isEmpty()){
            try {
                player.setPosition(AppConstants.PlayerPosition.valueOf(position));
            } catch (IllegalArgumentException e) {
                log.error("Invalid position");
                throw new ServerException(AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT,
                        new String[]{"Invalid position:" + position}, HttpStatus.BAD_REQUEST);
            }
        }

        Integer marketValue = playerPutDto.getMarketValue();
        if (Objects.nonNull(marketValue))
            player.setMarketValue(marketValue);

        UUID teamUUID = playerPutDto.getTeamId();
        if (Objects.nonNull(teamUUID)) {
            User loggedInUser = userService.loggedInUser();
            Optional<Boolean> isAdmin = loggedInUser.getRoles().stream().map(role -> role.getName().equals(AppConstants.RoleName.ROLE_ADMIN)).findFirst();
            if (isAdmin.isPresent() && isAdmin.get()) {
                Team newTeam = teamService.findTeamByIdOrName(teamUUID.toString());
                if (player.getTeam() != null && !player.getTeam().getId().equals(newTeam.getId())) {
                    player.setTeam(newTeam);
                }
            }
        }
        return savePlayer(player);
    }

    /**
     * @param player -- Player entity to be saved
     * @return -- Persisted Player entity
     */
    private Player savePlayer(Player player) {
        return playerRepository.save(player);
    }
}
