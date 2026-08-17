package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.SiteInspectionReport;
import com.noreco1.fireflyv2.model.SiteInspectionReportDescription;
import com.noreco1.fireflyv2.service.implementation.SiteInspectionReportServiceImpl;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.ArrayList;
import java.util.List;

@Component
public class SiteInspectionReportValidator implements Validator {

    private SiteInspectionReportServiceImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return SiteInspectionReport.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        SiteInspectionReport report = (SiteInspectionReport) o;

        if(report.getDate() == null) {
            errors.rejectValue("date", "siteInspectionReport.date.required");
        }

        if(report.getProject() == null) {
            errors.rejectValue("project", "siteInspectionReport.project.required");
        }
        if(report.getChecker() == null) {
            errors.rejectValue("checker", "siteInspectionReport.checker.required");
        }
        if(report.getNotedBy() == null) {
            errors.rejectValue("notedBy", "siteInspectionReport.noted.required");
        }

        List<SiteInspectionReportDescription> remarks = new ArrayList<>();

        for (SiteInspectionReportDescription r: report.getDescriptions()) {
            if(!Checker.isStringNullAndEmpty(r.getRemark()) && !Checker.isStringNullAndEmpty(r.getDescription())) {
                remarks.add(r);
            }
        }

        if(remarks.isEmpty()) {
            errors.rejectValue("descriptions", "siteInspectionReport.descriptions.required");
        } else {
            report.setDescriptions(remarks);
        }
    }

    public void setService(SiteInspectionReportServiceImpl service) {
        this.service = service;
    }
}
