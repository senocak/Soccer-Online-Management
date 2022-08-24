package com.github.senocak.exception;

import com.github.senocak.util.AppConstants;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
public class ServerException extends Exception {
    private final AppConstants.OmaErrorMessageType omaErrorMessageType;
    private final String[] variables;
    private final HttpStatus statusCode;
}
