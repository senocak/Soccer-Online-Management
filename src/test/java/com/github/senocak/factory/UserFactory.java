package com.github.senocak.factory;

import com.github.senocak.model.Loan;
import com.github.senocak.model.Role;
import com.github.senocak.model.User;
import com.github.senocak.util.AppConstants;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.github.senocak.TestConstants.USER_EMAIL;
import static com.github.senocak.TestConstants.USER_NAME;
import static com.github.senocak.TestConstants.USER_PASSWORD;

public class UserFactory {
    private UserFactory(){}

    public static User createUser() {
        final User build = User.builder()
                .name(USER_NAME)
                .surname(USER_NAME)
                .email(USER_EMAIL)
                .password(USER_PASSWORD)
                .roles(Set.of(createRole(AppConstants.RoleName.ROLE_USER), createRole(AppConstants.RoleName.ROLE_ADMIN)))
                .creditLimit(BigDecimal.valueOf(100_000_000L))
                .usedCreditLimit(BigDecimal.TEN)
                .build();
        build.setId(UUID.randomUUID().toString());
        return build;
    }

    public static User createUser(final Loan loan) {
        final User createUser = createUser();
        createUser.setLoans(List.of(loan));
        return createUser;
    }

    /**
     * Creates a new role with the given name.
     * @param roleName the name of the role
     * @return the new role
     */
    public static Role createRole(AppConstants.RoleName roleName){
        return Role.builder().name(roleName).build();
    }
}
