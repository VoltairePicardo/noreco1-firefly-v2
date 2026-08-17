package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.ProjectAcceptanceCertification;
import com.noreco1.fireflyv2.service.implementation.ProjectAcceptanceCertificationServiceImpl;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class ProjectAcceptanceCertificationValidator implements Validator {

    private ProjectAcceptanceCertificationServiceImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return ProjectAcceptanceCertification.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        ProjectAcceptanceCertification projectAcceptanceCertification = (ProjectAcceptanceCertification) o;

        if(projectAcceptanceCertification.getProject() == null) {
            errors.rejectValue("project", "projectAcceptanceCertification.project.required");
        }

    }

    public void setService(ProjectAcceptanceCertificationServiceImpl service) {
        this.service = service;
    }
}
