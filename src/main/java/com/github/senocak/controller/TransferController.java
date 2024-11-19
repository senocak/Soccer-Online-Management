package com.github.senocak.controller;

import com.github.senocak.config.SwaggerConfig;
import com.github.senocak.dto.ExceptionDto;
import com.github.senocak.dto.transfer.TransferCreateDto;
import com.github.senocak.dto.transfer.TransferUpdateDto;
import com.github.senocak.dto.transfer.TransferWrapperDto;
import com.github.senocak.exception.ServerException;
import com.github.senocak.model.Player;
import com.github.senocak.model.Transfer;
import com.github.senocak.model.User;
import com.github.senocak.security.Authorize;
import com.github.senocak.service.PlayerService;
import com.github.senocak.service.TransferService;
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
@RequestMapping(TransferController.URL)
@Tag(name = "Transfer", description = "Transfer API")
public class TransferController extends BaseController {
    public static final String URL = "/api/v1/transfers";
    private final TransferService transferService;
    private final PlayerService playerService;
    private final UserService userService;

    @PostMapping
    @Authorize(roles = {ADMIN, USER})
    @Operation(summary = "Create Transfer", tags = {"Transfer"}, responses = {
        @ApiResponse(responseCode = "201", description = "successful operation",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransferWrapperDto.class))),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDto.class)))
    }, security = @SecurityRequirement(name = SwaggerConfig.securitySchemeName, scopes = {USER}))
    public ResponseEntity<TransferWrapperDto> create(
        @Parameter(description = "Request body to create", required = true)
            @RequestBody @Validated TransferCreateDto transferCreateDto,
        BindingResult errors
    ) throws ServerException {
        hasErrors(errors);
        checkPlayerBelongToUser(transferCreateDto.getPlayerId().toString());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TransferService.convertEntityToDto(transferService.createTransfer(transferCreateDto)));
    }

    @GetMapping
    @Operation(summary = "Get All Transfer", tags = {"Transfer"}, responses = {
        @ApiResponse(responseCode = "200", description = "successful operation",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransferWrapperDto.class))),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDto.class)))
    })
    public ResponseEntity<TransferWrapperDto> getAll(
        @Parameter(description = "Number of resources that is requested.", required = true)
            @RequestParam(value = "next", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) @Min(0) @Max(99) final int nextPage,
        @Parameter(description = "Pointer for the next page to retrieve.", required = true)
            @RequestParam(value = "max", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) @Min(0) @Max(99) final int maxNumber
    ) {
        return ResponseEntity.ok(transferService.getAll(nextPage, maxNumber));
    }

    @GetMapping(value = "/{id}")
    @Operation(summary = "Get Single Transfer", tags = {"Transfer"}, responses = {
        @ApiResponse(responseCode = "200", description = "successful operation",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransferWrapperDto.class))),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDto.class)))
    })
    public ResponseEntity<TransferWrapperDto> getTransfer(
        @Parameter(description = "Transfer identifier", required = true) @PathVariable String id
    ) throws ServerException {
        return ResponseEntity.ok(TransferService.convertEntityToDto(transferService.findById(id)));
    }

    @PatchMapping("/{id}")
    @Authorize(roles = {ADMIN, USER})
    @Operation(summary = "Update Transfer", tags = {"Transfer"}, responses = {
        @ApiResponse(responseCode = "200", description = "successful operation",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransferWrapperDto.class))),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDto.class)))
    }, security = @SecurityRequirement(name = SwaggerConfig.securitySchemeName, scopes = {ADMIN, USER}))
    public ResponseEntity<TransferWrapperDto> updateTransfer(
        @Parameter(description = "Identifier of the transfer", required = true) @PathVariable String id,
        @Parameter(description = "Request body to create", required = true)
            @RequestBody @Validated TransferUpdateDto transferUpdateDto,
        BindingResult errors
    ) throws ServerException {
        hasErrors(errors);
        return ResponseEntity.ok(TransferService.convertEntityToDto(
                transferService.updateTransfer(id, transferUpdateDto)));
    }

    @PatchMapping("/{id}/confirm")
    @Authorize(roles = {ADMIN, USER})
    @Operation(summary = "Confirm Transfer", tags = {"Transfer"}, responses = {
        @ApiResponse(responseCode = "200", description = "successful operation",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransferWrapperDto.class))),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDto.class)))
    }, security = @SecurityRequirement(name = SwaggerConfig.securitySchemeName, scopes = {ADMIN, USER}))
    public ResponseEntity<TransferWrapperDto> confirmTransfer(
        @Parameter(description = "Identifier of the transfer", required = true) @PathVariable String id
    ) throws ServerException {
        return ResponseEntity.ok(TransferService.convertEntityToDto(transferService.confirmTransfer(id)));
    }

    @DeleteMapping(value = "/{id}")
    @Authorize(roles = {ADMIN, USER})
    @Operation(summary = "Delete Transfer", tags = {"Transfer"}, responses = {
        @ApiResponse(responseCode = "204", description = "successful operation"),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDto.class)))
    }, security = @SecurityRequirement(name = SwaggerConfig.securitySchemeName, scopes = {ADMIN, USER}))
    public ResponseEntity<Void> deleteTransfer(
        @Parameter(description = "Identifier of the transfer", required = true) @PathVariable String id
    ) throws ServerException {
        Transfer transferById = transferService.findById(id);
        checkPlayerBelongToUser(transferById.getPlayer().getId());
        transferService.deleteTransfer(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Checks if the player belongs to the user.
     * @param id    Identifier of the player.
     * @throws ServerException  If the player does not belong to the user.
     */
    private void checkPlayerBelongToUser(String id) throws ServerException {
        User loggedInUser = userService.loggedInUser();
        Player player = playerService.findPlayerById(id);
        if (loggedInUser.getRoles().stream().noneMatch(role -> role.getName().equals(AppConstants.RoleName.ROLE_ADMIN)) &&
                (player.getTeam().getUser() == null ||
                !player.getTeam().getUser().getEmail().equalsIgnoreCase(loggedInUser.getEmail()))
        ) {
            throw new ServerException(AppConstants.OmaErrorMessageType.UNAUTHORIZED,
                    new String[]{"This user does not have the right permission for transfer operation"},
                    HttpStatus.UNAUTHORIZED);
        }
    }
}
