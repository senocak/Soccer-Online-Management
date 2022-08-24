package com.github.senocak.exception;

import com.github.senocak.dto.ExceptionDto;
import com.github.senocak.util.AppConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.TypeMismatchException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import javax.validation.ConstraintViolationException;
import java.security.InvalidParameterException;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler({
        BadCredentialsException.class,
        ConstraintViolationException.class,
        InvalidParameterException.class
    })
    public ResponseEntity<Object> handleBadRequestException(Exception ex) {
        return generateResponseEntity(
                HttpStatus.BAD_REQUEST,
                AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT,
                new String[]{ex.getMessage()});
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(RuntimeException ex) {
        return generateResponseEntity(
                HttpStatus.UNAUTHORIZED,
                AppConstants.OmaErrorMessageType.UNAUTHORIZED,
                new String[]{ex.getMessage()});
    }

    @ExceptionHandler(ServerException.class)
    public ResponseEntity<Object> handleServerException(Exception ex) {
        return generateResponseEntity(
                ((ServerException) ex).getStatusCode(),
                ((ServerException) ex).getOmaErrorMessageType(),
                ((ServerException) ex).getVariables());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex) {
        return generateResponseEntity(
                HttpStatus.INTERNAL_SERVER_ERROR,
                AppConstants.OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                new String[]{ex.getMessage()});
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                         HttpHeaders headers, HttpStatus status,
                                                                         WebRequest request) {
        return generateResponseEntity(
                HttpStatus.METHOD_NOT_ALLOWED,
                AppConstants.OmaErrorMessageType.EXTRA_INPUT_NOT_ALLOWED,
                new String[]{ex.getMessage()});
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers,
                                                                  HttpStatus status, WebRequest request) {
        return generateResponseEntity(
                HttpStatus.BAD_REQUEST,
                AppConstants.OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                new String[]{ex.getMessage()});
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers,
                                                                     HttpStatus status, WebRequest request) {
        return generateResponseEntity(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT,
                new String[]{ex.getMessage()});
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers,
                                                                   HttpStatus status, WebRequest request) {
        return generateResponseEntity(
                HttpStatus.NOT_FOUND,
                AppConstants.OmaErrorMessageType.NOT_FOUND,
                new String[]{ex.getMessage()});
    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex, HttpHeaders headers,
                                                               HttpStatus status, WebRequest request) {
        return generateResponseEntity(
                HttpStatus.BAD_REQUEST,
                AppConstants.OmaErrorMessageType.MANDATORY_INPUT_MISSING,
                new String[]{ex.getMessage()});
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
                                                        HttpStatus status, WebRequest request) {
        return generateResponseEntity(
                HttpStatus.BAD_REQUEST,
                AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT,
                new String[]{ex.getMessage()});
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                          HttpHeaders headers, HttpStatus status, WebRequest request) {
        return generateResponseEntity(
                HttpStatus.INTERNAL_SERVER_ERROR,
                AppConstants.OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                new String[]{ex.getMessage()});
    }

    /**
     *
     * @param httpStatus -- returned code
     * @return -- returned body
     */
    private ResponseEntity<Object> generateResponseEntity(HttpStatus httpStatus, AppConstants.OmaErrorMessageType omaErrorMessageType, String[] variables){
        log.error("Exception is handled. HttpStatus: {}, OmaErrorMessageType: {}, variables: {}",
                httpStatus, omaErrorMessageType, variables);
        ExceptionDto exceptionDto = new ExceptionDto();
        exceptionDto.setStatusCode(httpStatus.value());
        ExceptionDto.OmaErrorMessageTypeDto omaErrorMessageTypeDto = new ExceptionDto.OmaErrorMessageTypeDto();
        omaErrorMessageTypeDto.setMessageId(omaErrorMessageType.getMessageId());
        omaErrorMessageTypeDto.setText(omaErrorMessageType.getText());
        exceptionDto.setOmaErrorMessageType(omaErrorMessageTypeDto);
        exceptionDto.setVariables(variables);
        return ResponseEntity.status(httpStatus).body(exceptionDto);
    }
}
