package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.Canvass;
import com.noreco1.fireflyv2.controller.response.CanvassDetailDto;
import com.noreco1.fireflyv2.service.CanvassService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.math.BigDecimal;
import java.util.ArrayList;

@Component
public class CanvassValidator implements Validator {

    private CanvassService service;

    @Override
    public boolean supports(Class<?> aClass) {
        return Canvass.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Canvass canvass = (Canvass) o;

        if(!canvass.getSuppliers().isEmpty()) {

            ArrayList<CanvassDetailDto> canvassDetails = canvass.getCanvassDetails();
            if(!canvassDetails.isEmpty()) {

                boolean hasItemsWithPrice = false;
                for (CanvassDetailDto line:canvassDetails) {
                    if(line.getPriceSupplier1() != null && line.getPriceSupplier1().compareTo(BigDecimal.ZERO) > 0) {
                        hasItemsWithPrice = true;
                        break;
                    }
                    if(line.getPriceSupplier2() != null && line.getPriceSupplier2().compareTo(BigDecimal.ZERO) > 0) {
                        hasItemsWithPrice = true;
                        break;
                    }
                    if(line.getPriceSupplier3() != null && line.getPriceSupplier3().compareTo(BigDecimal.ZERO) > 0) {
                        hasItemsWithPrice = true;
                        break;
                    }
                }

                if(!hasItemsWithPrice) {
                    errors.rejectValue("canvassDetails", "canvass.items.noUnitPrice");
                }
            }
        }
    }

    public void setService(CanvassService service) {
        this.service = service;
    }
}
