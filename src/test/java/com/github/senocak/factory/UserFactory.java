package com.github.senocak.factory;

import com.github.senocak.model.Role;
import com.github.senocak.model.Team;
import com.github.senocak.model.User;
import com.github.senocak.util.AppConstants;
import java.util.HashSet;
import java.util.Set;
import static com.github.senocak.TestConstants.USER_EMAIL;
import static com.github.senocak.TestConstants.USER_NAME;
import static com.github.senocak.TestConstants.USER_PASSWORD;
import static com.github.senocak.TestConstants.USER_USERNAME;

public class UserFactory {
    private UserFactory(){}

    /**
     * Creates a new user with the given name, username, email, password and roles.
     * @return the new user
     */
    public static User createUser(Team team){
        User user = new User();
        user.setName(USER_NAME);
        user.setUsername(USER_USERNAME);
        user.setEmail(USER_EMAIL);
        user.setPassword(USER_PASSWORD);
        Set<Role> USER_ROLES = new HashSet<>();
        USER_ROLES.add(createRole(AppConstants.RoleName.ROLE_USER));
        USER_ROLES.add(createRole(AppConstants.RoleName.ROLE_ADMIN));
        user.setRoles(USER_ROLES);
        user.setTeam(team);
        return user;
    }

    /**
     * Creates a new role with the given name.
     * @param roleName the name of the role
     * @return the new role
     */
    public static Role createRole(AppConstants.RoleName roleName){
        Role role = new Role();
        role.setName(roleName);
        return role;
    }
}
