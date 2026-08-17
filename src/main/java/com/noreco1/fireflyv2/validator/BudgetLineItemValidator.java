package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.BudgetLineItem;
import com.noreco1.fireflyv2.model.BudgetLineItemDetail;
import com.noreco1.fireflyv2.model.Employee;
import com.noreco1.fireflyv2.model.User;
import com.noreco1.fireflyv2.repo.EmployeeRepo;
import com.noreco1.fireflyv2.service.BudgetLineItemService;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class BudgetLineItemValidator implements Validator {

    private BudgetLineItemService service;
    private AuthenticationFacade authenticationFacade;
    private EmployeeRepo employeeRepo;

    @Override
    public boolean supports(Class<?> clazz) {
        return BudgetLineItem.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        BudgetLineItem budgetLineItem = (BudgetLineItem) target;

//        // Check if we're creating a new BudgetLineItem
//        User loggedInUser = this.authenticationFacade.getLoggedIn();
//        Employee employee = this.employeeRepo.findOneByAccountNumber(loggedInUser.getAccountNo());
//
//        // Validate that the employee has an assigned division
//        if (employee.getDivision() == null) {
//            errors.rejectValue("budgetLineItemDetails", "budgetLineItem.employee.division.required");
//        } else {
//            // Check if the division has a non-empty abbreviation
//            if (Checker.isStringNullOrEmpty(employee.getDivision().getAbbreviation())) {
//                errors.rejectValue("budgetLineItemDetails", "budgetLineItem.employee.division.abbreviation.required");
//            }
//        }

    }

    public void setService(BudgetLineItemService service) {
        this.service = service;
    }

    public void setAuthenticationFacade(AuthenticationFacade authenticationFacade) {
        this.authenticationFacade = authenticationFacade;
    }

    public void setEmployeeRepo(EmployeeRepo employeeRepo) {
        this.employeeRepo = employeeRepo;
    }
}
