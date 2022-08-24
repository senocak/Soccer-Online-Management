package com.github.senocak.service;

import com.github.senocak.dto.transfer.TransferCreateDto;
import com.github.senocak.dto.transfer.TransferUpdateDto;
import com.github.senocak.dto.transfer.TransferWrapperDto;
import com.github.senocak.exception.ServerException;
import com.github.senocak.factory.PlayerFactory;
import com.github.senocak.factory.TeamFactory;
import com.github.senocak.factory.TransferFactory;
import com.github.senocak.factory.UserFactory;
import com.github.senocak.model.Player;
import com.github.senocak.model.Team;
import com.github.senocak.model.Transfer;
import com.github.senocak.model.User;
import com.github.senocak.repository.TransferRepository;
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
import org.springframework.data.domain.PageRequest;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for TransferService")
public class TransferServiceTest {
    @InjectMocks TransferService transferService;

    @Mock TransferRepository transferRepository;
    @Mock PlayerService playerService;
    @Mock UserService userService;
    private final User user = UserFactory.createUser(null);

    @BeforeEach
    public void setUp() throws ServerException {
        Mockito.lenient().when(userService.loggedInUser()).thenReturn(user);
    }

    @Nested
    class CreateTransferTest {
        private final TransferCreateDto transferCreateDto = new TransferCreateDto();

        @BeforeEach
        public void setup() {
            transferCreateDto.setAskedPrice(100);
            transferCreateDto.setPlayerId(UUID.randomUUID());
        }

        @Test
        public void givenExist_WhenCreateTransfer_ThenThrowServerException() throws ServerException {
            // Given
            Player player = PlayerFactory.createPlayer(null);
            Mockito.doReturn(player).when(playerService).findPlayerById(transferCreateDto.getPlayerId().toString());
            Transfer transfer = TransferFactory.createTransfer(player,null,null);
            Mockito.doReturn(Optional.of(transfer)).when(transferRepository)
                    .findByPlayerAndTransferred(player, false);
            // When
            Executable closureToTest = () -> transferService.createTransfer(transferCreateDto);
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        public void given_WhenCreateTransfer_ThenAssertResult() throws ServerException {
            // Given
            Player player = PlayerFactory.createPlayer(null);
            Mockito.doReturn(player).when(playerService).findPlayerById(transferCreateDto.getPlayerId().toString());
            Transfer transfer = TransferFactory.createTransfer(player,null,null);
            Mockito.doReturn(null).when(transferRepository)
                    .findByPlayerAndTransferred(player, false);
            Mockito.doReturn(transfer).when(transferRepository)
                    .save(Mockito.any(Transfer.class));
            // When
            Transfer createTransfer = transferService.createTransfer(transferCreateDto);
            // Then
            Assertions.assertEquals(transfer, createTransfer);
        }
    }

    @Nested
    class ConvertEntityToDtoTest {
        @Test
        public void given_WhenConvertEntityToDto_ThenAssertResult() {
            // Given
            Transfer transfer = TransferFactory.createTransfer(null,null,null);
            // When
            TransferWrapperDto convertEntityToDto = TransferService.convertEntityToDto(transfer);
            // Then
            Assertions.assertEquals(transfer.getMarketValue(), convertEntityToDto.getTransferDto().getMarketValue());
            Assertions.assertEquals(transfer.getAskedPrice(), convertEntityToDto.getTransferDto().getAskedPrice());
            Assertions.assertFalse(convertEntityToDto.getTransferDto().getTransferred());
            Assertions.assertNull(convertEntityToDto.getTransferDto().getPlayer());
            Assertions.assertNull(convertEntityToDto.getTransferDto().getTransferredTo());
            Assertions.assertNull(convertEntityToDto.getTransferDto().getTransferredFrom());
        }
    }

    @Nested
    class FindTransferByIdTest {
        @Test
        public void givenNotFound_WhenFindTransferById_ThenThrowServerException() {
            // Given
            Mockito.doReturn(Optional.empty()).when(transferRepository).findById("id");
            // When
            Executable closureToTest = () -> transferService.findById("id");
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        public void given_WhenFindTransferById_ThenAssertResult() throws ServerException {
            // Given
            Transfer transfer = TransferFactory.createTransfer(null,null,null);
            Mockito.doReturn(Optional.of(transfer)).when(transferRepository).findById("id");
            // When
            Transfer findById = transferService.findById("id");
            // Then
            Assertions.assertEquals(transfer, findById);
        }
    }

