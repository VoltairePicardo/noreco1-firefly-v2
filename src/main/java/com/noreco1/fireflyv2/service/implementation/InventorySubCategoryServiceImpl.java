package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.controller.response.InventorySubCategoryDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.InventorySubCategory;
import com.noreco1.fireflyv2.repo.InventoryCategoryRepo;
import com.noreco1.fireflyv2.repo.InventorySubCategoryRepo;
import com.noreco1.fireflyv2.service.InventorySubCategoryService;
import com.noreco1.fireflyv2.validator.InventorySubCategoryValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

@Service
public class InventorySubCategoryServiceImpl implements InventorySubCategoryService {

    @Autowired
    private InventorySubCategoryRepo inventorySubCategoryRepo;

    @Autowired
    private InventoryCategoryRepo inventoryCategoryRepo;

    @Transactional
    @Override
    public PostResponse processCreate(InventorySubCategoryDto dto, BindingResult bindingResult, MessageSource messageSource) {
        dto.setId(null);
        PostResponse response = this.validate(dto, bindingResult, messageSource);

        if (response.isSuccess()) {
            InventorySubCategory subCategory = new InventorySubCategory();
            subCategory.setDescription(dto.getDescription().trim());
            subCategory.setInventoryCategory(inventoryCategoryRepo.getReferenceById(dto.getCategoryId()));
            InventorySubCategory saved = inventorySubCategoryRepo.save(subCategory);

            response.setModelId(saved.getId());
            response.setSuccessMessage("Inventory Sub Category successfully saved.");
        }

        return response;
    }

    @Transactional
    @Override
    public PostResponse processUpdate(InventorySubCategoryDto dto, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = this.validate(dto, bindingResult, messageSource);

        if (response.isSuccess()) {
            InventorySubCategory subCategory = dto.getId() == null ? null : inventorySubCategoryRepo.findById(dto.getId()).orElse(null);

            if (subCategory == null) {
                response.setFailureMessage("Inventory Sub Category is not available.");
                return response;
            }

            subCategory.setDescription(dto.getDescription().trim());
            inventorySubCategoryRepo.save(subCategory);

            response.setModelId(subCategory.getId());
            response.setSuccessMessage("Inventory Sub Category successfully updated.");
        }

        return response;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public InventorySubCategoryDto findById(Integer id) {
        InventorySubCategory subCategory = inventorySubCategoryRepo.findById(id).orElse(null);
        if (subCategory == null) {
            return null;
        }

        InventorySubCategoryDto dto = new InventorySubCategoryDto();
        dto.setId(subCategory.getId());
        dto.setDescription(subCategory.getDescription());
        if (subCategory.getInventoryCategory() != null) {
            dto.setCategoryId(subCategory.getInventoryCategory().getId());
            dto.setCategoryDescription(subCategory.getInventoryCategory().getDescription());
        }
        return dto;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public InventorySubCategory findByCategoryAndDescription(Integer categoryId, String description) {
        return inventorySubCategoryRepo.findFirstByInventoryCategoryIdAndDescriptionIgnoreCase(categoryId, description.trim());
    }

    private PostResponse validate(InventorySubCategoryDto dto, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        InventorySubCategoryValidator validator = new InventorySubCategoryValidator();
        validator.setService(this);
        validator.validate(dto, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            response.setSuccess(true);
        }

        return response;
    }

}
