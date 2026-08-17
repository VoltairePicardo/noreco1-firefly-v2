package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.WorkOrder;
import com.noreco1.fireflyv2.repo.WorkOrderRepo;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class WorkOrderValidator implements Validator {

    WorkOrderRepo workOrderRepo;

    @Override
    public boolean supports(Class<?> aClass) {
        return WorkOrder.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        WorkOrder workOrder = (WorkOrder) o;

//        if (workOrder.getDate().compareTo(workOrder.getTargetDate()) >= 0) {
//            errors.rejectValue("targetDate", "work.order.target.date.lessOrEqual");
//        }
    }
    // this how we add dependency, @Autowired is not working here!
    public void setWorkOrderRepo(WorkOrderRepo workOrderRepo) {
        this.workOrderRepo = workOrderRepo;
    }
}
