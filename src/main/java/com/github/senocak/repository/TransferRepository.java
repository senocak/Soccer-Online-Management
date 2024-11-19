package com.github.senocak.repository;

import com.github.senocak.model.Player;
import com.github.senocak.model.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, String> {
    Optional<Transfer> findByPlayerAndTransferred(Player player, boolean transferred);
}