    @Nested
    class FindByPlayerAndTransferredTest {
        @Test
        public void givenNotFound_WhenFindByPlayerAndTransferred_ThenThrowServerException() {
            // Given
            Player player = PlayerFactory.createPlayer(null);
            Mockito.doReturn(Optional.empty()).when(transferRepository).findByPlayerAndTransferred(player, false);
            // When
            Executable closureToTest = () -> transferService.findByPlayerAndTransferred(player, false);
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        public void given_WhenFindByPlayerAndTransferred_ThenAssertResult() throws ServerException {
            // Given
            Transfer transfer = TransferFactory.createTransfer(null,null,null);
            Mockito.doReturn(Optional.of(transfer)).when(transferRepository).findByPlayerAndTransferred(null, false);
            // When
            Transfer findByPlayerAndTransferred = transferService.findByPlayerAndTransferred(null, false);
            // Then
            Assertions.assertEquals(transfer, findByPlayerAndTransferred);
        }
    }

    @Nested
    class GetAllTest {
        @Test
        public void given_WhenGetAll_ThenAssertResult() {
            // Given
            Transfer transfer = TransferFactory.createTransfer(null,null,null);
            Page<Transfer> all = new PageImpl<>(Collections.singletonList(transfer));
            Mockito.doReturn(all).when(transferRepository)
                    .findAll(PageRequest.of(10, 10));
            // When
            TransferWrapperDto getAll = transferService.getAll(10,10);
            // Then
            Assertions.assertNotNull(getAll.getTransfersDto());
            Assertions.assertEquals(1,getAll.getTransfersDto().size());
            Assertions.assertEquals(transfer.getMarketValue(), getAll.getTransfersDto().get(0).getMarketValue());
            Assertions.assertEquals(transfer.getAskedPrice(), getAll.getTransfersDto().get(0).getAskedPrice());
            Assertions.assertFalse(getAll.getTransfersDto().get(0).getTransferred());
            Assertions.assertNull(getAll.getTransfersDto().get(0).getPlayer());
            Assertions.assertNull(getAll.getTransfersDto().get(0).getTransferredTo());
            Assertions.assertNull(getAll.getTransfersDto().get(0).getTransferredFrom());
            Assertions.assertEquals(1, getAll.getTotal());
            Assertions.assertEquals(0, getAll.getNext());
        }
    }

    @Nested
    class DeleteTransferTest {

