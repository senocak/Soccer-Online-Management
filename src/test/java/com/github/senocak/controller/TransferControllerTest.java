package com.github.senocak.controller;

import com.github.senocak.dto.transfer.TransferCreateDto;
import com.github.senocak.dto.transfer.TransferUpdateDto;
import com.github.senocak.dto.transfer.TransferWrapperDto;
import com.github.senocak.exception.ServerException;
import com.github.senocak.factory.PlayerFactory;
import com.github.senocak.factory.TeamFactory;
import com.github.senocak.factory.TransferFactory;
import com.github.senocak.factory.UserFactory;
import com.github.senocak.model.Player;
import com.github.senocak.model.Transfer;
import com.github.senocak.model.User;
import com.github.senocak.service.DtoConverter;
import com.github.senocak.service.PlayerService;
import com.github.senocak.service.TransferService;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for TransferController")
public class TransferControllerTest {
    @InjectMocks TransferController transferController;

    @Mock UserService userService;
    @Mock TransferService transferService;
    @Mock PlayerService playerService;
    @Mock BindingResult bindingResult;
    private final User user = UserFactory.createUser(TeamFactory.createTeam(null));

    @BeforeEach
    public void setup() throws ServerException {
        Mockito.lenient().doReturn(user).when(userService).loggedInUser();
    }

    @Nested
    class CreateTransferTest {
        private final TransferCreateDto transferCreateDto = new TransferCreateDto();

