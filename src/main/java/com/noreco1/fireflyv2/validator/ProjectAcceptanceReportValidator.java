package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.ProjectAcceptanceReport;
import com.noreco1.fireflyv2.service.implementation.ProjectAcceptanceReportServiceImpl;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class ProjectAcceptanceReportValidator implements Validator {

    private ProjectAcceptanceReportServiceImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return ProjectAcceptanceReport.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        ProjectAcceptanceReport projectAcceptanceReport = (ProjectAcceptanceReport) o;

        if(projectAcceptanceReport.getProject() == null) {
            errors.rejectValue("project", "projectAcceptanceReport.project.required");
        }

        if(projectAcceptanceReport.getInspector1() == null) {
            errors.rejectValue("inspector1", "projectAcceptanceReport.inspector1.required");
        }

        if(projectAcceptanceReport.getInspector2() == null) {
            errors.rejectValue("inspector2", "projectAcceptanceReport.inspector3.required");
        }

        if(projectAcceptanceReport.getInspector3() == null) {
            errors.rejectValue("inspector3", "projectAcceptanceReport.inspector2.required");
        }

    }

    public void setService(ProjectAcceptanceReportServiceImpl service) {
        this.service = service;
    }
}
