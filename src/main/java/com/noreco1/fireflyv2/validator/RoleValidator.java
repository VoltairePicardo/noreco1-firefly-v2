package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.Role;
import com.noreco1.fireflyv2.model.User;
import com.noreco1.fireflyv2.service.implementation.RoleServiceImpl;
import com.noreco1.fireflyv2.service.implementation.UserServiceImpl;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class RoleValidator implements Validator {

    private RoleServiceImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return Role.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Role role = (Role) o;

        Role r = this.service.findByName(role.getName());
        // insert mode
        if (role.getId() == null || role.getId() == 0) {
            if (r != null) {
                errors.rejectValue("name", "role.name.taken");
            }
        } else {
            // update role
            if (r != null && r.getId() != role.getId()) { // diff role
                errors.rejectValue("name", "role.name.taken");
            }
        }
    }

    public void setService(RoleServiceImpl service) {
        this.service = service;
    }
}
