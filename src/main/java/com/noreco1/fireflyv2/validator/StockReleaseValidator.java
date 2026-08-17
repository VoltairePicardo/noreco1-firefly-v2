package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.enums.*;
import com.noreco1.fireflyv2.model.enums.InventoryCategory;
import com.noreco1.fireflyv2.model.enums.SpecialEquipmentStatus;
import com.noreco1.fireflyv2.repo.ItemRepo;
import com.noreco1.fireflyv2.repo.SpecialEquipmentRepo;
import com.noreco1.fireflyv2.controller.response.ItemTransactionDetailDto;
import com.noreco1.fireflyv2.service.StockReleaseService;
import com.noreco1.fireflyv2.service.StockWithdrawalService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class StockReleaseValidator implements Validator {

    private StockReleaseService service;
    ItemRepo itemRepo;
    SpecialEquipmentRepo specialEquipmentRepo;

    @Override
    public boolean supports(Class<?> aClass) {
        return StockRelease.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        StockRelease release = (StockRelease) o;

        if(!release.getDetails().isEmpty()){

            for (ItemTransactionDetailDto itemTransactionDetailDto : release.getDetails()){

                if(Checker.isValidId(itemTransactionDetailDto.getItemId())){
                    Item item = itemRepo.findById(itemTransactionDetailDto.getItemId()).orElse(null);
                    if(item != null){
                        if(item.getInventoryCategory() != null){
                            if(item.getInventoryCategory().getId() == InventoryCategory.SPECIAL_EQUIPMENT.getId()){
                                if(!itemTransactionDetailDto.getSerialNumbers().isEmpty()){

                                    boolean hasError = false;

                                    for(SpecialEquipment specialEquipment : itemTransactionDetailDto.getSerialNumbers()){

                                        SpecialEquipment existingSpecialEquipment = specialEquipmentRepo.findBySerialNo(specialEquipment.getSerialNo());

                                        if(existingSpecialEquipment != null){

                                            if(existingSpecialEquipment.getStatus() != null){

                                                if(existingSpecialEquipment.getStatus().getId() != SpecialEquipmentStatus.TESTED.getId()){
                                                    hasError = true;
                                                    break;
                                                }

                                            } else {
                                                hasError = true;
                                                break;
                                            }

                                        } else {
                                            hasError = true;
                                            break;
                                        }

                                    }

                                    if(hasError){
                                        errors.rejectValue("details", "stockRelease.details.special.equipment.not.tested");
                                    }

                                }
                            }
                        }

                    }
                }

            }

        }

    }

    public void setService(StockReleaseService service) {
        this.service = service;
    }

    public void setItemRepo(ItemRepo itemRepo) {
        this.itemRepo = itemRepo;
    }

    public void setSpecialEquipmentRepo(SpecialEquipmentRepo specialEquipmentRepo) {
        this.specialEquipmentRepo = specialEquipmentRepo;
    }
}
