package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.Project;
import com.noreco1.fireflyv2.service.implementation.ProjectServiceImpl;
import org.hibernate.annotations.Check;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ProjectValidator implements Validator {

    private ProjectServiceImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return Project.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Project project = (Project) o;

       /* if(project.getProjectFunding() == null || !Checker.isValidId(project.getProjectFunding().getId())) {
            errors.rejectValue("projectFunding", "project.funding.required");
        }
*/
        if(Checker.isStringNullOrEmpty(project.getName())) {
            errors.rejectValue("name", "project.name.required");
        }

        if(Checker.isStringNullOrEmpty(project.getLocation())) {
            errors.rejectValue("location", "project.location.required");
        }

        if(Checker.isStringNullOrEmpty(project.getPurpose())) {
            errors.rejectValue("purpose", "project.purpose.required");
        }
    }

    public void setService(ProjectServiceImpl service) {
        this.service = service;
    }
}
