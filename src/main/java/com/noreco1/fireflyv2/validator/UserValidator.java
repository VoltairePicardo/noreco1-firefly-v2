package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.User;
import com.noreco1.fireflyv2.service.implementation.UserServiceImpl;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UserValidator implements Validator {

    private UserServiceImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return User.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        User user = (User) o;

        // insert mode
        if (user.getId() == null || user.getId() == 0) {
            errors = this.validatePasswordMatching(errors, user.getPassword(), user.getRetypePassword());
        } else {
            // has new password when updating user
            if (!Checker.isStringNullAndEmpty(user.getPassword()) || !Checker.isStringNullAndEmpty(user.getRetypePassword())) {
                errors = this.validatePasswordMatching(errors, user.getPassword(), user.getRetypePassword());
            }
        }

        // for email
        if(user.getEmail() != null && user.getEmail().trim().length() != 0){
            boolean valid = user.getEmail().matches("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");
            if (!valid) {
                errors.rejectValue("email", "user.email.invalid");
            }
        }

        User u;
        if(user.getEmail() != null && user.getEmail().trim().length() != 0){
            u = this.service.findByEmailLike(user.getEmail());
            if (u != null && !u.getId().equals(user.getId())) { // diff user
                errors.rejectValue("email", "user.email.taken");
            }
        }

        // for username
        u = this.service.findByUsername(user.getUsername());
        if (u != null && !u.getId().equals(user.getId())) { // diff user
            errors.rejectValue("username", "user.username.taken");
        }
    }

    private Errors validatePasswordMatching(Errors errors, String password, String retypePassword) {
        if (Checker.isStringNullAndEmpty(password)) {
            errors.rejectValue("password", "user.password.empty");
        } else if (password.length() < 8) {
            errors.rejectValue("password", "user.password.length.short");
        } else if (password.length() > 50) {
            errors.rejectValue("password", "user.password.length.long");
        } else {
            if (!(password.equals(retypePassword))) {
                errors.rejectValue("retypePassword", "user.password.mismatch");
            }
        }
        return errors;
    }

    public void setService(UserServiceImpl service) {
        this.service = service;
    }
}
