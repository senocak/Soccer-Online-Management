package com.github.senocak.service;

import org.springframework.security.core.userdetails.User;
import java.nio.file.AccessDeniedException;

public interface AuthenticationService {
    String AUTHORIZATION_FAILED = "Authentication error";

    boolean isAuthorized(String... aInRoles) throws AccessDeniedException;
    User getPrinciple();
}
