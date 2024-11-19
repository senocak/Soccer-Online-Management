package com.github.senocak.controller;

import com.github.senocak.TestConstants;
import com.github.senocak.dto.user.UserUpdateDto;
import com.github.senocak.dto.user.UserWrapperResponse;
import com.github.senocak.exception.ServerException;
import com.github.senocak.factory.TeamFactory;
import com.github.senocak.factory.UserFactory;
import com.github.senocak.model.User;
import com.github.senocak.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import java.util.Map;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for UserController")
public class UserControllerTest {
    @InjectMocks UserController userController;

    @Mock UserService userService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock BindingResult bindingResult;

    @Nested
    class GetMeTest {
        @Test
        void given_whenGetMe_thenReturn200() throws ServerException {
            // Given
            User user = UserFactory.createUser(TeamFactory.createTeam(null));
            Mockito.doReturn(user).when(userService).loggedInUser();
            // When
            ResponseEntity<UserWrapperResponse> getMe = userController.getMe();
            // Then
            Assertions.assertNotNull(getMe);
            Assertions.assertNotNull(getMe.getStatusCode());
            Assertions.assertEquals(HttpStatus.OK ,getMe.getStatusCode());
            Assertions.assertNotNull(getMe.getBody());
            Assertions.assertNotNull(getMe.getBody().getUserResponse());
            Assertions.assertEquals(user.getUsername(),
                getMe.getBody().getUserResponse().getUsername());
            Assertions.assertEquals(user.getEmail(),
                getMe.getBody().getUserResponse().getEmail());
            Assertions.assertEquals(user.getName(),
                getMe.getBody().getUserResponse().getName());
            Assertions.assertNull(getMe.getBody().getToken());
        }
    }

    @Nested
    class PatchMeTest {
        private final UserUpdateDto updateUserDto = new UserUpdateDto();

        @Test
        void given_whenPatchMe_thenThrowServerException() throws ServerException {
            // Given
            User user = UserFactory.createUser(TeamFactory.createTeam(null));
            Mockito.doReturn(user).when(userService).loggedInUser();
            updateUserDto.setName(TestConstants.USER_NAME);
            updateUserDto.setPassword("pass1");
            updateUserDto.setPassword_confirmation("pass1");
            // When
            ResponseEntity<Map<String, String>> patchMe = userController.patchMe(updateUserDto, bindingResult);
            // Then
            Assertions.assertNotNull(patchMe);
            Assertions.assertNotNull(patchMe.getStatusCode());
            Assertions.assertEquals(HttpStatus.OK ,patchMe.getStatusCode());
            Assertions.assertNotNull(patchMe.getBody());
            Assertions.assertEquals(1, patchMe.getBody().size());
            Assertions.assertNotNull(patchMe.getBody().get("message"));
            Assertions.assertEquals("User updated.",
                    patchMe.getBody().get("message"));
        }
    }
}