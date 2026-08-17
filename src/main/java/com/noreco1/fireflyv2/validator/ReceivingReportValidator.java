package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.DateHelper;
import com.noreco1.fireflyv2.model.ReceivingReport;
import com.noreco1.fireflyv2.service.ReceivingReportService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ReceivingReportValidator implements Validator {

    private ReceivingReportService service;

    @Override
    public boolean supports(Class<?> aClass) {
        return ReceivingReport.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        ReceivingReport rr = (ReceivingReport) o;

        if (DateHelper.getDayDifferential(rr.getDeliveryDate(), DateHelper.getServerDate()) > 30){
            errors.rejectValue("deliveryDate", "receivingReport.deliveryDate.invalid");
        }

    }

    public void setService(ReceivingReportService service) {
        this.service = service;
    }
}
