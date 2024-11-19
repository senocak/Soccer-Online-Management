package com.github.senocak.service;

import com.github.senocak.dto.transfer.TransferCreateDto;
import com.github.senocak.dto.transfer.TransferDto;
import com.github.senocak.dto.transfer.TransferUpdateDto;
import com.github.senocak.dto.transfer.TransferWrapperDto;
import com.github.senocak.exception.ServerException;
import com.github.senocak.model.Player;
import com.github.senocak.model.Transfer;
import com.github.senocak.model.User;
import com.github.senocak.repository.TransferRepository;
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
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {
    private final TransferRepository transferRepository;
    private final PlayerService playerService;
    private final UserService userService;

    /**
     * Create transfer
     * @param transferCreateDto transfer create dto
     * @return  transfer entity
     * @throws ServerException  server exception
     */
    public Transfer createTransfer(TransferCreateDto transferCreateDto) throws ServerException {
        Transfer transferByPlayer = null;
        Player player = playerService.findPlayerById(transferCreateDto.getPlayerId().toString());
        try{
            transferByPlayer = findByPlayerAndTransferred(player, false);
        }catch (ServerException serverException) {
            log.debug("Caught ServerException, Transfer exist in db");
        }
        if (Objects.nonNull(transferByPlayer)){
            String error = "Transfer " + transferByPlayer.getPlayer().getFirstName() + " already in list";
            log.error(error);
            throw new ServerException(AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT,
                    new String[]{error}, HttpStatus.CONFLICT);
        }

        Transfer build = Transfer.builder()
                .player(player)
                .askedPrice(transferCreateDto.getAskedPrice())
                .build();
        return saveTransfer(build);
    }

    /**
     * @param transfer  transfer entity
     * @return  transfer dto
     */
    public static TransferWrapperDto convertEntityToDto(Transfer transfer) {
        TransferDto transferDto = DtoConverter.convertEntityToDto(transfer);
        if (transferDto.getTransferredTo() != null)
            transferDto.getTransferredTo().setPlayers(null);
        if (transferDto.getTransferredFrom() != null)
            transferDto.getTransferredFrom().setPlayers(null);
        return TransferWrapperDto.builder().transferDto(transferDto).build();
    }

    /**
     * @param id -- id or name of Transfer entity
     * @return -- Transfer entity that is retrieved from db
     * @throws ServerException -- if Transfer is not found
     */
    public Transfer findById(String id) throws ServerException {
        Optional<Transfer> optionalTransfer = transferRepository.findById(id);
        if (!optionalTransfer.isPresent()) {
            log.error("Transfer is not found.");
            throw new ServerException(AppConstants.OmaErrorMessageType.NOT_FOUND,
                    new String[]{"Transfer: " + id}, HttpStatus.NOT_FOUND);
        }
        return optionalTransfer.get();
    }

    /**
     * @param player -- player entity
     * @param transferred   -- boolean value
     * @return  -- Transfer entity that is retrieved from db
     * @throws ServerException  -- if Transfer is not found
     */
    public Transfer findByPlayerAndTransferred(Player player, boolean transferred) throws ServerException {
        Optional<Transfer> optionalTransfer = transferRepository.findByPlayerAndTransferred(player, transferred);
        if (Objects.isNull(optionalTransfer) || !optionalTransfer.isPresent()) {
            log.error("Transfer is not found.");
            throw new ServerException(AppConstants.OmaErrorMessageType.NOT_FOUND,
                    new String[]{"Transfer: " + player.getFirstName()}, HttpStatus.NOT_FOUND);
        }
        return optionalTransfer.get();
    }

    /**
     * @param nextPage -- next page variable to filter
     * @param maxNumber -- max page to retrieve from db
     * @return -- transfer list
     */
    public TransferWrapperDto getAll(int nextPage, int maxNumber) {
        Pageable paging = PageRequest.of(nextPage, maxNumber);
        Page<Transfer> all = transferRepository.findAll(paging);
        List<TransferDto> transferDtos = all.stream().map(DtoConverter::convertEntityToDto)
                .collect(Collectors.toList());
        return TransferWrapperDto.builder()
                .transfersDto(transferDtos)
                .next((long) (all.hasNext() ? nextPage + 1 : 0))
                .total(all.getTotalElements())
                .build();
    }

    /**
     * @param id -- id of Transfer entity
     * @param transferUpdateDto -- transfer update dto
     * @return  -- Transfer entity that is retrieved from db
     * @throws ServerException  -- if Transfer is not found
     */
    public Transfer updateTransfer(String id, TransferUpdateDto transferUpdateDto) throws ServerException {
        Transfer transfer = findById(id);
        User user = getUserFromContext();
        // Admin access here
        if (user.getRoles().stream().noneMatch(role -> role.getName().equals(AppConstants.RoleName.ROLE_ADMIN)) &&
                (Objects.isNull(user.getTeam()) || !user.getTeam().equals(transfer.getPlayer().getTeam()))) {
            log.error("Team does not belong to user");
            throw new ServerException(AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT,
                    new String[]{"Team does not belong to user"}, HttpStatus.FORBIDDEN);
        }
        if (transfer.isTransferred()) {
            log.error("Transfer is already transferred");
            throw new ServerException(AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT,
                    new String[]{"Transfer is already transferred"}, HttpStatus.FORBIDDEN);
        }
        int askedPrice = transferUpdateDto.getAskedPrice();
        if (askedPrice > 0)
            transfer.setAskedPrice(askedPrice);
        return saveTransfer(transfer);
    }

    /**
     * @param id -- id of Transfer entity
     * @return  -- Transfer entity that is retrieved from db
     * @throws ServerException  -- if Transfer is not found
     */
    public Transfer confirmTransfer(String id) throws ServerException {
        Transfer transfer = transferRepository.findById(id).orElseThrow(() ->
                new ServerException(AppConstants.OmaErrorMessageType.NOT_FOUND,
                        new String[]{"Transfer: "+ id}, HttpStatus.NOT_FOUND));
        User user = getUserFromContext();
        if (Objects.isNull(user.getTeam())) {
            log.error("User has no team");
            throw new ServerException(AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT,
                    new String[]{"User has no team"}, HttpStatus.BAD_REQUEST);
        }
        // Admin access here
        if (user.getRoles().stream().noneMatch(role -> role.getName().equals(AppConstants.RoleName.ROLE_ADMIN)) &&
                transfer.getPlayer().getTeam().equals(user.getTeam())) {
            log.error("Transfer player is in user team");
            throw new ServerException(AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT,
                    new String[]{"Transfer player is in user team"}, HttpStatus.BAD_REQUEST);
        }
        if (user.getTeam().getAvailableCash() < transfer.getAskedPrice()) {
            log.error("Buyer team has not enough cash");
            throw new ServerException(AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT,
                    new String[]{"Buyer team has not enough cash"}, HttpStatus.BAD_REQUEST);
        }
        transfer.setTransferred(true);
        transfer.setMarketValue(transfer.getAskedPrice() * new Random().nextInt(100));
        transfer.setTransferredTo(user.getTeam());
        transfer.setTransferredFrom(transfer.getPlayer().getTeam());
        transfer.getPlayer().setTeam(user.getTeam());
        user.getTeam().setAvailableCash(user.getTeam().getAvailableCash() - transfer.getAskedPrice());
        transfer.getPlayer().getTeam().setAvailableCash(transfer.getPlayer().getTeam().getAvailableCash() + transfer.getAskedPrice());
        return saveTransfer(transfer);
    }

    /**
     * Update Transfer
     * @param transfer -- Transfer entity to be wanted to update
     * @return -- Updated Transfer entity
     */
    private Transfer saveTransfer(Transfer transfer) {
        return transferRepository.save(transfer);
    }

    /**
     * @param id -- id of Transfer entity
     */
    public void deleteTransfer(String id) throws ServerException {
        User user = getUserFromContext();
        if (Objects.isNull(user.getTeam())) {
            log.error("User has no team");
            throw new ServerException(AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT,
                    new String[]{"User has no team"}, HttpStatus.BAD_REQUEST);
        }
        Transfer transfer = transferRepository.findById(id).orElseThrow(() ->
                new ServerException(AppConstants.OmaErrorMessageType.NOT_FOUND,
                        new String[]{"Transfer", id}, HttpStatus.NOT_FOUND));
        if (transfer.isTransferred()) {
            log.error("Transfer is already transferred");
            throw new ServerException(AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT,
                    new String[]{"Transfer is already transferred"}, HttpStatus.BAD_REQUEST);
        }
        transferRepository.delete(findById(id));
    }

    /**
     * @return -- User object retrieved from security context
     * @throws ServerException -- if user not found in context based on jwt token
     */
    private User getUserFromContext() throws ServerException {
        return userService.loggedInUser();
    }
}
