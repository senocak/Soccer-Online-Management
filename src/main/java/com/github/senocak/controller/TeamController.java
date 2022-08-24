package com.github.senocak.controller;

import com.github.senocak.config.SwaggerConfig;
import com.github.senocak.dto.ExceptionDto;
import com.github.senocak.dto.team.TeamCreateDto;
import com.github.senocak.dto.team.TeamUpdateDto;
import com.github.senocak.dto.team.TeamWrapperDto;
import com.github.senocak.exception.ServerException;
import com.github.senocak.model.User;
import com.github.senocak.security.Authorize;
import com.github.senocak.service.TeamService;
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
@RequestMapping(TeamController.URL)
@Tag(name = "Team", description = "Team API")
public class TeamController extends BaseController {
    public static final String URL = "/api/v1/teams";
    private final TeamService teamService;
    private final UserService userService;

    @PostMapping
    @Authorize(roles = {ADMIN})
    @Operation(summary = "Create Team", tags = {"Team"}, responses = {
        @ApiResponse(responseCode = "201", description = "successful operation",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamWrapperDto.class))),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDto.class)))
    }, security = @SecurityRequirement(name = SwaggerConfig.securitySchemeName, scopes = {ADMIN}))
    public ResponseEntity<TeamWrapperDto> create(
        @Parameter(description = "Request body to create", required = true)
            @RequestBody @Validated TeamCreateDto teamCreateDto,
        BindingResult errors
    ) throws ServerException {
        hasErrors(errors);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TeamService.convertEntityToDto(teamService.createTeam(teamCreateDto)));
    }

    @GetMapping
    @Operation(summary = "Get All Team", tags = {"Team"}, responses = {
        @ApiResponse(responseCode = "200", description = "successful operation",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamWrapperDto.class))),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDto.class)))
    })
    public ResponseEntity<TeamWrapperDto> getAll(
        @Parameter(description = "Number of resources that is requested.", required = true)
            @RequestParam(value = "next", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) @Min(0) @Max(99) final int nextPage,
        @Parameter(description = "Pointer for the next page to retrieve.", required = true)
            @RequestParam(value = "max", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) @Min(0) @Max(99) final int maxNumber
    ) {
        return ResponseEntity.ok(teamService.getAll(nextPage, maxNumber));
    }

    @GetMapping(value = "/{idOrName}")
    @Operation(summary = "Get Single Team", tags = {"Team"}, responses = {
        @ApiResponse(responseCode = "200", description = "successful operation",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamWrapperDto.class))),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDto.class)))
    })
    public ResponseEntity<TeamWrapperDto> getTeam(
        @Parameter(description = "Team identifier", required = true) @PathVariable String idOrName
    ) throws ServerException {
        checkTeamBelongToUser(idOrName);
        return ResponseEntity.ok(TeamService.convertEntityToDto(teamService.findTeamByIdOrName(idOrName)));
    }

    @PatchMapping("/{idOrName}")
    @Authorize(roles = {ADMIN, USER})
    @Operation(summary = "Update Team", tags = {"Team"}, responses = {
        @ApiResponse(responseCode = "200", description = "successful operation",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamWrapperDto.class))),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDto.class)))
    }, security = @SecurityRequirement(name = SwaggerConfig.securitySchemeName, scopes = {ADMIN, USER}))
    public ResponseEntity<TeamWrapperDto> updateTeam(
        @Parameter(description = "Identifier of the team", required = true) @PathVariable String idOrName,
        @Parameter(description = "Request body to update the eam", required = true)
            @RequestBody @Validated TeamUpdateDto teamUpdateDto,
        BindingResult errors
    ) throws ServerException {
        hasErrors(errors);
        checkTeamBelongToUser(idOrName);
        return ResponseEntity.ok(TeamService.convertEntityToDto(teamService.updateTeam(idOrName, teamUpdateDto)));
    }

    @DeleteMapping(value = "/{idOrName}")
    @Authorize(roles = {ADMIN, USER})
    @Operation(summary = "Delete Team", tags = {"Team"}, responses = {
        @ApiResponse(responseCode = "204", description = "successful operation"),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDto.class)))
    }, security = @SecurityRequirement(name = SwaggerConfig.securitySchemeName, scopes = {ADMIN, USER}))
    public ResponseEntity<Void> deleteTeam(
        @Parameter(description = "Identifier of the team", required = true) @PathVariable String idOrName
    ) throws ServerException {
        checkTeamBelongToUser(idOrName);
        teamService.deleteTeam(idOrName);
        return ResponseEntity.noContent().build();
    }

    /**
     * @param idOrName -- id or slug of the team
     * @throws ServerException -- throws ServerException
     */
    private void checkTeamBelongToUser(String idOrName) throws ServerException {
        User getUserFromContext = getUserFromContext();
        TeamWrapperDto teamByIdOrName = TeamService.convertEntityToDto(teamService.findTeamByIdOrName(idOrName));
        if (getUserFromContext.getRoles().stream().noneMatch(role -> role.getName().equals(AppConstants.RoleName.ROLE_ADMIN)) &&
                (teamByIdOrName.getTeamDto().getUser() == null ||
                !teamByIdOrName.getTeamDto().getUser().getEmail().equalsIgnoreCase(getUserFromContext.getEmail()))
        ) {
            throw new ServerException(AppConstants.OmaErrorMessageType.UNAUTHORIZED,
                    new String[]{"This user does not have the right permission for this operation"},
                    HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * @return -- User object retrieved from security context
     * @throws ServerException -- if user not found in context based on jwt token
     */
    private User getUserFromContext() throws ServerException {
        return userService.loggedInUser();
    }
}
