package com.github.senocak.validator;

import lombok.extern.slf4j.Slf4j;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class EmailValidator implements ConstraintValidator<ValidEmail, String> {
    @Override
    public void initialize(ValidEmail constraintAnnotation) {
        log.info("EmailValidator initialized");
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context){
        if (Objects.isNull(email))
            return false;
        Pattern pattern = Pattern.compile("^[_A-Za-z0-9-+]"+
                "(.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(.[A-Za-z0-9]+)*"+ "(.[A-Za-z]{2,})$");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
