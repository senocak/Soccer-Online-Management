package com.github.senocak.factory;

import com.github.senocak.model.Player;
import com.github.senocak.model.Team;
import com.github.senocak.util.AppConstants;
import java.util.Date;
import java.util.UUID;

public class PlayerFactory {
    private PlayerFactory(){}

    /**
     * Creates a new Player instance.
     * @return a new Player instance.
     */
    public static Player createPlayer(Team team){
        Player player = new Player();
        player.setId(UUID.randomUUID().toString());
        player.setFirstName("John");
        player.setLastName("Doe");
        player.setPosition(AppConstants.PlayerPosition.Forward);
        player.setTeam(team);
        player.setCountry("Turkey");
        player.setAge(30);
        player.setMarketValue(100);
        player.setCreatedAt(new Date());
        player.setUpdatedAt(new Date());
        return player;
    }
}
