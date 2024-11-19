package com.github.senocak.controller;

import com.github.senocak.config.SwaggerConfig;
import com.github.senocak.dto.ExceptionDto;
import com.github.senocak.dto.player.PlayerCreateDto;
import com.github.senocak.dto.player.PlayerPatchDto;
import com.github.senocak.dto.player.PlayerPutDto;
import com.github.senocak.dto.player.PlayerWrapperDto;
import com.github.senocak.exception.ServerException;
import com.github.senocak.model.Player;
import com.github.senocak.model.User;
import com.github.senocak.security.Authorize;
import com.github.senocak.service.PlayerService;
import com.github.senocak.service.UserService;
import com.github.senocak.util.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import static com.github.senocak.util.AppConstants.ADMIN;
import static com.github.senocak.util.AppConstants.USER;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(PlayerController.URL)
@Tag(name = "Player", description = "Player API")
public class PlayerController extends BaseController {
    public static final String URL = "/api/v1/players";
    private final PlayerService playerService;
    private final UserService userService;

    @PostMapping
    @Authorize(roles = {ADMIN})
    @Operation(summary = "Create Player", tags = {"Player"}, responses = {
        @ApiResponse(responseCode = "201", description = "successful operation",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlayerWrapperDto.class))),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDto.class)))
    }, security = @SecurityRequirement(name = SwaggerConfig.securitySchemeName, scopes = {ADMIN}))
    public ResponseEntity<PlayerWrapperDto> create(
        @Parameter(description = "Request body to create", required = true)
            @RequestBody @Validated PlayerCreateDto playerCreateDto,
        BindingResult errors
    ) throws ServerException {
        hasErrors(errors);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PlayerService.convertEntityToDto(playerService.createPlayer(playerCreateDto)));
    }

    @GetMapping
    @Operation(summary = "Get All Players", tags = {"Player"}, responses = {
        @ApiResponse(responseCode = "200", description = "successful operation",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlayerWrapperDto.class))),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDto.class)))
    })
    public ResponseEntity<PlayerWrapperDto> getAll(
        @Parameter(description = "Number of resources that is requested.", required = true)
            @RequestParam(value = "next", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) @Min(0) @Max(99) final int nextPage,
        @Parameter(description = "Pointer for the next page to retrieve.", required = true)
            @RequestParam(value = "max", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) @Min(0) @Max(99) final int maxNumber
    ) {
        return ResponseEntity.ok(playerService.getAll(nextPage, maxNumber));
    }

    @GetMapping(value = "/{id}")
    @Operation(summary = "Get Single Player", tags = {"Player"}, responses = {
        @ApiResponse(responseCode = "200", description = "successful operation",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlayerWrapperDto.class))),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDto.class)))
    })
    public ResponseEntity<PlayerWrapperDto> getPlayer(
        @Parameter(description = "Player identifier", required = true) @PathVariable String id
    ) throws ServerException {
        checkPlayerBelongToUser(id);
        return ResponseEntity.ok(PlayerService.convertEntityToDto(playerService.findPlayerById(id)));
    }

    @PutMapping("/{id}")
    @Authorize(roles = {ADMIN, USER})
    @Operation(summary = "Update Player", tags = {"Player"}, responses = {
        @ApiResponse(responseCode = "200", description = "successful operation",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlayerWrapperDto.class))),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDto.class)))
    }, security = @SecurityRequirement(name = SwaggerConfig.securitySchemeName, scopes = {ADMIN, USER}))
    public ResponseEntity<PlayerWrapperDto> updatePlayer(
        @Parameter(description = "Identifier of the player", required = true) @PathVariable String id,
        @Parameter(description = "Request body to update the player", required = true)
            @RequestBody @Validated PlayerPutDto playerPutDto,
        BindingResult errors
    ) throws ServerException {
        hasErrors(errors);
        checkPlayerBelongToUser(id);
        return ResponseEntity.ok(PlayerService.convertEntityToDto(playerService.updatePlayer(id, playerPutDto)));
    }

    @PatchMapping("/{id}")
    @Authorize(roles = {ADMIN, USER})
    @Operation(summary = "Update Player", tags = {"Player"}, responses = {
        @ApiResponse(responseCode = "200", description = "successful operation",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlayerWrapperDto.class))),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDto.class)))
    }, security = @SecurityRequirement(name = SwaggerConfig.securitySchemeName, scopes = {ADMIN, USER}))
    public ResponseEntity<PlayerWrapperDto> updatePartiallyPlayer(
        @Parameter(description = "Identifier of the player", required = true)
            @PathVariable String id,
        @Parameter(description = "Request body to update the player", required = true)
            @RequestBody @Validated PlayerPatchDto playerPatchDto,
        BindingResult errors
    ) throws ServerException {
        hasErrors(errors);
        checkPlayerBelongToUser(id);
        return ResponseEntity.ok(PlayerService.convertEntityToDto(playerService.updatePartiallyPlayer(id, playerPatchDto)));
    }

    @DeleteMapping(value = "/{id}")
    @Authorize(roles = {ADMIN, USER})
    @Operation(summary = "Delete Player", tags = {"Player"}, responses = {
        @ApiResponse(responseCode = "204", description = "successful operation"),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDto.class)))
    }, security = @SecurityRequirement(name = SwaggerConfig.securitySchemeName, scopes = {ADMIN, USER}))
    public ResponseEntity<Void> deletePlayer(
        @Parameter(description = "Identifier of the player", required = true) @PathVariable String id
    ) throws ServerException {
        checkPlayerBelongToUser(id);
        playerService.deletePlayer(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * @param id -- Identifier of the player
     * @throws ServerException -- throws ServerException
     */
    private void checkPlayerBelongToUser(String id) throws ServerException {
        User loggedInUser = userService.loggedInUser();
        Player player = playerService.findPlayerById(id);
        Boolean aBoolean = loggedInUser.getRoles().stream().map(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER)).findFirst().orElse(false);
        if (aBoolean &&
                (player.getTeam() == null || player.getTeam().getUser() == null ||
                    !player.getTeam().getUser().getEmail().equalsIgnoreCase(loggedInUser.getEmail()))) {
            throw new ServerException(AppConstants.OmaErrorMessageType.UNAUTHORIZED,
                    new String[]{"Admin or user that has the provided user have the right permission for this operation"},
                    HttpStatus.UNAUTHORIZED);
        }
    }
}
