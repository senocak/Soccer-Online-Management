package com.github.senocak.validator;

import com.github.senocak.dto.user.UserUpdateDto;
import lombok.extern.slf4j.Slf4j;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Slf4j
public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {
    @Override
    public void initialize(PasswordMatches passwordMatches) {
        log.info("PasswordMatchesValidator initialized");
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context){
        if (obj.getClass().equals(UserUpdateDto.class)) {
            UserUpdateDto userDto = (UserUpdateDto) obj;
            return userDto.getPassword().equals(userDto.getPassword_confirmation());
        }
        return false;
    }

}