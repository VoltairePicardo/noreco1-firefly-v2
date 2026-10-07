package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.controller.response.InventoryCategoryDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.InventoryCategory;
import com.noreco1.fireflyv2.model.InventoryCategoryType;
import com.noreco1.fireflyv2.repo.InventoryCategoryRepo;
import com.noreco1.fireflyv2.repo.InventoryCategoryTypeRepo;
import com.noreco1.fireflyv2.repo.InventorySubCategoryRepo;
import com.noreco1.fireflyv2.service.InventoryCategoryService;
import com.noreco1.fireflyv2.validator.InventoryCategoryValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryCategoryServiceImpl implements InventoryCategoryService {

    @Autowired
    private InventoryCategoryRepo inventoryCategoryRepo;

    @Autowired
    private InventorySubCategoryRepo inventorySubCategoryRepo;

    @Autowired
    private InventoryCategoryTypeRepo inventoryCategoryTypeRepo;

    @Transactional
    @Override
    public PostResponse processCreate(InventoryCategoryDto dto, BindingResult bindingResult, MessageSource messageSource) {
        dto.setId(null);
        PostResponse response = this.validate(dto, bindingResult, messageSource);

        if (response.isSuccess()) {
            InventoryCategory category = new InventoryCategory();
            category.setDescription(dto.getDescription().trim());
            category.setType(inventoryCategoryTypeRepo.getReferenceById(dto.getType().getId()));
            InventoryCategory saved = inventoryCategoryRepo.save(category);

            response.setModelId(saved.getId());
            response.setSuccessMessage("Inventory Category successfully saved.");
        }

        return response;
    }

    @Transactional
    @Override
    public PostResponse processUpdate(InventoryCategoryDto dto, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = this.validate(dto, bindingResult, messageSource);

        if (response.isSuccess()) {
            InventoryCategory category = dto.getId() == null ? null : inventoryCategoryRepo.findById(dto.getId()).orElse(null);

            if (category == null) {
                response.setFailureMessage("Inventory Category is not available.");
                return response;
            }

            category.setDescription(dto.getDescription().trim());
            category.setType(inventoryCategoryTypeRepo.getReferenceById(dto.getType().getId()));
            inventoryCategoryRepo.save(category);

            response.setModelId(category.getId());
            response.setSuccessMessage("Inventory Category successfully updated.");
        }

        return response;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<InventoryCategory> findAll(Pageable pageable) {
        return inventoryCategoryRepo.findAll(pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<InventoryCategory> find(String query, Pageable pageable) {
        return inventoryCategoryRepo.findByDescriptionContainingIgnoreCase(query.trim(), pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public InventoryCategoryDto findById(Integer id) {
        InventoryCategory category = inventoryCategoryRepo.findById(id).orElse(null);
        if (category == null) {
            return null;
        }

        InventoryCategoryDto dto = new InventoryCategoryDto();
        dto.setId(category.getId());
        dto.setDescription(category.getDescription());
        dto.setType(category.getType());
        dto.setSubCategories(inventorySubCategoryRepo.findByInventoryCategoryIdOrderByDescriptionAsc(id).stream()
                .map(s -> {
                    InventoryCategoryDto.SubCategoryDto sub = new InventoryCategoryDto.SubCategoryDto();
                    sub.setId(s.getId());
                    sub.setDescription(s.getDescription());
                    return sub;
                })
                .collect(Collectors.toList()));
        return dto;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public InventoryCategory findByDescription(String description) {
        return inventoryCategoryRepo.findFirstByDescriptionIgnoreCase(description.trim());
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<InventoryCategoryType> findAllTypes() {
        return inventoryCategoryTypeRepo.findAllByOrderByDescriptionAsc();
    }

    private PostResponse validate(InventoryCategoryDto dto, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        InventoryCategoryValidator validator = new InventoryCategoryValidator();
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
