package com.github.senocak.exception;

import com.github.senocak.dto.ExceptionDto;
import com.github.senocak.util.AppConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.NoHandlerFoundException;
import java.util.Arrays;
import java.util.Optional;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for RestExceptionHandler")
public class RestExceptionHandlerTest {
    @InjectMocks RestExceptionHandler restExceptionHandler;

    @Test
    public void givenExceptionWhenHandleBadRequestExceptionThenAssertResult(){
        // Given
        Exception ex = new BadCredentialsException("lorem");
        // When
        ResponseEntity<Object> handleBadRequestException = restExceptionHandler.handleBadRequestException(ex);
        ExceptionDto exceptionDto = (ExceptionDto)handleBadRequestException.getBody();
        // Then
        Assertions.assertNotNull(exceptionDto);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, handleBadRequestException.getStatusCode());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), exceptionDto.getStatusCode());
        Assertions.assertEquals(AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT.getMessageId(), exceptionDto.getOmaErrorMessageType().getMessageId());
        Assertions.assertEquals(AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT.getText(), exceptionDto.getOmaErrorMessageType().getText());
        Assertions.assertEquals(1, exceptionDto.getVariables().length);
        Optional<String> message = Arrays.stream(exceptionDto.getVariables()).findFirst();
        Assertions.assertTrue(message.isPresent());
        Assertions.assertEquals(ex.getMessage(), message.get());
    }

    @Test
    public void givenExceptionWhenHandleAccessDeniedExceptionThenAssertResult(){
        // Given
        RuntimeException ex = new AccessDeniedException("lorem");
        // When
        ResponseEntity<Object> handleBadRequestException = restExceptionHandler.handleAccessDeniedException(ex);
        ExceptionDto exceptionDto = (ExceptionDto)handleBadRequestException.getBody();
        // Then
        Assertions.assertNotNull(exceptionDto);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, handleBadRequestException.getStatusCode());
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), exceptionDto.getStatusCode());
        Assertions.assertEquals(AppConstants.OmaErrorMessageType.UNAUTHORIZED.getMessageId(), exceptionDto.getOmaErrorMessageType().getMessageId());
        Assertions.assertEquals(AppConstants.OmaErrorMessageType.UNAUTHORIZED.getText(), exceptionDto.getOmaErrorMessageType().getText());
        Assertions.assertEquals(1, exceptionDto.getVariables().length);
        Optional<String> message = Arrays.stream(exceptionDto.getVariables()).findFirst();
        Assertions.assertTrue(message.isPresent());
        Assertions.assertEquals(ex.getMessage(), message.get());
    }

    @Test
    public void givenExceptionWhenHandleServerExceptionThenAssertResult(){
        // Given
        String errrMsg = "lorem";
        Exception ex = new ServerException(AppConstants.OmaErrorMessageType.NOT_FOUND, new String[]{errrMsg}, HttpStatus.CONFLICT);
        // When
        ResponseEntity<Object> handleBadRequestException = restExceptionHandler.handleServerException(ex);
        ExceptionDto exceptionDto = (ExceptionDto)handleBadRequestException.getBody();
        // Then
        Assertions.assertNotNull(exceptionDto);
        Assertions.assertEquals(HttpStatus.CONFLICT, handleBadRequestException.getStatusCode());
        Assertions.assertEquals(HttpStatus.CONFLICT.value(), exceptionDto.getStatusCode());
        Assertions.assertEquals(AppConstants.OmaErrorMessageType.NOT_FOUND.getMessageId(), exceptionDto.getOmaErrorMessageType().getMessageId());
        Assertions.assertEquals(AppConstants.OmaErrorMessageType.NOT_FOUND.getText(), exceptionDto.getOmaErrorMessageType().getText());
        Assertions.assertEquals(1, exceptionDto.getVariables().length);
        Optional<String> message = Arrays.stream(exceptionDto.getVariables()).findFirst();
        Assertions.assertTrue(message.isPresent());
        Assertions.assertEquals(errrMsg, message.get());
    }

    @Test
    public void givenExceptionWhenHandleGeneralExceptionThenAssertResult(){
        // Given
        Exception ex = new Exception("lorem");
        // When
        ResponseEntity<Object> handleBadRequestException = restExceptionHandler.handleGeneralException(ex);
        ExceptionDto exceptionDto = (ExceptionDto)handleBadRequestException.getBody();
        // Then
        Assertions.assertNotNull(exceptionDto);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, handleBadRequestException.getStatusCode());
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), exceptionDto.getStatusCode());
        Assertions.assertEquals(AppConstants.OmaErrorMessageType.GENERIC_SERVICE_ERROR.getMessageId(), exceptionDto.getOmaErrorMessageType().getMessageId());
        Assertions.assertEquals(AppConstants.OmaErrorMessageType.GENERIC_SERVICE_ERROR.getText(), exceptionDto.getOmaErrorMessageType().getText());
        Assertions.assertEquals(1, exceptionDto.getVariables().length);
        Optional<String> message = Arrays.stream(exceptionDto.getVariables()).findFirst();
        Assertions.assertTrue(message.isPresent());
        Assertions.assertEquals(ex.getMessage(), message.get());
    }

    @Test
    public void givenExceptionWhenHandleHttpRequestMethodNotSupportedThenAssertResult(){
        // Given
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("lorem");
        // When
        ResponseEntity<Object> handleBadRequestException = restExceptionHandler.handleHttpRequestMethodNotSupported(ex, null, null, null);
        ExceptionDto exceptionDto = (ExceptionDto)handleBadRequestException.getBody();
        // Then
        Assertions.assertNotNull(exceptionDto);
        Assertions.assertEquals(HttpStatus.METHOD_NOT_ALLOWED, handleBadRequestException.getStatusCode());
        Assertions.assertEquals(HttpStatus.METHOD_NOT_ALLOWED.value(), exceptionDto.getStatusCode());
        Assertions.assertEquals(AppConstants.OmaErrorMessageType.EXTRA_INPUT_NOT_ALLOWED.getMessageId(), exceptionDto.getOmaErrorMessageType().getMessageId());
        Assertions.assertEquals(AppConstants.OmaErrorMessageType.EXTRA_INPUT_NOT_ALLOWED.getText(), exceptionDto.getOmaErrorMessageType().getText());
        Assertions.assertEquals(1, exceptionDto.getVariables().length);
        Optional<String> message = Arrays.stream(exceptionDto.getVariables()).findFirst();
        Assertions.assertTrue(message.isPresent());
        Assertions.assertEquals(ex.getMessage(), message.get());
    }

    @Test
    public void givenExceptionWhenHandleHttpMessageNotReadableThenAssertResult(){
        // Given
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("lorem");
        // When
        ResponseEntity<Object> handleBadRequestException = restExceptionHandler.handleHttpMessageNotReadable(ex, null, null, null);
        ExceptionDto exceptionDto = (ExceptionDto)handleBadRequestException.getBody();
        // Then
        Assertions.assertNotNull(exceptionDto);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, handleBadRequestException.getStatusCode());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), exceptionDto.getStatusCode());
        Assertions.assertEquals(AppConstants.OmaErrorMessageType.GENERIC_SERVICE_ERROR.getMessageId(), exceptionDto.getOmaErrorMessageType().getMessageId());
        Assertions.assertEquals(AppConstants.OmaErrorMessageType.GENERIC_SERVICE_ERROR.getText(), exceptionDto.getOmaErrorMessageType().getText());
        Assertions.assertEquals(1, exceptionDto.getVariables().length);
        Optional<String> message = Arrays.stream(exceptionDto.getVariables()).findFirst();
        Assertions.assertTrue(message.isPresent());
        Assertions.assertEquals(ex.getMessage(), message.get());
    }

    @Test
    public void givenExceptionWhenHandleHttpMediaTypeNotSupportedThenAssertResult(){
        // Given
        HttpMediaTypeNotSupportedException ex = new HttpMediaTypeNotSupportedException("lorem");
        // When
        ResponseEntity<Object> handleBadRequestException = restExceptionHandler.handleHttpMediaTypeNotSupported(ex, null, null, null);
        ExceptionDto exceptionDto = (ExceptionDto)handleBadRequestException.getBody();
        // Then
        Assertions.assertNotNull(exceptionDto);
        Assertions.assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, handleBadRequestException.getStatusCode());
        Assertions.assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), exceptionDto.getStatusCode());
        Assertions.assertEquals(AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT.getMessageId(), exceptionDto.getOmaErrorMessageType().getMessageId());
        Assertions.assertEquals(AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT.getText(), exceptionDto.getOmaErrorMessageType().getText());
        Assertions.assertEquals(1, exceptionDto.getVariables().length);
        Optional<String> message = Arrays.stream(exceptionDto.getVariables()).findFirst();
        Assertions.assertTrue(message.isPresent());
        Assertions.assertEquals(ex.getMessage(), message.get());
    }

    @Test
    public void givenExceptionWhenHandleNoHandlerFoundExceptionThenAssertResult(){
        // Given
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "", null);
        // When
        ResponseEntity<Object> handleBadRequestException = restExceptionHandler.handleNoHandlerFoundException(ex, null, null, null);
        ExceptionDto exceptionDto = (ExceptionDto)handleBadRequestException.getBody();
        // Then
        Assertions.assertNotNull(exceptionDto);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, handleBadRequestException.getStatusCode());
        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), exceptionDto.getStatusCode());
        Assertions.assertEquals(AppConstants.OmaErrorMessageType.NOT_FOUND.getMessageId(), exceptionDto.getOmaErrorMessageType().getMessageId());
        Assertions.assertEquals(AppConstants.OmaErrorMessageType.NOT_FOUND.getText(), exceptionDto.getOmaErrorMessageType().getText());
        Assertions.assertEquals(1, exceptionDto.getVariables().length);
        Optional<String> message = Arrays.stream(exceptionDto.getVariables()).findFirst();
        Assertions.assertTrue(message.isPresent());
        Assertions.assertEquals("No handler found for GET ", message.get());
    }

    @Test
    public void givenExceptionWhenHandleMissingPathVariableThenAssertResult(){
        // Given
        MissingPathVariableException ex = Mockito.mock(MissingPathVariableException.class);
        Mockito.doReturn("lorem").when(ex).getMessage();
        // When
        ResponseEntity<Object> handleBadRequestException = restExceptionHandler.handleMissingPathVariable(ex, null, null, null);
        ExceptionDto exceptionDto = (ExceptionDto)handleBadRequestException.getBody();
        // Then
        Assertions.assertNotNull(exceptionDto);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, handleBadRequestException.getStatusCode());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), exceptionDto.getStatusCode());
        Assertions.assertEquals(AppConstants.OmaErrorMessageType.MANDATORY_INPUT_MISSING.getMessageId(), exceptionDto.getOmaErrorMessageType().getMessageId());
        Assertions.assertEquals(AppConstants.OmaErrorMessageType.MANDATORY_INPUT_MISSING.getText(), exceptionDto.getOmaErrorMessageType().getText());
        Assertions.assertEquals(1, exceptionDto.getVariables().length);
        Optional<String> message = Arrays.stream(exceptionDto.getVariables()).findFirst();
        Assertions.assertTrue(message.isPresent());
        Assertions.assertEquals("lorem", message.get());
    }

    @Test
    public void givenExceptionWhenHandleTypeMismatchThenAssertResult(){
        // Given
        TypeMismatchException ex = Mockito.mock(TypeMismatchException.class);
        Mockito.doReturn("lorem").when(ex).getMessage();
        // When
        ResponseEntity<Object> handleBadRequestException = restExceptionHandler.handleTypeMismatch(ex, null, null, null);
        ExceptionDto exceptionDto = (ExceptionDto)handleBadRequestException.getBody();
        // Then
        Assertions.assertNotNull(exceptionDto);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, handleBadRequestException.getStatusCode());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), exceptionDto.getStatusCode());
        Assertions.assertEquals(AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT.getMessageId(), exceptionDto.getOmaErrorMessageType().getMessageId());
        Assertions.assertEquals(AppConstants.OmaErrorMessageType.BASIC_INVALID_INPUT.getText(), exceptionDto.getOmaErrorMessageType().getText());
        Assertions.assertEquals(1, exceptionDto.getVariables().length);
        Optional<String> message = Arrays.stream(exceptionDto.getVariables()).findFirst();
        Assertions.assertTrue(message.isPresent());
        Assertions.assertEquals("lorem", message.get());
    }

    @Test
    public void givenExceptionWhenHandleMissingServletRequestParameterThenAssertResult(){
        // Given
        MissingServletRequestParameterException ex = Mockito.mock(MissingServletRequestParameterException.class);
        Mockito.doReturn("lorem").when(ex).getMessage();
        // When
        ResponseEntity<Object> handleBadRequestException = restExceptionHandler.handleMissingServletRequestParameter(ex, null, null, null);
        ExceptionDto exceptionDto = (ExceptionDto)handleBadRequestException.getBody();
        // Then
        Assertions.assertNotNull(exceptionDto);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, handleBadRequestException.getStatusCode());
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), exceptionDto.getStatusCode());
        Assertions.assertEquals(AppConstants.OmaErrorMessageType.GENERIC_SERVICE_ERROR.getMessageId(), exceptionDto.getOmaErrorMessageType().getMessageId());
        Assertions.assertEquals(AppConstants.OmaErrorMessageType.GENERIC_SERVICE_ERROR.getText(), exceptionDto.getOmaErrorMessageType().getText());
        Assertions.assertEquals(1, exceptionDto.getVariables().length);
        Optional<String> message = Arrays.stream(exceptionDto.getVariables()).findFirst();
        Assertions.assertTrue(message.isPresent());
        Assertions.assertEquals("lorem", message.get());
    }
}