        @Test
        void givenNotValidUserRole_whenCreate_thenThrowServerException() throws ServerException {
            // Given
            transferCreateDto.setPlayerId(UUID.randomUUID());
            Player player = PlayerFactory.createPlayer(null);
            player.setTeam(TeamFactory.createTeam(null));
            Mockito.doReturn(player).when(playerService)
                    .findPlayerById(transferCreateDto.getPlayerId().toString());
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_ADMIN));
            // When
            Executable closureToTest = () -> transferController.create(transferCreateDto, bindingResult);
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        void given_whenCreate_thenReturn201() throws ServerException {
            // Given
            transferCreateDto.setPlayerId(UUID.randomUUID());
            Player player = PlayerFactory.createPlayer(null);
            player.setTeam(TeamFactory.createTeam(null));
            Mockito.doReturn(player).when(playerService)
                    .findPlayerById(transferCreateDto.getPlayerId().toString());
            Transfer transfer = TransferFactory.createTransfer(player, null, null);
            Mockito.doReturn(transfer).when(transferService).createTransfer(transferCreateDto);
            // When
            ResponseEntity<TransferWrapperDto> create = transferController.create(transferCreateDto, bindingResult);
            // Then
            Assertions.assertNotNull(create);
            Assertions.assertNotNull(create.getStatusCode());
            Assertions.assertEquals(HttpStatus.CREATED ,create.getStatusCode());
            Assertions.assertNotNull(create.getBody());
            Assertions.assertNotNull(create.getBody().getTransferDto());
            Assertions.assertEquals(transfer.getId(), create.getBody().getTransferDto().getId());
            Assertions.assertEquals(transfer.getAskedPrice(), create.getBody().getTransferDto().getAskedPrice());
            Assertions.assertEquals(transfer.getMarketValue(), create.getBody().getTransferDto().getMarketValue());
            Assertions.assertEquals(transfer.getPlayer().getId(), create.getBody().getTransferDto().getPlayer().getId());
            Assertions.assertEquals(transfer.getPlayer().getFirstName(), create.getBody().getTransferDto().getPlayer().getFirstName());
            Assertions.assertEquals(transfer.getPlayer().getLastName(), create.getBody().getTransferDto().getPlayer().getLastName());
            Assertions.assertEquals(transfer.getPlayer().getCountry(), create.getBody().getTransferDto().getPlayer().getCountry());
            Assertions.assertEquals(transfer.getPlayer().getAge(), create.getBody().getTransferDto().getPlayer().getAge());
            Assertions.assertEquals(transfer.getPlayer().getPosition(), create.getBody().getTransferDto().getPlayer().getPosition());
            Assertions.assertEquals(transfer.getPlayer().getMarketValue(), create.getBody().getTransferDto().getPlayer().getMarketValue());
            Assertions.assertFalse( create.getBody().getTransferDto().getTransferred());
        }
    }

    @Nested
    class GetAllTransferTest {
        @Test
        void given_whenGetAll_thenReturn200() {
            // Given
            Transfer transfer = TransferFactory.createTransfer(null,null,null);
            Page<Transfer> all = new PageImpl<>(Collections.singletonList(transfer));
            TransferWrapperDto build = TransferWrapperDto.builder()
                    .transfersDto(all.stream().map(DtoConverter::convertEntityToDto).collect(Collectors.toList()))
                    .next(0L)
                    .total(all.getTotalElements())
                    .build();
            Mockito.doReturn(build).when(transferService).getAll(0,0);
            // When
            ResponseEntity<TransferWrapperDto> getAll = transferController.getAll(0,0);
            // Then
            Assertions.assertNotNull(getAll);
            Assertions.assertNotNull(getAll.getStatusCode());
            Assertions.assertEquals(HttpStatus.OK ,getAll.getStatusCode());
            Assertions.assertNotNull(getAll.getBody());
            Assertions.assertEquals(0, getAll.getBody().getNext());
            Assertions.assertEquals(1, getAll.getBody().getTotal());
            Assertions.assertNotNull(getAll.getBody().getTransfersDto());
            Assertions.assertEquals(1, getAll.getBody().getTransfersDto().size());
            Assertions.assertEquals(transfer.getAskedPrice(), getAll.getBody().getTransfersDto().get(0).getAskedPrice());
            Assertions.assertEquals(transfer.getMarketValue(), getAll.getBody().getTransfersDto().get(0).getMarketValue());
            Assertions.assertFalse( getAll.getBody().getTransfersDto().get(0).getTransferred());
        }
    }

    @Nested
    class GetTransferTest {
        @Test
        void given_whenGetTransfer_thenReturn200() throws ServerException {
            // Given
            Transfer transfer = TransferFactory.createTransfer(null,null,null);
            Mockito.doReturn(transfer).when(transferService).findById("id");
            // When
            ResponseEntity<TransferWrapperDto> getTransfer = transferController.getTransfer("id");
            // Then
            Assertions.assertNotNull(getTransfer);
            Assertions.assertNotNull(getTransfer.getStatusCode());
            Assertions.assertEquals(HttpStatus.OK ,getTransfer.getStatusCode());
            Assertions.assertNotNull(getTransfer.getBody());
            Assertions.assertNotNull(getTransfer.getBody().getTransferDto());
            Assertions.assertEquals(transfer.getId(), getTransfer.getBody().getTransferDto().getId());
            Assertions.assertEquals(transfer.getAskedPrice(), getTransfer.getBody().getTransferDto().getAskedPrice());
            Assertions.assertEquals(transfer.getMarketValue(), getTransfer.getBody().getTransferDto().getMarketValue());
            Assertions.assertFalse( getTransfer.getBody().getTransferDto().getTransferred());
        }
    }

    @Nested
    class UpdateTransferTest {
        private final TransferUpdateDto transferUpdateDto = new TransferUpdateDto();

        @Test
        void given_whenUpdate_thenReturn200() throws ServerException {
            // Given
            transferUpdateDto.setAskedPrice(11);
            Transfer transfer = TransferFactory.createTransfer(null,null,null);
            Mockito.doReturn(transfer).when(transferService).updateTransfer("id",transferUpdateDto);
            // When
            ResponseEntity<TransferWrapperDto> updateTransfer = transferController.updateTransfer("id",transferUpdateDto,bindingResult);
            // Then
            Assertions.assertNotNull(updateTransfer);
            Assertions.assertNotNull(updateTransfer.getStatusCode());
            Assertions.assertEquals(HttpStatus.OK ,updateTransfer.getStatusCode());
            Assertions.assertNotNull(updateTransfer.getBody());
            Assertions.assertNotNull(updateTransfer.getBody().getTransferDto());
            Assertions.assertEquals(transfer.getId(), updateTransfer.getBody().getTransferDto().getId());
            Assertions.assertEquals(transfer.getAskedPrice(), updateTransfer.getBody().getTransferDto().getAskedPrice());
            Assertions.assertEquals(transfer.getMarketValue(), updateTransfer.getBody().getTransferDto().getMarketValue());
            Assertions.assertFalse( updateTransfer.getBody().getTransferDto().getTransferred());
        }
    }

    @Nested
    class ConfirmTransferTest {
        @Test
        void given_whenConfirm_thenReturn200() throws ServerException {
            // Given
            Transfer transfer = TransferFactory.createTransfer(null,null,null);
            Mockito.doReturn(transfer).when(transferService).confirmTransfer("id");
            // When
            ResponseEntity<TransferWrapperDto> updateTransfer = transferController.confirmTransfer("id");
            // Then
            Assertions.assertNotNull(updateTransfer);
            Assertions.assertNotNull(updateTransfer.getStatusCode());
            Assertions.assertEquals(HttpStatus.OK ,updateTransfer.getStatusCode());
            Assertions.assertNotNull(updateTransfer.getBody());
            Assertions.assertNotNull(updateTransfer.getBody().getTransferDto());
            Assertions.assertEquals(transfer.getId(), updateTransfer.getBody().getTransferDto().getId());
            Assertions.assertEquals(transfer.getAskedPrice(), updateTransfer.getBody().getTransferDto().getAskedPrice());
            Assertions.assertEquals(transfer.getMarketValue(), updateTransfer.getBody().getTransferDto().getMarketValue());
            Assertions.assertFalse( updateTransfer.getBody().getTransferDto().getTransferred());
        }
    }

    @Nested
    class DeleteTransferTest {

        @Test
        void givenNotValidUserRole_whenDelete_thenThrowServerException() throws ServerException {
            // Given
            Player player = PlayerFactory.createPlayer(TeamFactory.createTeam(null));
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_ADMIN));
            Transfer transfer = TransferFactory.createTransfer(PlayerFactory.createPlayer(null),null,null);
            Mockito.doReturn(transfer).when(transferService).findById("id");
            Mockito.doReturn(player).when(playerService).findPlayerById(transfer.getPlayer().getId());
            // When
            Executable closureToTest = () -> transferController.deleteTransfer("id");
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        void given_whenDelete_thenReturn204() throws ServerException {
            // Given
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            Player player = PlayerFactory.createPlayer(TeamFactory.createTeam(null));
            Transfer transfer = TransferFactory.createTransfer(PlayerFactory.createPlayer(null),null,null);
            Mockito.doReturn(transfer).when(transferService).findById("id");
            Mockito.doReturn(player).when(playerService).findPlayerById(transfer.getPlayer().getId());
            Mockito.doNothing().when(transferService).deleteTransfer("id");
            // When
            ResponseEntity<Void> updateTransfer = transferController.deleteTransfer("id");
            // Then
            Assertions.assertNotNull(updateTransfer);
            Assertions.assertNotNull(updateTransfer.getStatusCode());
            Assertions.assertEquals(HttpStatus.NO_CONTENT ,updateTransfer.getStatusCode());
        }
    }
}