        @Test
        public void givenUserHasNoTransfer_WhenDeleteTransfer_ThenThrowServerException() {
            // Given
            user.setTeam(null);
            // When
            Executable closureToTest = () -> transferService.deleteTransfer("id");
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        public void givenNotExistTransfer_WhenDeleteTransfer_ThenThrowServerException() {
            // Given
            user.setTeam(TeamFactory.createTeam(null));
            Mockito.doReturn(Optional.empty()).when(transferRepository).findById("id");
            // When
            Executable closureToTest = () -> transferService.deleteTransfer("id");
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        public void givenTransferred_WhenDeleteTransfer_ThenThrowServerException() {
            // Given
            user.setTeam(TeamFactory.createTeam(null));
            Transfer transfer = TransferFactory.createTransfer(null,null,null);
            transfer.setTransferred(true);
            Mockito.doReturn(Optional.of(transfer)).when(transferRepository).findById("id");
            // When
            Executable closureToTest = () -> transferService.deleteTransfer("id");
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        public void given_WhenDeleteTransfer_ThenAssertResult() throws ServerException {
            // Given
            user.setTeam(TeamFactory.createTeam(null));
            Transfer transfer = TransferFactory.createTransfer(null,null,null);
            Mockito.doReturn(Optional.of(transfer)).when(transferRepository).findById("id");
            // When
            transferService.deleteTransfer("id");
            // Then
            Mockito.verify(transferRepository).delete(transfer);
        }
    }

    @Nested
    class UpdateTransferTest {
        private final TransferUpdateDto transferUpdateDto = new TransferUpdateDto();

        @BeforeEach
        public void setup() {
            transferUpdateDto.setAskedPrice(123);
        }

        @Test
        public void givenNotExist_WhenUpdateTransfer_ThenThrowServerException() {
            // Given
            Mockito.doReturn(Optional.empty()).when(transferRepository).findById("id");
            // When
            Executable closureToTest = () -> transferService.updateTransfer("id", transferUpdateDto);
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        public void givenInvalidTransfer_WhenUpdateTransfer_ThenThrowServerException() {
            // Given
            user.setTeam(null);
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_ADMIN));
            Transfer transfer = TransferFactory.createTransfer(null,null,null);
            Mockito.doReturn(Optional.of(transfer)).when(transferRepository).findById("id");
            // When
            Executable closureToTest = () -> transferService.updateTransfer("id",transferUpdateDto);
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        public void givenTransferred_WhenUpdateTransfer_ThenThrowServerException() {
            // Given
            user.setTeam(TeamFactory.createTeam(null));
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            Transfer transfer = TransferFactory.createTransfer(null,null,null);
            transfer.setTransferred(true);
            Mockito.doReturn(Optional.of(transfer)).when(transferRepository).findById("id");
            // When
            Executable closureToTest = () -> transferService.updateTransfer("id",transferUpdateDto);
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        public void given_WhenUpdateTransfer_ThenAssertResult() throws ServerException {
            // Given
            user.setTeam(TeamFactory.createTeam(null));
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            Transfer transfer = TransferFactory.createTransfer(null,null,null);
            transfer.setTransferred(false);
            Mockito.doReturn(transfer).when(transferRepository).save(Mockito.any(Transfer.class));
            Mockito.doReturn(Optional.of(transfer)).when(transferRepository).findById("id");
            // When
            Transfer updateTransfer = transferService.updateTransfer("id", transferUpdateDto);
            // Then
            Assertions.assertEquals(transfer, updateTransfer);
        }
    }

    @Nested
    class ConfirmTransferTest {
        @Test
        public void givenNotExist_WhenConfirmTransfer_ThenThrowServerException() {
            // Given
            Mockito.doReturn(Optional.empty()).when(transferRepository).findById("id");
            // When
            Executable closureToTest = () -> transferService.confirmTransfer("id");
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        public void givenInvalidTransfer_WhenConfirmTransfer_ThenThrowServerException() {
            // Given
            user.setTeam(null);
            Transfer transfer = TransferFactory.createTransfer(null,null,null);
            Mockito.doReturn(Optional.of(transfer)).when(transferRepository).findById("id");
            // When
            Executable closureToTest = () -> transferService.confirmTransfer("id");
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        public void givenInvalidRole_WhenConfirmTransfer_ThenThrowServerException() {
            // Given
            Team team = TeamFactory.createTeam(null);
            user.setTeam(team);
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_ADMIN));
            Transfer transfer = TransferFactory.createTransfer(PlayerFactory.createPlayer(team),null,null);
            Mockito.doReturn(Optional.of(transfer)).when(transferRepository).findById("id");
            // When
            Executable closureToTest = () -> transferService.confirmTransfer("id");
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        public void givenInvalidCash_WhenConfirmTransfer_ThenThrowServerException() {
            // Given
            user.setTeam(TeamFactory.createTeam(null));
            user.getTeam().setAvailableCash(0);
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            Transfer transfer = TransferFactory.createTransfer(null,null,null);
            Mockito.doReturn(Optional.of(transfer)).when(transferRepository).findById("id");
            // When
            Executable closureToTest = () -> transferService.confirmTransfer("id");
            // Then
            Assertions.assertThrows(ServerException.class, closureToTest);
        }

        @Test
        public void given_WhenConfirmTransfer_ThenAssertResult() throws ServerException {
            // Given
            user.setTeam(TeamFactory.createTeam(null));
            user.getRoles().removeIf(role -> role.getName().equals(AppConstants.RoleName.ROLE_USER));
            Transfer transfer = TransferFactory.createTransfer(PlayerFactory.createPlayer(null),null,null);
            transfer.setTransferred(false);
            Mockito.doReturn(transfer).when(transferRepository).save(Mockito.any(Transfer.class));
            Mockito.doReturn(Optional.of(transfer)).when(transferRepository).findById("id");
            // When
            Transfer updateTransfer = transferService.confirmTransfer("id");
            // Then
            Assertions.assertEquals(transfer, updateTransfer);
        }
    }
}
