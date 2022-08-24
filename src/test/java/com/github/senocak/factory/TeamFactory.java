package com.github.senocak.factory;

import com.github.senocak.model.Team;
import com.github.senocak.model.User;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

public class TeamFactory {
    private TeamFactory(){}

    /**
     * Creates a new team with random data.
     * @return a new team with random data.
     */
    public static Team createTeam(User user){
        Team team = new Team();
        team.setId(UUID.randomUUID().toString());
        team.setName("Random Team");
        team.setCountry("Turkey");
        team.setUser(user);
        team.setPlayers(Collections.singletonList(PlayerFactory.createPlayer(null)));
        team.setAvailableCash(100);
        team.setCreatedAt(new Date());
        team.setUpdatedAt(new Date());
        return team;
    }
}
