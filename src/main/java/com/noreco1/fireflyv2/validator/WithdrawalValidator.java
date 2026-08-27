package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.PurchaseOrder;
import com.noreco1.fireflyv2.model.StockWithdrawal;
import com.noreco1.fireflyv2.controller.response.StockWithdrawalDetailDto;
import com.noreco1.fireflyv2.service.PurchaseOrderService;
import com.noreco1.fireflyv2.service.StockWithdrawalService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.math.BigDecimal;

@Component
public class WithdrawalValidator implements Validator {

    private StockWithdrawalService service;

    @Override
    public boolean supports(Class<?> aClass) {
        return StockWithdrawal.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        StockWithdrawal withdrawal = (StockWithdrawal) o;

        if (Checker.collectionIsNotEmpty(withdrawal.getDetails())) {
            for (StockWithdrawalDetailDto detail : withdrawal.getDetails()) {
                boolean hasQuantity = detail.getQuantity() != null && detail.getQuantity().compareTo(BigDecimal.ZERO) > 0;
                if (hasQuantity && detail.getUnitId() == null) {
                    errors.rejectValue("details", "withdrawal.details.unit.required");
                    break;
                }
            }
        }

        if(Checker.isValidId(withdrawal.getTurnOnOrderWithdrawalId())){
            BigDecimal totalItems = BigDecimal.ZERO;
            if(Checker.collectionIsNotEmpty(withdrawal.getDetails())){
                for(StockWithdrawalDetailDto detail : withdrawal.getDetails()){
                    totalItems = totalItems.add(detail.getQuantity());
                }
            }

            if(totalItems.compareTo(BigDecimal.valueOf(withdrawal.getTurnOnOrderWithdrawal().getTotalTurnOnOrders())) < 0){
                errors.rejectValue("turnOnOrderWithdrawalId", "withdrawal.turnOnOrderWithdrawalId.invalid.quantity");
            }
        }
    }

    public void setService(StockWithdrawalService service) {
        this.service = service;
    }
}
