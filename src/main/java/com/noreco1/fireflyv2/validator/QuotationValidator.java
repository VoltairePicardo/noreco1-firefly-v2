package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.Quotation;
import com.noreco1.fireflyv2.model.Supplier;
import com.noreco1.fireflyv2.controller.response.QuotationDetailDto;
import com.noreco1.fireflyv2.controller.response.QuotationItemDetailDto;
import com.noreco1.fireflyv2.controller.response.QuotationItemDto;
import com.noreco1.fireflyv2.service.QuotationService;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.ArrayList;
import java.util.List;

public class QuotationValidator implements Validator {

    private QuotationService service;

    @Override
    public boolean supports(Class<?> aClass) {
        return false;
    }

    @Override
    public void validate(Object o, Errors errors) {

        Quotation quotation = (Quotation) o;

        ArrayList<QuotationItemDto> quotationDetails = quotation.getQuotationDetails();
        if(quotationDetails.isEmpty()) {
            errors.rejectValue("quotationDetails", "quotation.quotationDetails.isEmpty");
        } else if(quotation.getSuppliers().isEmpty()) {
            errors.rejectValue("quotationDetails", "quotation.suppliers.isEmpty");
        } else {

//            for (QuotationItemDto quotationItemDto : quotationDetails) {
//
//                List<QuotationItemDetailDto> details = quotationItemDto.getDetails();
//
//                if(quotation.getSuppliers().size() == 1) {
//
//                    QuotationItemDetailDto detailDto = details.get(0);
//
//                    if(!Checker.isAmountGreaterThanZero(detailDto.getPrice())) {
//                        errors.rejectValue("quotationDetails", "quotation.items.priceSupplier1IsEmpty");
//                        break;
//                    }
//                } else if(quotation.getSuppliers().size() == 2) {
//
//                    QuotationItemDetailDto detailDto = details.get(1);
//
//                    if(!Checker.isAmountGreaterThanZero(detailDto.getPrice())) {
//                        errors.rejectValue("quotationDetails", "quotation.items.priceSupplier2IsEmpty");
//                        break;
//                    }
//                } else if(quotation.getSuppliers().size() == 3) {
//
//                    QuotationItemDetailDto detailDto = details.get(2);
//
//                    if(!Checker.isAmountGreaterThanZero(detailDto.getPrice())) {
//                        errors.rejectValue("quotationDetails", "quotation.items.priceSupplier2IsEmpty");
//                        break;
//                    }
//                }
//            }

        }

        /*ArrayList<QuotationDetailDto> quotationDetails = quotation.getQuotationDetails();

        if(Checker.collectionIsNotEmpty(quotation.getSuppliers())) {

            if(Checker.collectionIsEmpty(quotationDetails)) {
                errors.rejectValue("quotationDetails", "quotation.quotationDetails.isEmpty");
            } else {

                Supplier supplier1 = quotation.getSuppliers().get(0);

                Supplier supplier2 = null;
                if(quotation.getSuppliers().size() > 1) supplier2 = quotation.getSuppliers().get(1);

                Supplier supplier3 = null;
                if(quotation.getSuppliers().size() > 2) supplier3 = quotation.getSuppliers().get(2);

                for (QuotationDetailDto quotationDetailDto : quotationDetails) {

                    if(quotationDetailDto.getId() == null) continue;

                    boolean okSupplierAndPrice1 = false;
                    boolean okSupplierAndPrice2 = false;
                    boolean okSupplierAndPrice3 = false;

                    if(Checker.isAmountGreaterThanZero(quotationDetailDto.getPriceSupplier1())) {   // has amount1
                        if(supplier1 != null) { // and supplier1
                            okSupplierAndPrice1 = true;
                        }
                    } else {
                        if (supplier1 == null) { // no amount and no supplier
                            okSupplierAndPrice1 = true;
                        } else {    // zero amount & has supplier
                            // check other rows if has amount
                            for (QuotationDetailDto dto : quotationDetails) {
                                if(!quotationDetailDto.getId().equals(dto.getId())) {   // exclude self
                                    if(Checker.isAmountGreaterThanZero(dto.getPriceSupplier1())) {   // has amount
                                        okSupplierAndPrice1 = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if(Checker.isAmountGreaterThanZero(quotationDetailDto.getPriceSupplier2())) {   // has amount2
                        if(supplier2 != null) { // and supplier2
                            okSupplierAndPrice2 = true;
                        }
                    } else {
                        if (supplier2 == null) { // no amount and no supplier
                            okSupplierAndPrice2 = true;
                        } else {    // zero amount & has supplier
                            // check other rows if has amount
                            for (QuotationDetailDto dto : quotationDetails) {
                                if(!quotationDetailDto.getId().equals(dto.getId())) {   // exclude self
                                    if(Checker.isAmountGreaterThanZero(dto.getPriceSupplier2())) {   // has amount
                                        okSupplierAndPrice2 = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if(Checker.isAmountGreaterThanZero(quotationDetailDto.getPriceSupplier3())) {   // has amount3
                        if(supplier3 != null) { // and supplier3
                            okSupplierAndPrice3 = true;
                        }
                    } else {
                        if (supplier3 == null) { // no amount3 and no supplier3
                            okSupplierAndPrice3 = true;
                        } else {    // zero amount & has supplier
                            // check other rows if has amount
                            for (QuotationDetailDto dto : quotationDetails) {
                                if(!quotationDetailDto.getId().equals(dto.getId())) {   // exclude self
                                    if(Checker.isAmountGreaterThanZero(dto.getPriceSupplier3())) {   // has amount3
                                        okSupplierAndPrice3 = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if(!okSupplierAndPrice1) {
                        errors.rejectValue("quotationDetails", "quotation.items.priceSupplier1IsEmpty");
                        break;
                    }

                    if(!okSupplierAndPrice2) {
                        errors.rejectValue("quotationDetails", "quotation.items.priceSupplier2IsEmpty");
                        break;
                    }

                    if(!okSupplierAndPrice3) {
                        errors.rejectValue("quotationDetails", "quotation.items.priceSupplier3IsEmpty");
                        break;
                    }

                    if(quotationDetailDto.getAwardedToSupplier1() == null &&
                                        quotationDetailDto.getAwardedToSupplier2() == null &&
                                        quotationDetailDto.getAwardedToSupplier3() == null ) {

                        errors.rejectValue("quotationDetails", "quotation.suppliers.noAward");
                        break;
                    }

                }
            }
        } else {
            errors.rejectValue("quotationDetails", "quotation.suppliers.isEmpty");
        }*/

    }

    public void setService(QuotationService service) {
        this.service = service;
    }
}
