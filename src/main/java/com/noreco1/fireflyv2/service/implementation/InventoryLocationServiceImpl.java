package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.model.InventoryLocation;
import com.noreco1.fireflyv2.repo.InventoryLocationRepo;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.InventoryLocationService;
import com.noreco1.fireflyv2.validator.InventoryLocationValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class InventoryLocationServiceImpl implements InventoryLocationService{

    @Autowired
    private InventoryLocationRepo inventoryLocationRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Override
    public PostResponse processCreate(InventoryLocation inventoryLocation, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        try {

            response = this.validate(inventoryLocation, bindingResult, messageSource);

            if(response.isSuccess()) {

                inventoryLocation.setId(null);
                inventoryLocation.setCreatedAt(new Date());
                inventoryLocation.setUpdatedAt(new Date());
                inventoryLocation.setCreatedBy(authenticationFacade.getLoggedIn());
                InventoryLocation savedType = inventoryLocationRepo.save(inventoryLocation);

                if(savedType != null) {
                    response.setModelId(savedType.getId());
                    response.setSuccessMessage("Asset type successfully saved.");
                } else {
                    response.setFailureMessage("Failed to save new Asset Type.");
                }
            }

        } catch (Exception e) {
            Logger.getLogger(InventoryLocationServiceImpl.class.getName()).log(Level.SEVERE, null, e);
            e.printStackTrace();
        }

        return response;

    }

    @Override
    public PostResponse processUpdate(InventoryLocation inventoryLocation, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        try {

            response = this.validate(inventoryLocation, bindingResult, messageSource);

            if(response.isSuccess()) {

                InventoryLocation foundInventoryLocation = inventoryLocationRepo.findById(inventoryLocation.getId()).orElse(null);

                if(foundInventoryLocation != null) {

                    foundInventoryLocation.setDescription(inventoryLocation.getDescription());
                    foundInventoryLocation.setAccount(inventoryLocation.getAccount());
                    foundInventoryLocation.setIsSubsidy(inventoryLocation.getIsSubsidy());
                    foundInventoryLocation.setUpdatedAt(new Date());
                    inventoryLocationRepo.save(foundInventoryLocation);

                    response.setModelId(foundInventoryLocation.getId());
                    response.setSuccessMessage("Inventory Location successfully updated.");

                } else {
                    response.setFailureMessage("Inventory Location is not available.");
                }

            }

        } catch (Exception e) {
            Logger.getLogger(InventoryLocationServiceImpl.class.getName()).log(Level.SEVERE, null, e);
            e.printStackTrace();
        }

        return response;

    }

    @Override
    public Page<InventoryLocation> findAll(Pageable pageable) {
        return inventoryLocationRepo.findAll(pageable);
    }

    @Override
    public Page<InventoryLocation> find(String query, Pageable pageable) {
        query = "%"+query.trim()+"%";
        return inventoryLocationRepo.findByDescriptionContainingIgnoreCase(query, pageable);
    }

    @Override
    public InventoryLocation findById(Integer id) {
        return inventoryLocationRepo.findById(id).orElse(null);
    }

    @Override
    public InventoryLocation findByDescription(String query) {
        return inventoryLocationRepo.findByDescriptionContainingIgnoreCase(query.trim());
    }

    private PostResponse validate(InventoryLocation inventoryLocation, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        InventoryLocationValidator validator = new InventoryLocationValidator();
        validator.setService(this);
        validator.validate(inventoryLocation, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            response.setSuccess(true);
        }

        return response;

    }

}
