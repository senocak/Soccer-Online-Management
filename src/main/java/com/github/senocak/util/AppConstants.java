package com.github.senocak.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppConstants {
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String TOKEN_HEADER_NAME = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String REFRESH = "refresh";
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
    public enum PlayerPosition {
        GoalKeeper("GK"),
        Defender("DF"),
        Midfielder("MF"),
        Forward("FW");
        private final String text;
    }

    @Getter
    @AllArgsConstructor
    public enum OmaErrorMessageType {
        BASIC_INVALID_INPUT("SVC0001", "Invalid input value for message part %1"),
        GENERIC_SERVICE_ERROR("SVC0002", "The following service error occurred: %1. Error code is %2"),
        DETAILED_INVALID_INPUT("SVC0003", "Invalid input value for %1 %2: %3"),
        EXTRA_INPUT_NOT_ALLOWED("SVC0004", "Input %1 %2 not permitted in request"),
        MANDATORY_INPUT_MISSING("SVC0005", "Mandatory input %1 %2 is missing from request"),
        UNAUTHORIZED("SVC0006", "UnAuthorized Endpoint"),
        JSON_SCHEMA_VALIDATOR("SVC0007", "Schema failed."),
        NOT_FOUND("SVC0008", "Entry is not found"),
        REDIS_CONFIG_EXCEPTION("SVC0009", "Could not get data from redis");

        private final String messageId;
        private final String text;
    }
}
