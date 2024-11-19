package com.github.senocak.controller;

import com.github.senocak.dto.ExceptionDto;
import com.github.senocak.dto.auth.LoginRequest;
import com.github.senocak.dto.auth.RegisterRequest;
import com.github.senocak.dto.user.UserResponse;
import com.github.senocak.dto.user.UserWrapperResponse;
import com.github.senocak.exception.ServerException;
import com.github.senocak.model.Role;
import com.github.senocak.model.Team;
import com.github.senocak.model.User;
import com.github.senocak.repository.TeamRepository;
import com.github.senocak.repository.UserRepository;
import com.github.senocak.security.JwtTokenProvider;
import com.github.senocak.service.DtoConverter;
import com.github.senocak.service.RoleService;
import com.github.senocak.service.UserService;
import com.github.senocak.util.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Collections;
import java.util.Objects;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(AuthController.URL)
@Tag(name = "Authentication", description = "AUTH API")
public class AuthController extends BaseController {
    public static final String URL = "/api/v1/auth";
    private final UserService userService;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final RoleService roleService;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    @Operation(summary = "Login Endpoint", tags = {"Authentication"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "successful operation",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserWrapperResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad credentials",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDto.class))),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDto.class)))
    })
    public ResponseEntity<UserWrapperResponse> login(
        @Parameter(description = "Request body to login", required = true) @RequestBody @Validated LoginRequest loginRequest,
        BindingResult errors
    ) throws ServerException {
        hasErrors(errors);
        return ResponseEntity.ok(generateUserWrapperResponse(login(loginRequest)));
    }

    @PostMapping("/register")
    @Operation(summary = "Register Endpoint", tags = {"Authentication"}, responses = {
        @ApiResponse(responseCode = "200", description = "successful operation",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserWrapperResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad credentials",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDto.class))),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDto.class)))
    })
    public ResponseEntity<UserWrapperResponse> register(
        @Parameter(description = "Request body to register", required = true)
            @RequestBody @Validated RegisterRequest signUpRequest,
        BindingResult errors
    ) throws ServerException {
        hasErrors(errors);
        if (userService.existsByUsername(signUpRequest.getUsername())) {
            log.error("Username:{} is already taken!", signUpRequest.getUsername());
            throw new ServerException(AppConstants.OmaErrorMessageType.JSON_SCHEMA_VALIDATOR,
                    new String[]{"Username is already taken!"}, HttpStatus.BAD_REQUEST);
        }
        if (userService.existsByEmail(signUpRequest.getEmail())) {
            log.error("Email Address:{} is already taken!", signUpRequest.getEmail());
            throw new ServerException(AppConstants.OmaErrorMessageType.JSON_SCHEMA_VALIDATOR,
                    new String[]{"Email Address already in use!"}, HttpStatus.BAD_REQUEST);
        }
        Role userRole = roleService.findByName(AppConstants.RoleName.ROLE_USER);
        if (Objects.isNull(userRole)){
            log.error("User Role is not found");
                throw new ServerException(AppConstants.OmaErrorMessageType.MANDATORY_INPUT_MISSING,
                    new String[]{"User Role is not found"}, HttpStatus.BAD_REQUEST);
        }
        User user = User.builder()
                .name(signUpRequest.getName())
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .roles(Collections.singleton(userRole))
                .build();
        Team team = Team.builder()
                .name("Default Team")
                .country("Turkey")
                .players(null)
                .user(user)
                .build();
        user.setTeam(team);
        User result = userService.save(user);
        log.info("User created. User: {}", result);
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(signUpRequest.getUsername());
        loginRequest.setPassword(signUpRequest.getPassword());
        UserWrapperResponse object;
        try{
            object = generateUserWrapperResponse(login(loginRequest));
        }catch (Exception e){
            throw new ServerException(AppConstants.OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                    new String[]{"Error occured for generating jwt attempt", HttpStatus.INTERNAL_SERVER_ERROR.toString()},
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(object);
    }

    /**
     * Generate user wrapper response.
     * @param loginRequest the login request
     * @return  the user wrapper response
     */
    private UserResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = userService.findByUsername(loginRequest.getUsername());
        return DtoConverter.convertEntityToDto(user);
    }

    /**
     * Generate UserWrapperResponse with given UserResponse
     * @param userResponse -- UserResponse that contains user data
     * @return UserWrapperResponse
     */
    private UserWrapperResponse generateUserWrapperResponse(UserResponse userResponse) {
        UserWrapperResponse userWrapperResponse = new UserWrapperResponse();
        userWrapperResponse.setUserResponse(userResponse);
        String generatedToken = tokenProvider.generateJwtToken(userResponse.getUsername());
        log.debug("Token is generated. Token: {}", generatedToken);
        userWrapperResponse.setToken(generatedToken);
        log.info("UserWrapperResponse is generated. UserWrapperResponse: {}", userWrapperResponse);
        return userWrapperResponse;
    }
}
