package com.github.senocak.service;

import lombok.extern.slf4j.Slf4j;
import java.nio.file.AccessDeniedException;
import java.util.Objects;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    /**
     * Getting username from the security context
     * @param aInRoles -- roles that user must have
     * @return  -- boolean or null
     * @throws AccessDeniedException -- if user does not have required roles
     */
    @Override
    public boolean isAuthorized(String... aInRoles) throws AccessDeniedException {
        User getPrinciple = getPrinciple();
        if (Objects.isNull(getPrinciple)) {
            throw new AccessDeniedException(AUTHORIZATION_FAILED);
        }
        try {
            for (String role : aInRoles) {
                for (GrantedAuthority authority : getPrinciple.getAuthorities()) {
                    if (authority.getAuthority().equals("ROLE_" + role)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            throw new AccessDeniedException(AUTHORIZATION_FAILED);
        }
        return false;
    }

    /**
     * Getting user object that is in the security context
     * @return -- user object or null
     */
    @Override
    public User getPrinciple() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        try {
            if (authentication.getPrincipal() instanceof User) {
                return (User) authentication.getPrincipal();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
