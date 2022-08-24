package com.github.senocak.controller;

import com.github.senocak.config.SwaggerConfig;
import com.github.senocak.dto.ExceptionDto;
import com.github.senocak.dto.team.TeamDto;
import com.github.senocak.dto.user.UserResponse;
import com.github.senocak.dto.user.UserWrapperResponse;
import com.github.senocak.dto.user.UserUpdateDto;
import com.github.senocak.security.Authorize;
import com.github.senocak.service.DtoConverter;
import com.github.senocak.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import com.github.senocak.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.github.senocak.exception.ServerException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import static com.github.senocak.util.AppConstants.ADMIN;
import static com.github.senocak.util.AppConstants.USER;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(UserController.URL)
@Tag(name = "User", description = "User Controller")
public class UserController extends BaseController {
    public static final String URL = "/api/v1/user";
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/me")
    @Authorize(roles = {ADMIN, USER})
    @Operation(summary = "Get me", tags = {"User"}, responses = {
        @ApiResponse(responseCode = "200", description = "successful operation",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserWrapperResponse.class))),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDto.class)))
    }, security = @SecurityRequirement(name = SwaggerConfig.securitySchemeName, scopes = {ADMIN, USER}))
    public ResponseEntity<UserWrapperResponse> getMe() throws ServerException {
        UserWrapperResponse userWrapperResponse = new UserWrapperResponse();
        User user = userService.loggedInUser();
        TeamDto teamDto = DtoConverter.convertEntityToDto(user.getTeam(), false);
        UserResponse userResponse = DtoConverter.convertEntityToDto(user);
        teamDto.setUser(null);
        teamDto.setPlayers(
            user.getTeam().getPlayers().stream()
                    .map(t -> DtoConverter.convertEntityToDto(t, false))
                    .collect(Collectors.toList())
        );
        userResponse.setTeamDto(teamDto);
        userWrapperResponse.setUserResponse(userResponse);
        return ResponseEntity.ok(userWrapperResponse);
    }

    @PatchMapping("/me")
    @Authorize(roles = {ADMIN, USER})
    @Operation(summary = "Update user by username", tags = {"User"}, responses = {
        @ApiResponse(responseCode = "200", description = "successful operation",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = HashMap.class))),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDto.class)))
    }, security = @SecurityRequirement(name = SwaggerConfig.securitySchemeName, scopes = {ADMIN, USER}))
    public ResponseEntity<Map<String, String>> patchMe(
        @Parameter(description = "Request body to update", required = true)
            @RequestBody @Validated UserUpdateDto userDto,
        BindingResult errors
    ) throws ServerException {
        hasErrors(errors);
        User user = userService.loggedInUser();
        String name = userDto.getName();
        if (Objects.nonNull(name) && !name.isEmpty())
            user.setName(name);
        String password = userDto.getPassword();
        String password_confirmation = userDto.getPassword_confirmation();
        if (Objects.nonNull(password) && !password.isEmpty() &&
                Objects.nonNull(password_confirmation) && !password_confirmation.isEmpty()){
            user.setPassword(passwordEncoder.encode(password));
        }
        userService.save(user);
        Map<String, String> response = new HashMap<>();
        response.put("message", "User updated.");
        return ResponseEntity.ok(response);
    }
}
