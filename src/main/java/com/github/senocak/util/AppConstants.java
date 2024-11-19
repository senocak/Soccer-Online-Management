package com.github.senocak.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class AppConstants {
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String TOKEN_HEADER_NAME = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";

    @Getter
    @AllArgsConstructor
    public enum RoleName {
        ROLE_USER(USER),
        ROLE_ADMIN(ADMIN);
        private final String role;
    }

    @Getter
    @AllArgsConstructor
    public enum OmaErrorMessageType {
        BASIC_INVALID_INPUT("SVC0001", "Invalid input value for message part %1"),
        GENERIC_SERVICE_ERROR("SVC0002", "The following service error occurred: %1. Error code is %2"),
        NOT_FOUND("SVC0003", "Entry is not found"),
        EXTRA_INPUT_NOT_ALLOWED("SVC0004", "Input %1 %2 not permitted in request"),
        MANDATORY_INPUT_MISSING("SVC0005", "Mandatory input %1 %2 is missing from request"),
        UNAUTHORIZED("SVC0006", "UnAuthorized Endpoint"),
        JSON_SCHEMA_VALIDATOR("SVC0007", "Schema failed.");

        private final String messageId;
        private final String text;
    }
}
