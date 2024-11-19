package com.github.senocak.factory;

import com.github.senocak.model.Player;
import com.github.senocak.model.Team;
import com.github.senocak.model.Transfer;
import java.util.UUID;

public class TransferFactory {
    private TransferFactory(){}

    /**
     * Creates a new transfer with random data.
     * @return a new transfer with random data.
     */
    public static Transfer createTransfer(Player player, Team transferredFrom, Team transferredTo) {
        Transfer transfer = new Transfer();
        transfer.setId(UUID.randomUUID().toString());
        transfer.setPlayer(player);
        transfer.setAskedPrice(1);
        transfer.setMarketValue(2);
        transfer.setTransferred(false);
        transfer.setTransferredFrom(transferredFrom);
        transfer.setTransferredTo(transferredTo);
        return transfer;
    }
}
