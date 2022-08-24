package com.github.senocak.controller;

import com.github.senocak.exception.ServerException;
import com.github.senocak.util.AppConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.ArrayList;

@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public abstract class BaseController {

    /**
     * Check if the request is valid.
     * @param errors    BindingResult
     * @throws ServerException  if validation fails
     */
    public void hasErrors(final BindingResult errors) throws ServerException {
        if (errors.hasErrors()) {
            log.error("Error while validating the request body. AllErrors: {}", errors.getAllErrors());
            ArrayList<String> errorList = new ArrayList<>();
            for (FieldError err: errors.getFieldErrors()) {
                errorList.add(err.getField() + ": " +err.getDefaultMessage());
            }
            throw new ServerException(AppConstants.OmaErrorMessageType.JSON_SCHEMA_VALIDATOR, errorList.toArray(new String[0]),
                    HttpStatus.BAD_REQUEST);
        }
    }
}
